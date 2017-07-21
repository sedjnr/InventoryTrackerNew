package sed.inventorytracker;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import sed.inventorytracker.data.InventContract.InventEntry;

/**
 * Created by Sed on 17/07/2017.
 */

public class UpdateActivity  extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = UpdateActivity.class.getSimpleName();

    private static final int INVENT_LOADER = 1;
    // content uri for item
    private Uri mCurrentItemUri;
    // edit texts for each field
    private EditText mEditItem;
    private EditText mEditSupplier;
    private EditText mEditPrice;
    private TextView mEditQuantity;
    private Spinner mEditShipped;
    private ImageView mEditImage;

    // sets inital value of shipped spinner to false
    private int mShipped = InventEntry.SHIPPED_FALSE;
    // keeps track if item status has changed
    private boolean mItemHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        //Recieves data from intent in main activity
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        if (mCurrentItemUri == null) {
            setTitle("Insert Item");
            invalidateOptionsMenu();
        }else {
            setTitle("Edit Item");
            getLoaderManager().initLoader(INVENT_LOADER,null,this);
        }
        // Where users will get input from
        mEditItem = (EditText) findViewById(R.id.updateitem);
        mEditSupplier = (EditText) findViewById(R.id.updatesupplier);
        mEditPrice  = (EditText) findViewById(R.id.updateprice);
        mEditQuantity  = (TextView) findViewById(R.id.updatequantity);
        mEditShipped = (Spinner) findViewById(R.id.updateshipped);

        // sets ontouch listeners to see whether fields have been edited or not
        mEditItem.setOnTouchListener(mTouchListener);
        mEditSupplier.setOnTouchListener(mTouchListener);
        mEditPrice.setOnTouchListener(mTouchListener);
        mEditQuantity.setOnTouchListener(mTouchListener);
        mEditShipped.setOnTouchListener(mTouchListener);

        setupSpinner();

        Button emailButton = (Button) findViewById(R.id.emailstock);
        emailButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent mailIntent = new Intent(Intent.ACTION_SEND);
                mailIntent.setData(Uri.parse("mailto:"));
                mailIntent.setType("text/plain");
                // Defining supplier's email.
                mailIntent.putExtra(Intent.EXTRA_EMAIL, mEditSupplier.toString());
                mailIntent.putExtra(Intent.EXTRA_SUBJECT, mEditItem.toString());
                startActivity(Intent.createChooser(mailIntent, "Send Mail..."));
                if (mailIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mailIntent);
                }
            }
        });

    }

    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter shippedSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_shipped, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        shippedSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mEditShipped.setAdapter(shippedSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mEditShipped.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.True))) {
                        mShipped = InventEntry.SHIPPED_TRUE;
                    } else if (selection.equals(getString(R.string.False))) {
                        mShipped = InventEntry.SHIPPED_FALSE;
                    } else {
                        mShipped = InventEntry.SHIPPED_PROCESSING;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mShipped = InventEntry.SHIPPED_FALSE;
            }
        });
    }

    private void saveItem(){
        // Read user input
        String itemEntry = mEditItem.getText().toString().trim();
        String supplierEntry = mEditSupplier.getText().toString().trim();
        String priceEntry = mEditPrice.getText().toString().trim();
        String quantityEntry = mEditQuantity.getText().toString().trim();

        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(itemEntry) && TextUtils.isEmpty(supplierEntry) &&
                TextUtils.isEmpty(priceEntry) && TextUtils.isEmpty(quantityEntry)
                && mShipped == InventEntry.SHIPPED_FALSE) {
            return;
        }
        //ADD LINE FOR IMAGE

        // Create content values from user entries to corrosponding columns
        ContentValues values= new ContentValues();
        values.put(InventEntry.COLUMN_ITEM_NAME,itemEntry);
        values.put(InventEntry.COLUMN_SUPPLIER,supplierEntry);
        values.put(InventEntry.COLUMN_SHIPPED,mShipped );
        //ADD LINE FOR IMAGE

        int price = Integer.parseInt(priceEntry);
        values.put(InventEntry.COLUMN_PRICE,price);

        int quantity = Integer.parseInt(quantityEntry);
        values.put(InventEntry.COLUMN_QUANTITY,quantity);

        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(InventEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, "Couldnt add", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Item add", Toast.LENGTH_SHORT).show();
            }
        }else {
            // used if existing item is being edited and will update contentvalues then return them
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this,"update failed", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, "updated", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_insert, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveItem();
                // Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(UpdateActivity.this);
                    return true;
                    //  Otherwise if there are unsaved changes, setup a dialog to warn the user.
                    // Create a click listener to handle the user confirming that
                    // changes should be discarded.
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(UpdateActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be cancelled.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "cancel" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // projection that contains all columns editor has all columns
        String[] projection = {
                InventEntry._ID,
                InventEntry.COLUMN_ITEM_NAME,
                InventEntry.COLUMN_PRICE,
                InventEntry.COLUMN_SUPPLIER,
                InventEntry.COLUMN_QUANTITY,
                InventEntry.COLUMN_SHIPPED,
                InventEntry.COLUMN_IMAGE
        };

        // execute query method in background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentItemUri,         // queries content uri
                projection,             // defines colums to us
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // no sort order
    }

    @Override
    public void onLoadFinished (Loader<Cursor> loader, Cursor cursor) {
        // if cursor is null do not execute
        if (cursor == null) {
            return;
        }
        if ( (cursor.moveToFirst())) {
            int nameColumnIndex = cursor.getColumnIndex(InventEntry.COLUMN_ITEM_NAME);
            int supplierColumnIndex = cursor.getColumnIndex(InventEntry.COLUMN_SUPPLIER);
            int priceColumnIndex = cursor.getColumnIndex(InventEntry.COLUMN_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(InventEntry.COLUMN_QUANTITY);
            int shippedColumnIndex = cursor.getColumnIndex(InventEntry.COLUMN_SHIPPED);
            int imageColumnIndex = cursor.getColumnIndex(InventEntry.COLUMN_IMAGE);

            // gets data from cursor
            String name = cursor.getString(nameColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            final int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int shipped = cursor.getInt(shippedColumnIndex);
            String image = cursor.getString(imageColumnIndex);
            Button upQuantity = (Button) findViewById(R.id.increaseQuantity);
            Button downQuantity = (Button) findViewById(R.id.reduceQuantity);

            upQuantity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeQuantity(mCurrentItemUri, (quantity + 1));
                }
            });

            downQuantity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeQuantity(mCurrentItemUri, (quantity - 1));
                }
            });

            mEditItem.setText(name);
            mEditSupplier.setText(supplier);
            mEditPrice.setText(Integer.toString(price));
            mEditQuantity.setText(Integer.toString(quantity));
            // ADD Line for image

            Uri mUri = Uri.parse(image);
            mEditImage = (ImageView) findViewById(R.id.updateimage);
            mEditImage.setImageBitmap(getBitmapFromUri(mUri));
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        // int targetW = mEditImgg.getWidth();
        // int targetH = mEditImgg.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            // int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            // bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }
    private int changeQuantity( Uri uri, int mQuantity){


        ContentValues values = new ContentValues();
        values.put(InventEntry.COLUMN_QUANTITY, mQuantity);
        int numRowsUpdated = getContentResolver().update(uri, values, null, null);
        return numRowsUpdated;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mEditItem.setText("");
        mEditSupplier.setText("");
        mEditPrice.setText("");
        mEditQuantity.setText("");
        mEditShipped.setSelection(0);
        // Add image mEditImage.setImageResource(R.drawable.nocar);

    }
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("cancel and quit editing?");
        builder.setPositiveButton("cancel", discardButtonClickListener);
        builder.setNegativeButton("keep editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("delete item?");
        builder.setPositiveButton("delete?", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    private void deleteItem(){
        // only perform if existing pet
        if (mCurrentItemUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri,null,null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this,"error",  Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "successful",  Toast.LENGTH_SHORT).show();
            }
        }
        // Close the activity
        finish();
    }
}
