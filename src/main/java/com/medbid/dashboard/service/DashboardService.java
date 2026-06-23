package com.medbid.dashboard.service;

import com.medbid.dashboard.dto.DashboardStats;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;

    public DashboardStats getStats() {
        long totalTenders = queryCount("SELECT COUNT(*) FROM tenders WHERE deleted = false");
        long activeTenders = queryCount("SELECT COUNT(*) FROM tenders WHERE deleted = false AND status IN ('DRAFT','REVIEWING','APPROVED','SUBMITTED')");
        long wonTenders = queryCount("SELECT COUNT(*) FROM tenders WHERE deleted = false AND status = 'WON'");
        long lostTenders = queryCount("SELECT COUNT(*) FROM tenders WHERE deleted = false AND status = 'LOST'");
        long totalProducts = queryCount("SELECT COUNT(*) FROM products WHERE deleted = false");
        long expiringDocuments = queryCount(
                "SELECT COUNT(*) FROM legal_documents WHERE expiry_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '30 days'");

        BigDecimal totalRevenue = queryBigDecimal(
                "SELECT COALESCE(SUM(estimated_value), 0) FROM tenders WHERE status = 'WON' AND deleted = false");

        BigDecimal winRate = totalTenders > 0
                ? BigDecimal.valueOf(wonTenders).divide(BigDecimal.valueOf(totalTenders), 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        Map<String, Long> tendersByStatus = queryStatusMap();

        List<DashboardStats.MonthlyStats> monthlyStats = getMonthlyStats();
        List<DashboardStats.ProductCategoryStats> topCategories = getTopCategories();
        List<DashboardStats.PriceTrendPoint> priceTrend = List.of();

        return new DashboardStats(
                totalTenders, activeTenders, wonTenders, lostTenders,
                totalProducts, expiringDocuments, totalRevenue, winRate,
                tendersByStatus, monthlyStats, topCategories, priceTrend
        );
    }

    private long queryCount(String sql) {
        Long result = jdbcTemplate.queryForObject(sql, Long.class);
        return result != null ? result : 0L;
    }

    private BigDecimal queryBigDecimal(String sql) {
        BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class);
        return result != null ? result : BigDecimal.ZERO;
    }

    private Map<String, Long> queryStatusMap() {
        Map<String, Long> map = new LinkedHashMap<>();
        jdbcTemplate.query(
                "SELECT status, COUNT(*) FROM tenders WHERE deleted = false GROUP BY status",
                (rs) -> {
                    map.put(rs.getString(1), rs.getLong(2));
                }
        );
        return map;
    }

    private List<DashboardStats.MonthlyStats> getMonthlyStats() {
        List<DashboardStats.MonthlyStats> stats = new ArrayList<>();
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        jdbcTemplate.query(
                "SELECT TO_CHAR(created_at, 'YYYY-MM'), COUNT(*), COALESCE(SUM(estimated_value), 0) " +
                "FROM tenders WHERE deleted = false AND created_at >= ? " +
                "GROUP BY TO_CHAR(created_at, 'YYYY-MM') ORDER BY 1",
                ps -> ps.setDate(1, java.sql.Date.valueOf(sixMonthsAgo)),
                (rs) -> {
                    stats.add(new DashboardStats.MonthlyStats(
                            rs.getString(1), rs.getLong(2), rs.getBigDecimal(3)));
                }
        );
        return stats;
    }

    private List<DashboardStats.ProductCategoryStats> getTopCategories() {
        List<DashboardStats.ProductCategoryStats> cats = new ArrayList<>();
        jdbcTemplate.query(
                "SELECT COALESCE(category, 'Khác'), COUNT(*), COALESCE(AVG(CAST(technical_specs->>'price' AS DECIMAL)), 0) " +
                "FROM products WHERE deleted = false GROUP BY category ORDER BY 2 DESC LIMIT 10",
                (rs) -> {
                    cats.add(new DashboardStats.ProductCategoryStats(
                            rs.getString(1), rs.getLong(2), rs.getBigDecimal(3)));
                }
        );
        return cats;
    }
}
