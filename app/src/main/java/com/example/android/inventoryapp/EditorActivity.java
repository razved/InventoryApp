package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = "EditorActivity.java";

    @BindView(R.id.name_edit_text) EditText nameEditText;
    @BindView(R.id.price_edit_text) EditText priceEditText;
    @BindView(R.id.quantity_edit_text) EditText quantityEditText;
    @BindView(R.id.supplier_name_edit_text) EditText supplierNameEditText;
    @BindView(R.id.supplier_phone_edit_text) EditText supplierPhoneEditText;

    // Constant to define CursorLoader
    private static final int ITEM_LOADER = 1;

    //If item info was changed toggle to true
    private boolean itemHasChanged = false;

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mPetHasChanged boolean to true.
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            itemHasChanged = true;
            return false;
        }
    };

    Uri itemUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        //Set all editText OnTouchListener defined above
        nameEditText.setOnTouchListener(touchListener);
        priceEditText.setOnTouchListener(touchListener);
        quantityEditText.setOnTouchListener(touchListener);
        supplierNameEditText.setOnTouchListener(touchListener);
        supplierPhoneEditText.setOnTouchListener(touchListener);

        //Get data from intent passed by Activity called this EditorActivity
        itemUri = getIntent().getData();
        Log.i(LOG_TAG, "Start EditorActivity - Uri: " + itemUri);
        if (itemUri == null) {
            setTitle(R.string.editor_activity_add);
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.editor_activity_edit);
            getLoaderManager().initLoader(ITEM_LOADER, null, this);
        }

    }

    private void saveItem() {
        ContentValues values = new ContentValues();
        //Read data from EditText fields
        String name = nameEditText.getText().toString().trim();
        String priceString = priceEditText.getText().toString();
        String quantityString = quantityEditText.getText().toString();
        int quantity = 0;
        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }
        String supplierName = supplierNameEditText.getText().toString().trim();
        String supplierPhone = supplierPhoneEditText.getText().toString().trim();

        //todo check this if statement
        if (itemUri == null &&
                (TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(supplierName) ||
                TextUtils.isEmpty(supplierPhone))) {
            return;
        }

        //Prepare inserting data and request for Database
        values.put(InventoryEntry.COLUMN_INVENTORY_NAME, name);
        values.put(InventoryEntry.COLUMN_INVENTORY_PRICE, Integer.parseInt(priceString));
        values.put(InventoryEntry.COLUMN_INVENTORY_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME, supplierName);
        values.put(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE, supplierPhone);

        if (itemUri == null) {
            Uri uri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (uri == null) {
                showToast(getString(R.string.error_saving));
            } else {
                showToast(getString(R.string.item_saved));
            }
        } else {
            int rowsUpdated = getContentResolver().update(itemUri, values, null, null);
            if (rowsUpdated == 0) {
                showToast(getString(R.string.editing_failed));
            } else {
                showToast(rowsUpdated + getString(R.string.items_was_updated));
            }
        }

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (itemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //Inflate the menu options from res menu_editor.xml file
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //If user clicked on a menu options in the app bar
        switch (item.getItemId()) {
            case R.id.action_save:
                saveItem();
                //exit Activity
                finish();
                return true;
            case R.id.action_delete:
                // TODO: 04.07.2018 Удаляем
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                //
                // TODO: 04.07.2018 Делаем проверку на введенные данные и запрос если надо
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
