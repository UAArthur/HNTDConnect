package net.hauntedstudio.hntdconnect.utils;

import net.hauntedstudio.hntdconnect.models.ProductPermission;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public final class ProductPermissionUtils {

    private ProductPermissionUtils() {}

    public static Set<ProductPermission> fromBits(long bits) {
        EnumSet<ProductPermission> set = EnumSet.noneOf(ProductPermission.class);
        for (ProductPermission p : ProductPermission.values()) {
            if (p.hasPermission(bits)) {
                set.add(p);
            }
        }
        return set;
    }

    public static long toBits(Collection<ProductPermission> permissions) {
        long combined = 0L;
        for (ProductPermission p : permissions) {
            combined |= p.getBit();
        }
        return combined;
    }

    public static long toBits(ProductPermission... permissions) {
        long combined = 0L;
        for (ProductPermission p : permissions) {
            combined |= p.getBit();
        }
        return combined;
    }

    public static boolean has(long storedBits, ProductPermission permission) {
        return permission.hasPermission(storedBits);
    }
}
