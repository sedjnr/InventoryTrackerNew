package sed.inventorytracker.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import sed.inventorytracker.data.InventContract.InventEntry;

/**
 * Created by Sed on 14/07/2017.
 */

public class InventDBHelper extends SQLiteOpenHelper {
    public static final String LOG_TAG = InventDBHelper.class.getSimpleName();

   // Database name and version
    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;


    public InventDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creates database
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the pets table
        String SQL_CREATE_ITEM_TABLE =  "CREATE TABLE " + InventEntry.TABLE_NAME + " ("
                + InventEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + InventEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + InventEntry.COLUMN_SUPPLIER + " TEXT, "
                + InventEntry.COLUMN_SHIPPED + " INTEGER NOT NULL, "
                + InventEntry.COLUMN_PRICE + " INTEGER NOT NULL, "
                + InventEntry.COLUMN_QUANTITY + " INTEGER NOT NULL DEFAULT 0"
               // + InventEntry.COLUMN_IMAGE + "TEXT" +
                + ");";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_ITEM_TABLE);
    }

    // only used when databsas needs to be updated
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}
