package com.example.android.inventoryapp.data;

import android.provider.BaseColumns;

public class InventoryContract {
    public static abstract class InventoryEntry implements BaseColumns {

        public static final String TABLE_NAME = "inventory";
        /**
         * Column names for SQLite table
         */
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_INVENTORY_NAME = "product_name";
        public static final String COLUMN_INVENTORY_PRICE = "price";
        public static final String COLUMN_INVENTORY_QUANTITY = "quantity";
        public static final String COLUMNT_INVENTORY_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMNT_INVENTORY_SUPPLIER_PHONE = "supplier_phone";

    }
}
