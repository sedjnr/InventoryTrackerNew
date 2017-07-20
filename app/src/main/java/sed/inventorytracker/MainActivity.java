package sed.inventorytracker;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import sed.inventorytracker.data.InventContract.InventEntry;

import static android.R.attr.id;
import static sed.inventorytracker.R.id.price;
import static sed.inventorytracker.R.id.quantity;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENT_LOADER = 0;
    // adapter for listview
    private InventCursorAdapter mCursorAdapter;
    public Uri mCurrentItemUri = ContentUris.withAppendedId(InventEntry.CONTENT_URI,id);;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup button to open EditorActivity
         Button insertbutton = (Button) findViewById(R.id.insertbutton);
        insertbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, InsertActivity.class);
                startActivity(intent);
            }
        });

        // Initialises listview
        ListView inventListView = (ListView) findViewById(R.id.list_item);
        View emptyView = findViewById(R.id.emptyview);
        inventListView.setEmptyView(emptyView);

        // Creates adaptor which makes list item for every row of data in table
        mCursorAdapter = new InventCursorAdapter(this,null);
        inventListView.setAdapter(mCursorAdapter);

        inventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this,UpdateActivity.class);

                // sets uri to uri of the item clicked on by adding id onto content uri
                Uri currentItemUri = ContentUris.withAppendedId(InventEntry.CONTENT_URI,id);
                intent.setData(currentItemUri);
                startActivity(intent);
            }
        });

        // starts loader manager
        getLoaderManager().initLoader(INVENT_LOADER,null,this);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void deleteAllItems() {
        int rowsDeleted = getContentResolver().delete(InventEntry.CONTENT_URI, null, null);
        Log.v("MainActivity", rowsDeleted + " rows deleted from inventory database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllItems();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Create projection for list item view
        String[] projection = {
                InventEntry._ID,
                InventEntry.COLUMN_ITEM_NAME,
                InventEntry.COLUMN_QUANTITY,
                InventEntry.COLUMN_PRICE};

        return new CursorLoader(this,
                InventEntry.CONTENT_URI,
                projection,
                null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // if cursor is null do not execute
        if (data == null) {
            return;
        }
        if ((data.moveToFirst())) {
            int quantityColumnIndex = data.getColumnIndex(InventEntry.COLUMN_QUANTITY);

            // gets data from cursor
            final int quantity = data.getInt(quantityColumnIndex);

            View inflatedView = getLayoutInflater().inflate(R.layout.list_item, null);
            Button salebuton = (Button) inflatedView.findViewById(R.id.sale);
            salebuton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    productSold(mCurrentItemUri, (quantity - 1));
                }
            });

            // updates cursor adapter with new cursor data
            mCursorAdapter.swapCursor(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);

    }

    private int productSold(Uri uri, int mQuantity){
        ContentValues values = new ContentValues();
        values.put(InventEntry.COLUMN_QUANTITY, mQuantity);
        int numRowsUpdated = getContentResolver().update(uri, values, null, null);
        return numRowsUpdated;
    }
}

