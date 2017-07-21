package sed.inventorytracker.data;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import sed.inventorytracker.data.InventContract.InventEntry;

import static sed.inventorytracker.R.id.quantity;
import static sed.inventorytracker.data.InventContract.InventEntry.COLUMN_SHIPPED;

/**
 * Created by Sed on 15/07/2017.
 */

public class InventProvider  extends ContentProvider {

    public static final String LOG_TAG = InventProvider.class.getSimpleName();

    private static final int INVENT = 100;
    private static final int INVENT_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventContract.CONTENT_AUTHORITY,InventContract.PATH_INVENT,INVENT);
        sUriMatcher.addURI(InventContract.CONTENT_AUTHORITY,InventContract.PATH_INVENT + "/#",INVENT_ID);
    }
    //Initialize the global DBhelper to be referenced for content provider methods
    InventDBHelper mDBHelper;

    @Override
    public boolean onCreate() {
        // gain access to database
         mDBHelper = new InventDBHelper(getContext());
        return true;
    }

    // Performs query for cursor uri
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        // Cursor holds results of query
        Cursor cursor;
        // See if uri matcher matches to Invent or Invent_ID
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENT: // query to get all values from table
                cursor = database.query(InventEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENT_ID: // Performs query on specific item from uri id
                selection = InventEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown uri " + uri);
        }
        // set notifcation uri on cursor in case data is changed
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;
        }

    @Override // Where to put data and what to put into table
    public Uri insert(Uri uri, ContentValues contentValues) {

        // check if it maches any code, only one case as it doesnt make sense to add data to a row that already exists
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENT:
                return insertItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }
    // Seperates insert method to make it easier to read
    private Uri insertItem(Uri uri, ContentValues contentValues) {

        String name = contentValues.getAsString(InventEntry.COLUMN_ITEM_NAME);
        if (name == null ) {
            throw new IllegalArgumentException("Requires name");
        }

        String supplier = contentValues.getAsString(InventEntry.COLUMN_SUPPLIER);
        if (supplier == null ) {
            throw new IllegalArgumentException("Requires supplier");
        }
        Integer price = contentValues.getAsInteger(InventEntry.COLUMN_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Requires price");
        }
        Integer quantity = contentValues.getAsInteger(InventEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Requires quantity");
        }
        Integer shipped = contentValues.getAsInteger(COLUMN_SHIPPED);
        if (shipped != null && !InventEntry.isValidShipped(shipped) ) {
            throw  new IllegalArgumentException("requires valid ship status");
        }
        String image = contentValues.getAsString(InventEntry.COLUMN_IMAGE);
        if (image == null ) {
            throw new IllegalArgumentException("Requires image");
        }

        // enables writing to database
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        // adds pet with the given values
        long id = database.insert(InventEntry.TABLE_NAME,null,contentValues);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri,null);
        return ContentUris.withAppendedId(uri,id);
    }
    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENT:
                return updateItem(uri, contentValues, selection, selectionArgs);
            case INVENT_ID:
                selection = InventEntry._ID + "=?";
                selectionArgs = new String[] {
                        String.valueOf(ContentUris.parseId(uri))
                };
                return updateItem(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }

    }

    // Updates table with given values
    private int updateItem(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

            if (values.containsKey(InventEntry.COLUMN_ITEM_NAME)) {
                String name = values.getAsString(InventEntry.COLUMN_ITEM_NAME);
                if (name == null) {
                    throw new IllegalArgumentException("item name required");
                }
            }
            if (values.containsKey(InventEntry.COLUMN_SUPPLIER)) {
                String supplier = values.getAsString(InventEntry.COLUMN_SUPPLIER);
                if (supplier == null) {
                    throw new IllegalArgumentException("supplier name required");
                }
            }
            if (values.containsKey(InventEntry.COLUMN_PRICE)) {
                // Check that the weight is greater than or equal to 0 kg
                Integer price = values.getAsInteger(InventEntry.COLUMN_PRICE);
                if (price != null && price < 0) {
                    throw new IllegalArgumentException("item requires price");
                }
            }
            if (values.containsKey(InventEntry.COLUMN_QUANTITY)) {
                // Check that the weight is greater than or equal to 0 kg
                Integer quantity = values.getAsInteger(InventEntry.COLUMN_QUANTITY);
                if (quantity != null && quantity < 0) {
                    throw new IllegalArgumentException("item requires quantity");
                }
            }
        if (values.containsKey(InventEntry.COLUMN_IMAGE)) {
                    // Check that the weight is greater than or equal to 0 kg
                    String image = values.getAsString(InventEntry.COLUMN_IMAGE);
                    if (image != null ) {
                        throw new IllegalArgumentException("item requires image");
                    }
        }
            // If there are no values to update, then don't try to update the database
            if (values.size() == 0) {
                return 0;
            }


            // Otherwise, get writeable database to update the data
            SQLiteDatabase database = mDBHelper.getWritableDatabase();

            // Perform the update on the database and get the number of rows affected
            int rowsUpdated = database.update(InventEntry.TABLE_NAME, values, selection, selectionArgs);

            // If 1 or more rows were updated, then notify all listeners that the data at the given URI has changed
            if (rowsUpdated != 0) {
                getContext().getContentResolver().notifyChange(uri, null);
            }

            // Return the number of rows updated
            return rowsUpdated;
        }

    // delete data
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mDBHelper.getWritableDatabase();

        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENT:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InventEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVENT_ID:
                // Delete a single row given by the ID in the URI
                selection = InventEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(InventEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        // notifys listeners that table has changed if more than 1 row is deleted
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of rows deleted
        return rowsDeleted;
    }

    /**
     * Returns the MIME type of data for the content URI.
     */
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENT:
                return InventEntry.CONTENT_LIST_TYPE;
            case INVENT_ID:
                return InventEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
