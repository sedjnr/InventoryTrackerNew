package sed.inventorytracker;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import sed.inventorytracker.data.InventContract;

import static sed.inventorytracker.data.InventContract.*;

/**
 * Created by Sed on 16/07/2017.
 */

public class InventCursorAdapter extends CursorAdapter {

    public InventCursorAdapter (Context context, Cursor c) {
        super(context, c, 0 );
    }
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.price);
        TextView priceTextView = (TextView) view.findViewById(R.id.quantity);
        Button buttonSale = (Button) view.findViewById(R.id.sale);

        // Find the columns of pet attributes that we're interested in

        final int  idColumnIndex = cursor.getColumnIndexOrThrow(InventEntry._ID);
        int nameColumnIndex = cursor.getColumnIndexOrThrow(InventEntry.COLUMN_ITEM_NAME);
        int quantityColumnIndex = cursor.getColumnIndexOrThrow(InventEntry.COLUMN_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndexOrThrow(InventEntry.COLUMN_PRICE);
        final int quantityLeft = cursor.getInt(cursor.getColumnIndex(InventEntry.COLUMN_QUANTITY));
        buttonSale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri itemUri = ContentUris.withAppendedId(InventEntry.CONTENT_URI,idColumnIndex);
                itemSold(context,itemUri,quantityLeft);
            }
        });

        // reads attributes from cursor
        String itemName = cursor.getString(nameColumnIndex);
        String itemShipped = cursor.getString(quantityColumnIndex);
        String itemCost = cursor.getString(priceColumnIndex);

        if (TextUtils.isEmpty(itemShipped)) {
            itemName = context.getString(R.string.Unknown);
        }
        if (TextUtils.isEmpty(itemCost)) {
            itemName = context.getString(R.string.Unknown);
        }

        // Update the TextViews with the attributes for the current item
        nameTextView.setText(itemName);
        quantityTextView.setText(itemShipped);
        priceTextView.setText("£"+itemCost);
    }
    public void itemSold(Context context, Uri uri, int quantity){

        if ( quantity > 0 ) {
            int quantityNew = quantity - 1 ;

            ContentValues contentValues = new ContentValues();
            contentValues.put(InventEntry.COLUMN_QUANTITY,quantityNew);
            long mRowUpdated = context.getContentResolver().update(uri,contentValues,null,null);
        }
    }
    }

