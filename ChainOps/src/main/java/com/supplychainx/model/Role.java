package com.supplychainx.model;

/**
 * Enterprise user roles for role-based access control (RBAC).
 */
public enum Role {
    ADMIN(1, "Administrator", "Full system access"),
    WAREHOUSE_MANAGER(2, "Warehouse Manager", "Stock, inventory and transfer control"),
    PROCUREMENT_MANAGER(3, "Procurement Manager", "Supplier and Purchase Order control"),
    SALES_MANAGER(4, "Sales Manager", "Sales orders and shipment fulfillment");

    private final int id;
    private final String displayName;
    private final String description;

    Role(int id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
    }

    public int getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getDescription() { return description; }

    public static Role fromId(int id) {
        for (Role r : values()) {
            if (r.id == id) return r;
        }
        return WAREHOUSE_MANAGER;
    }

    public static Role fromString(String name) {
        try {
            return Role.valueOf(name.toUpperCase());
        } catch (Exception e) {
            return WAREHOUSE_MANAGER;
        }
    }
}
