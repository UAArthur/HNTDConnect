package net.hauntedstudio.hntdconnect.models;

import lombok.Getter;

@Getter
public enum ProductPermission {
    // Authentication permissions
    AUTH_LOGIN(1L << 1),
    AUTH_REGISTER(1L << 2),

    // User storage permissions
    USER_READ(1L << 3),
    USER_WRITE(1L << 4),
    USER_DELETE(1L << 5);


    private final long bit;

    ProductPermission(long bit) {
        this.bit = bit;
    }

    public boolean hasPermission(long permissions) {
        return (permissions & this.bit) == this.bit;
    }

    public static long combinePermissions(ProductPermission... permissions) {
        long combined = 0L;
        for (ProductPermission permission : permissions) {
            combined |= permission.bit;
        }
        return combined;
    }
}
