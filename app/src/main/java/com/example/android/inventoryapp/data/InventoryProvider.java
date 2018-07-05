package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryProvider extends ContentProvider {

    /** URI matcher code for the content URI for the pets table */
    private static final int INVENTORYES = 100;
    /** URI matche code for the content URI for a single pet in the pets table */
    private static final int INVENTORY_ID = 101;
    /** Tag for the log messages */
    private static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * THe input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //Static initializer. This is run the first time anything is called from this class.
    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS, INVENTORYES);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_ITEMS + "/#", INVENTORY_ID);
    }

    private InventoryDbHelper dbHelper;

    @Override
    public boolean onCreate() {
        dbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    /**
     * Perform the query for the given URI. Use the given projection, selection, selection arguments, and sort order.
     */
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Get readable database
        SQLiteDatabase database = dbHelper.getReadableDatabase();
        // This cursor will hold the result of the query
        Cursor cursor;

        //Figure out if the URI mathcer can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORYES:
                //For the INVENTORIES code, query the inventory table directly with the given
                //projection, selection, selection args, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ID:
                // For the INVENTORY_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.inventoryapp/inventory/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection,
                        selectionArgs, null, null, sortOrder);
                break;
             default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORYES:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    /**
     * Insert new data into the provider with the given ContentValues.
     */
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORYES:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert an Item into the database with the given content values.
     * @param uri Uri to insert
     * @param values values to insert into database
     * @return  the new content URI for that specific row in the database.
     */
    private Uri insertItem(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //Sanity check of data
        //Check the Inventory name is not null
        String inventoryName = values.getAsString(InventoryEntry.COLUMN_INVENTORY_NAME);
        if (inventoryName == null) {
            throw  new IllegalArgumentException("Item requires a name");
        }
        //Check price
        Integer inventoryPrice = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_PRICE);
        if (inventoryPrice == null) {
            throw  new IllegalArgumentException("Item requires a price");
        }
        //Check quantity
        Integer inventoryQuantity = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
        if (inventoryQuantity == null || inventoryQuantity < 0) {
            throw  new IllegalArgumentException("Quantity must be positive");
        }
        //Check supplier name
        String supplierName = values.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
        if (supplierName == null) {
            throw  new IllegalArgumentException("Item requires a supplier name");
        }
        //Check supplier phone
        String supplierPhone = values.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE);
        if (supplierPhone == null) {
            throw  new IllegalArgumentException("Item requires a supplier phone");
        }

        long id = db.insert(InventoryEntry.TABLE_NAME, null, values);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        if (id == -1) {
            Toast.makeText(getContext(), R.string.error_add_new, Toast.LENGTH_SHORT).show();
            return null;
        }
        //Notify all listeners that the data has changed for the item content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);

    }

    /**
     * Delete the data at the given selection and selection arguments.
     */
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = dbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORYES:
                //Delete all rows that math the selection and selection arguments
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);

        }
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORYES:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                // For the PET_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
    /**
     * Update item in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more items).
     * Return the number of rows that were successfully updated.
     */
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // check that the name value is present and not null.
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_NAME)) {
            String name = values.getAsString(InventoryEntry.COLUMN_INVENTORY_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item requires a name");
            }
        }
        // check that the price value is present and not null.
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_PRICE)) {
            Integer price = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Item requires a price");
            }
        }
        // check that the quantity value is present and not null.
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            if (quantity < 0) {
                throw new IllegalArgumentException("Quantity should be positive");
            }
        }
        // check that the supplier name value is present and not null.
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME)) {
            String supplierName = values.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME);
            if (supplierName == null) {
                throw new IllegalArgumentException("Item requires a suppliers name");
            }
        }
        // check that the supplier phone value is present and not null.
        if (values.containsKey(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE)) {
            String supplierPhone = values.getAsString(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE);
            if (supplierPhone == null) {
                throw new IllegalArgumentException("Item requires a suppliers name");
            }
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = db.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }


}
