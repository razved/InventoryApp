package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class InventoryContract {

    private InventoryContract() {}

    /** Content authority URI to access data in the provider */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_ITEMS = "inventory";

    public static abstract class InventoryEntry implements BaseColumns {

        public static final String TABLE_NAME = "inventory";
        /**
         * Column names for SQLite table
         */
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_INVENTORY_NAME = "product_name";
        public static final String COLUMN_INVENTORY_PRICE = "price";
        public static final String COLUMN_INVENTORY_QUANTITY = "quantity";
        public static final String COLUMN_INVENTORY_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_INVENTORY_SUPPLIER_PHONE = "supplier_phone";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a lsit of pets
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;
        /**
         * The MIME type of {@link #CONTENT_URI} for a single pet
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;
    }
}
