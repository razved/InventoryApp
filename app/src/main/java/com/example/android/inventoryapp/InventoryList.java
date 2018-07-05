package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InventoryList extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    final static private int MAX_DUMMY_PRICE = 300;
    final static private int MAX_DUMMY_QUANTITY = 300;

    //Cursor Loader ID
    private static final int INVENTORY_LOADER = 0;
    //constant for log files
    private static final String LOG_TAG = "InventoryList.java";
    //Adapter for creating Items list of Inventory
    InventoryCursorAdapter inventoryAdapter;
    //Bind views to variables

    @BindView(R.id.inventory_list_view) ListView inventoryListView;
    @BindView(R.id.fab) FloatingActionButton fab;
    @BindView(R.id.empty_view) View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_list);
        ButterKnife.bind(this);

        //Setup FAB to open EditorActivity
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InventoryList.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        //Set empty view for ListView
        inventoryListView.setEmptyView(emptyView);

        //Setup an Adapter to create a list item for each row of inventory data
        //in the Cursor. There are no data until the loader finishes so pass in null for the Cursor.
        inventoryAdapter = new InventoryCursorAdapter(this, null);
        inventoryListView.setAdapter(inventoryAdapter);

        inventoryListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create intent of EditorActivity and pass Uri as data
                Intent intent = new Intent(InventoryList.this, EditorActivity.class);
                Log.i(LOG_TAG, "Uri: " + ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id));
                intent.setData(ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id));
                startActivity(intent);
            }
        });


        //Kick off the loader
        getLoaderManager().initLoader(INVENTORY_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu options from the res/menu/menu_inventory_list.xml
        // This adds menu items to the app bar
        getMenuInflater().inflate(R.menu.menu_inventory_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //If User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                showToast(getString(R.string.inserting_message));
                insertDummyData();

                return true;
            case R.id.action_show_number_of_items:
                int itemsCount = numberOfItems(queryData());
                showToast(getString(R.string.number_of_items_beginning) + itemsCount +
                        getString(R.string.number_of_items_ending));
                return true;
            case R.id.action_delete_all_items:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_copyright:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Add new Item to Database
     * @param name name of item
     * @param price item price
     * @param quantity quatity of item
     * @param supplierName name of supplier
     * @param supplierPhone phone number of supplier
     */
    private void insertData(String name, int price,
                            int quantity,
                            String supplierName,
                            String supplierPhone) {

        ContentValues values = new ContentValues();

        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, name);
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, price);
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME, supplierName);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE, supplierPhone);

        getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
     }

     private void showDeleteConfirmationDialog() {
         //Create an AlertDialog.Builder and set the message, and click listeners
         //for the positive and negative buttons on the dialog
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setMessage(R.string.delete_all_dialog_msg);
         builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 //User clicked the "Delete" button
                 deleteItems();
             }
         });
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialogInterface, int i) {
                 //User clicked the "Cancel" button, so dissmiss the dialog
                 if (dialogInterface != null) {
                     dialogInterface.dismiss();
                 }
             }
         });
         //Create and show the AlertDialog
         AlertDialog alertDialog = builder.create();
         alertDialog.show();
     }

    /**
     * This method add to database dummy-data item generated randomly
     * from prepared mock's in DummyData class
     */
    private void insertDummyData() {
        //initialize random to generate Dummy-data
        Random random = new Random();

        //Generate random Dummy-data
        String rndName = DummyData.nameStrings[random.nextInt(DummyData.nameStrings.length)];
        //Add 1 to avoid 0$ price
        int rndPrice = random.nextInt(MAX_DUMMY_PRICE) + 1;
        int rndQuantity = random.nextInt(MAX_DUMMY_QUANTITY);
        String rndSupplierName = DummyData
                .supplierNameStrings[random.nextInt(DummyData.supplierNameStrings.length)];
        String rndSupplierPhone = DummyData
                .supplierPhoneStrings[random.nextInt(DummyData.supplierPhoneStrings.length)];
        //add Dummy data
        insertData(rndName, rndPrice, rndQuantity, rndSupplierName, rndSupplierPhone);
    }

    /**
     * This method retrieve all data from InventoryEntry.TABLE_NAME
     * @return all data from InventoryEntry.TABLE_NAME as Cursor
     */
    private Cursor queryData() {
        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projections = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE
        };

        // Perform this raw SQL query "SELECT * FROM inventory"
        // to get a Cursor that contains all rows from the pets table.
        Cursor cursor = getContentResolver().query(InventoryEntry.CONTENT_URI,
                projections,
                null,
                null,
                null);
        return cursor;
    }

    /**
     * This method return number of items in cursor
     * @param cursor database's data
     * @return number of items in cursor
     */
    private int numberOfItems(Cursor cursor) {
        int counts = cursor.getCount();
        cursor.close();
        return counts;
    }

    private void deleteItems() {
        int rowsDeleted = getContentResolver().delete(InventoryEntry.CONTENT_URI, null, null);
        if (rowsDeleted != 0) {
            showToast(rowsDeleted + getString(R.string.items_was_deleted));
        } else {
            showToast(getString(R.string.error_deleting));
        }
    }


    public void decreaseQuantity(int id, int quantity) {
        ContentValues values = new ContentValues();
        Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
        //if quantity more than 1 decrease quantity
        if (quantity >= 1) {
            values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity - 1);
            int rowsUpdated = getContentResolver().update(uri, values, null, null);
            showToast(getString(R.string.decreased));
        } else {
            showToast(getString(R.string.quantity_zero));
        }
    }


    /**
     * Helper method to show Toast message (for LENGTH_SHORT time)
     * @param message text to show
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY,
        };
        return new CursorLoader(this, InventoryEntry.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        inventoryAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        inventoryAdapter.swapCursor(null);
    }
}
