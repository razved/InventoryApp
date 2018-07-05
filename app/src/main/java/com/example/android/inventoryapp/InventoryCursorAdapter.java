package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InventoryCursorAdapter extends CursorAdapter {


    public InventoryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param viewGroup  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_items, viewGroup, false);
    }

    /**
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * list item layout.
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        final ViewHolder holder;
        holder = new ViewHolder(view);

        final int id = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));
        String name = cursor.getString(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_NAME));
        int price = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_PRICE));
        final int quantity = cursor.getInt(cursor.getColumnIndex(InventoryEntry.COLUMN_INVENTORY_QUANTITY));

        holder.inventoryName.setText(name);
        holder.inventoryPrice.setText(context.getString(R.string.dollar, price));
        holder.inventoryQuantity.setText(String.valueOf(quantity));

        holder.minusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // I think it isn't a good idea to use Main Activity here
                // but I don't know how it can work by other way
                InventoryList inventoryList = (InventoryList) context;
                inventoryList.decreaseQuantity(id, quantity);
            }
        });

    }

    /**
     * Helper class managed views
     */
    static class ViewHolder {
        @BindView(R.id.inventory_name_text_view_list) TextView inventoryName;
        @BindView(R.id.price_text_view_list) TextView inventoryPrice;
        @BindView(R.id.quantity_text_view_list) TextView inventoryQuantity;
        @BindView(R.id.minus_button) ImageView minusButton;

        public ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
