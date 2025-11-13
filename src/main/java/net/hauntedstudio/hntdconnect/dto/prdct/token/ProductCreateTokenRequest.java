package net.hauntedstudio.hntdconnect.dto.prdct.token;

public record ProductCreateTokenRequest(
        String organizationId,
        String productId,
        String name,
        long permissions) {}
