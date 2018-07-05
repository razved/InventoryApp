package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = "EditorActivity.java";
    // Code for asking permission for dealing telephone number

    @BindView(R.id.name_edit_text) EditText nameEditText;
    @BindView(R.id.price_edit_text) EditText priceEditText;
    @BindView(R.id.quantity_edit_text) EditText quantityEditText;
    @BindView(R.id.supplier_name_edit_text) EditText supplierNameEditText;
    @BindView(R.id.supplier_phone_edit_text) EditText supplierPhoneEditText;
    @BindView(R.id.contact_button) Button contactButton;

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
        if (itemUri == null) {
            setTitle(R.string.editor_activity_add);
            contactButton.setVisibility(View.GONE);
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

        if (itemUri == null &&
                (TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(supplierName) ||
                TextUtils.isEmpty(supplierPhone))) {
            showToast("You need to enter something to each field");
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
        finish();

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

    /** If user choose menu item */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //If user clicked on a menu options in the app bar
        switch (item.getItemId()) {
            case R.id.action_save:
                saveItem();
                if (!itemHasChanged) {
                    //exit Activity
                    finish();
                }
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:

                if (!itemHasChanged) {
                    // Navigate back to parent activity (CatalogActivity)
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //user clicked "Discard" button, navigate to parent activity
                        NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    }
                };
                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /** If user started change data but pressed back without saving, ask him for really he wont
     * go back without saving */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        //Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative button on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        //Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    /** If user want to delete some Item, ask him for really he wont it */
    private void showDeleteConfirmationDialog() {
        //Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_one_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //User Clicked the "Delete" button
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //User clicked the "Cancel" button, so dismiss the dialog
                //And continue editing
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        //Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /** Perform the deletion of the Item in the database */
    private void deleteItem() {
        int rowsDeleted = getContentResolver().delete(itemUri, null, null);
        if (rowsDeleted != 0) {
            showToast(rowsDeleted + getString(R.string.items_was_deleted));
        } else {
            showToast(getString(R.string.error_deleting));
        }
        finish();
    }

    /**  Make request to Database to get current Item values */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_INVENTORY_NAME,
                InventoryEntry.COLUMN_INVENTORY_PRICE,
                InventoryEntry.COLUMN_INVENTORY_QUANTITY,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME,
                InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE
        };
        if (itemUri == null) {
            return null;
        }
        return new CursorLoader(this, itemUri, projection, null, null, null);
    }

    /** populate EditText fields by values of current Item */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            String name = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME));
            int price = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE));
            int quantity = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY));
            String supplierName = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_NAME));
            String supplierPhone = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_SUPPLIER_PHONE));

            nameEditText.setText(name);
            priceEditText.setText(String.valueOf(price));
            quantityEditText.setText(String.valueOf(quantity));
            supplierNameEditText.setText(supplierName);
            supplierPhoneEditText.setText(supplierPhone);
        }
    }

    /** Populate EditText fields by nothing */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.setText("");
        priceEditText.setText("");
        quantityEditText.setText("");
        supplierNameEditText.setText("");
        supplierPhoneEditText.setText("");
    }

    @Override
    public void onBackPressed() {
        //If the pet hasn't changed, continue with handling back button press
        if (!itemHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    /** Helper method to show Toast */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * User pressed Quantity Down button. Decrease quantity by 1, check it will not be negative
     */
    @OnClick(R.id.quantity_down_button)
    public void quantityDown(View view) {
        int quantity = 1;
        // if nothing entered to field, set quantity = 1 (and decrease it next :)
        if (!TextUtils.isEmpty(quantityEditText.getText().toString())) {
            quantity = Integer.parseInt(quantityEditText.getText().toString());
        }
        itemHasChanged = true;
        if (quantity >= 1) {
            quantity--;
        }
        quantityEditText.setText(String.valueOf(quantity));
    }

    /**
     * User pressed Quantity Up button. Increase quantity by 1
     */
    @OnClick(R.id.quantity_up_button)
    public void quantityUp(View view) {
        int quantity = 0;
        // if nothing entered to field, set quantity = 0 (and increase it next :)
        if (!TextUtils.isEmpty(quantityEditText.getText().toString())) {
            quantity = Integer.parseInt(quantityEditText.getText().toString());
        }
        itemHasChanged = true;
        quantity++;
        quantityEditText.setText(String.valueOf(quantity));
    }

    /**
     * User pressed Price Down button. Decrease price by 1, check it will not be negative
     */
    @OnClick(R.id.price_down_button)
    public void priceDown(View view) {
        int price = 1;
        // if nothing entered to field, set price = 1 (and decrease it next :)
        if (!TextUtils.isEmpty(priceEditText.getText().toString())) {
            price = Integer.parseInt(priceEditText.getText().toString());
        }
        itemHasChanged = true;
        if (price >= 1) {
            price--;
        }
        priceEditText.setText(String.valueOf(price));
    }

    /**
     * User pressed Price Up button. Increase price by 1
     */
    @OnClick(R.id.price_up_button)
    public void priceUp(View view) {
        int price = 0;
        // if nothing entered to field, set price 0 (and increase it next :)
        if (!TextUtils.isEmpty(priceEditText.getText().toString())) {
            price = Integer.parseInt(priceEditText.getText().toString());
        }
        itemHasChanged = true;
        price++;
        priceEditText.setText(String.valueOf(price));
    }

    /**
     * User pressed Contact the supplier button, so show dealing number
     */
    @OnClick(R.id.contact_button)
    public void contactSupplier(View view) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        String phone = getString(R.string.tel_uri_prefix) + supplierPhoneEditText.getText().toString();
        intent.setData(Uri.parse(phone));
        startActivity(intent);
    }

}
