package sed.inventorytracker.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import static android.text.style.TtsSpan.GENDER_FEMALE;
import static android.text.style.TtsSpan.GENDER_MALE;
import static sed.inventorytracker.data.InventContract.InventEntry.SHIPPED_FALSE;
import static sed.inventorytracker.data.InventContract.InventEntry.SHIPPED_PROCESSING;
import static sed.inventorytracker.data.InventContract.InventEntry.SHIPPED_TRUE;

/**
 * Created by Sed on 14/07/2017.
 */

public final class InventContract {
    // Prevents instantiating the contract class
    private InventContract() {
    }

    public static final String CONTENT_AUTHORITY = "sed.inventorytracker";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_INVENT = "inventory";

    // Defines rows for inventory table with item properties
    public static final class InventEntry implements BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENT);
        // MIME type for a list of items
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENT;

        // MIME type for a single item
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENT;

        // Table name
        public static final String TABLE_NAME = "inventory";
        // Table details
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ITEM_NAME = "item";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_SHIPPED = "shipped";
        public static final String COLUMN_SUPPLIER = "supplier";
      //  public static final String COLUMN_IMAGE = "image";
        // Different shipping status'
        public static final int SHIPPED_TRUE = 2;
        public static final int SHIPPED_FALSE = 0;
        public static final int SHIPPED_PROCESSING = 1;

        // Method used to confirm shipping status
        public static boolean isValidShipped(int shipped) {

            if (shipped == SHIPPED_FALSE || shipped == SHIPPED_PROCESSING || shipped == SHIPPED_TRUE) {
                return true;
            }
            return false;
        }

    }
}
