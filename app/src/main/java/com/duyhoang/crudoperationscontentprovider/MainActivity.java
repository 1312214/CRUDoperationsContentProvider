package com.duyhoang.crudoperationscontentprovider;

import android.Manifest;
import android.app.LoaderManager;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.OperationApplicationException;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends BaseActivity implements View.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static String TAG = MainActivity.class.getSimpleName();
    private static int MY_READ_CONTACT_PERMISSION_CODE = 20;
    private static int MY_WRITE_CONTACT_PERMISSION_CODE = 30;


    private Button btnAdd, btnUpdate, btnDelete, btnLoad, btnPhotoTag;
    private TextView txtContact;
    private EditText etInputContact;

    private CursorLoader cursorLoader;

    private String[] mColumnProjection = {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
            ContactsContract.Contacts.CONTACT_STATUS,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
    };

    private boolean firstLoaded = false;
    enum CRUDOperation {
            ADD_CONTACT, UPDATE_CONTACT, DELETE_CONTACT, LOAD_CONTACT
    };

    private CRUDOperation recentOpRun;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAdd = (Button)findViewById(R.id.button_add_contact);
        btnDelete = (Button)findViewById(R.id.button_delete_contact);
        btnUpdate = (Button)findViewById(R.id.button_update_contact);
        btnLoad = (Button)findViewById(R.id.button_load_contact);
        txtContact = (TextView)findViewById(R.id.text_contact);
        etInputContact = (EditText)findViewById(R.id.edit_contact_input);
        btnPhotoTag = (Button)findViewById(R.id.button_tag_photo);

        btnAdd.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnLoad.setOnClickListener(this);
        btnPhotoTag.setOnClickListener(this);


    }



    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.button_add_contact: addNewContact();
                break;
            case R.id.button_update_contact: updateAContact();
                break;
            case R.id.button_delete_contact: deleleContact();
                break;
            case R.id.button_load_contact: loadContact();
                break;
            case R.id.button_tag_photo: runPhotoTagFunct();
                break;

        }
    }

    private void runPhotoTagFunct() {
        startActivity(new Intent(this, PhotoTaggingActivity.class));
    }

    private void loadContact() {
        recentOpRun = CRUDOperation.LOAD_CONTACT;
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            if(!firstLoaded){
                getLoaderManager().initLoader(1, null, this);
                firstLoaded = true;
            }
            else{
                getLoaderManager().restartLoader(1, null , this);
            }
        }
        else{
            requestRuntimePermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, MY_READ_CONTACT_PERMISSION_CODE);
        }


    }

    private void deleleContact() {
        recentOpRun = CRUDOperation.DELETE_CONTACT;

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED)
        {
            String whereClause = ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY + " = '" + etInputContact.getText().toString() + "'";
            getContentResolver().delete(ContactsContract.RawContacts.CONTENT_URI, whereClause, null);
            etInputContact.setText("");
        }
        else{
            requestRuntimePermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, MY_WRITE_CONTACT_PERMISSION_CODE);
        }

    }

    private void updateAContact() {
        recentOpRun = CRUDOperation.UPDATE_CONTACT;

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED)
        {
            String[] strValue = etInputContact.getText().toString().split(" ");
            String[] selectionArgs;

            if(strValue.length  == 2){
                ContentValues cvs = new ContentValues();
                cvs.put(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY, strValue[1]);
                String where = ContactsContract.Contacts._ID + " = ?";
                selectionArgs = new String[]{strValue[0]};
                getContentResolver().update(ContactsContract.RawContacts.CONTENT_URI, cvs,where,selectionArgs);
                etInputContact.setText("");
            }
        }
        else{
            requestRuntimePermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, MY_WRITE_CONTACT_PERMISSION_CODE);
        }



    }

    private void addNewContact() {
        recentOpRun = CRUDOperation.ADD_CONTACT;

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            // have to check: the added contact have already exist

            ArrayList<ContentProviderOperation> cops = new ArrayList<>();
            cops.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,"accountname@gmail.com")
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, "com.google")
                    .build());

            cops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, etInputContact.getText().toString())
                    .build());
            try {
                getContentResolver().applyBatch(ContactsContract.AUTHORITY, cops);
            } catch (RemoteException e) {
                Log.e(  getLocalClassName(), e.getMessage());
            } catch (OperationApplicationException e) {
                Log.e( getLocalClassName(), e.getMessage());
            }
            etInputContact.setText("");
        }
        else
            requestRuntimePermissions(this, new String[]{Manifest.permission.WRITE_CONTACTS}, MY_WRITE_CONTACT_PERMISSION_CODE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return cursorLoader = new CursorLoader(this, ContactsContract.Contacts.CONTENT_URI, mColumnProjection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        StringBuilder strBuilder = new StringBuilder();
        if(cursor != null && cursor.getCount() > 0 ){
            try {
                while(cursor.moveToNext()){
                    strBuilder.append(cursor.getString(0 ) + " , " + cursor.getString(1) + " , "
                            + cursor.getString(2) + " , " + cursor.getString(3) + "\n");
                }
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
            txtContact.setText(strBuilder);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == MY_WRITE_CONTACT_PERMISSION_CODE || requestCode == MY_READ_CONTACT_PERMISSION_CODE && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            switch (recentOpRun){
                case LOAD_CONTACT:loadContact();break;
                case ADD_CONTACT: addNewContact();break;
                case DELETE_CONTACT:deleleContact();break;
                case UPDATE_CONTACT:updateAContact();break;
                default: break;
            }
        }
    }
}
