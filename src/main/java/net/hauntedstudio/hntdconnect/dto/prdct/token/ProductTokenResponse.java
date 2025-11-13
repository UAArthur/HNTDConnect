package net.hauntedstudio.hntdconnect.dto.prdct.token;

public record ProductTokenResponse(
        String organizationId,
        String productId,
//        String tokenName, only for DB
        String tokenId,
        String token) {}
