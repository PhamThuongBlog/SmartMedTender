package com.medbid.product.dto;

public record ProductSearchRequest(
        String category,
        String manufacturer,
        String brand,
        String keyword
) {}
