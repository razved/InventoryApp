package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "inventory.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + InventoryEntry.TABLE_NAME + " (" +
            InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            InventoryEntry.COLUMN_INVENTORY_NAME + " TEXT NOT NULL, " +
            InventoryEntry.COLUMN_INVENTORY_PRICE + " INTEGER NOT NULL, " +
            InventoryEntry.COLUMN_INVENTORY_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
            InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME + " TEXT NOT NULL, " +
            InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE + " TEXT NOT NULL" +
            ");";
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " +
            InventoryEntry.TABLE_NAME;

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
