package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;
import com.example.android.inventoryapp.data.InventoryDbHelper;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InventoryList extends AppCompatActivity {

    InventoryDbHelper dbHelper;

    final static private int MAX_DUMMY_PRICE = 300;
    final static private int MAX_DUMMY_QUANTITY = 300;
    final static private String SORT_ORDER_ASC = " ASC";
    final static private String SORT_ORDER_DESC = " DESC";
    final static private String NEW_LINE = "\n";

    @BindView(R.id.text_view) TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_list);
        ButterKnife.bind(this);

        dbHelper = new InventoryDbHelper(this);
        displayData(queryData());
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
                displayData(queryData());
                return true;
            case R.id.action_show_number_of_items:
                int itemsCount = numberOfItems(queryData());
                showToast(getString(R.string.number_of_items_beginning) + itemsCount +
                        getString(R.string.number_of_items_ending));
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
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, name);
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, price);
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMNT_INVENTORY_SUPPLIER_NAME, supplierName);
        values.put(InventoryEntry.COLUMNT_INVENTORY_SUPPLIER_PHONE, supplierPhone);
        long newRowId = db.insert(InventoryEntry.TABLE_NAME, null, values);
        if (newRowId == -1) {
            showToast("Adding new Item was wrong");
        }
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
        int rndPrice = random.nextInt(MAX_DUMMY_PRICE);
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
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Define a projection that specifies which columns from the database
        // you will actually use after this query.
        String[] projections = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryEntry.COLUMNT_INVENTORY_SUPPLIER_NAME,
                InventoryEntry.COLUMNT_INVENTORY_SUPPLIER_PHONE
        };
        // How you want the results sorted in the resulting Cursor
        String sortOrder = InventoryEntry._ID + SORT_ORDER_ASC;
        // Perform this raw SQL query "SELECT * FROM inventory"
        // to get a Cursor that contains all rows from the pets table.
        Cursor cursor = db.query(InventoryEntry.TABLE_NAME,
                projections,
                null,
                null,
                null,
                null,
                sortOrder);
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

    /**
     * Display data from table to TextView
     * @param cursor cursor recieved data from table
     */
    private void displayData(Cursor cursor) {
        try {
            textView.setText(getString(R.string.rows_in_table) + cursor.getCount() + NEW_LINE);
            textView.append(InventoryEntry._ID + " - " +
            InventoryEntry.COLUMN_INVENTORY_NAME + " - " +
            InventoryEntry.COLUMN_INVENTORY_PRICE + " - " +
            InventoryEntry.COLUMN_INVENTORY_QUANTITY + " - " +
            InventoryEntry.COLUMNT_INVENTORY_SUPPLIER_NAME + " - " +
            InventoryEntry.COLUMNT_INVENTORY_SUPPLIER_PHONE);

            //indexes of table columns
            int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY);
            int supplierNameColumnIndex =  cursor.getColumnIndex(InventoryEntry.COLUMNT_INVENTORY_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMNT_INVENTORY_SUPPLIER_PHONE);

            // Add items from table line by line to TextView
            while (cursor.moveToNext()) {
                int currentId = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                int currentPrice = cursor.getInt(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                String currentSupplierName = cursor.getString(supplierNameColumnIndex);
                String currentSupplierPhone = cursor.getString(supplierPhoneColumnIndex);

                textView.append(NEW_LINE + currentId + " - " +
                    currentName + " - " +
                    currentPrice + " - " +
                    currentQuantity + " - " +
                    currentSupplierName + " - " +
                    currentSupplierPhone);
            }
        } finally {
            cursor.close();
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}