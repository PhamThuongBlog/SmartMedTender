package com.medbid.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record DashboardStats(
        long totalTenders,
        long activeTenders,
        long wonTenders,
        long lostTenders,
        long totalProducts,
        long expiringDocuments,
        BigDecimal totalRevenue,
        BigDecimal winRate,
        Map<String, Long> tendersByStatus,
        List<MonthlyStats> monthlyStats,
        List<ProductCategoryStats> topCategories,
        List<PriceTrendPoint> priceTrend
) {
    public record MonthlyStats(String month, long tenderCount, BigDecimal revenue) {}
    public record ProductCategoryStats(String category, long count, BigDecimal avgPrice) {}
    public record PriceTrendPoint(String productName, List<BigDecimal> prices, List<String> dates) {}
}
