package com.sp.smshelper.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;

public class MmsFileProvider extends ContentProvider {
    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Don't support queries.
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Don't support inserts.
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Don't support deletes.
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // Don't support updates.
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // For this sample, assume all files have no type.
        return null;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String fileMode) throws FileNotFoundException {
        File file = new File(getContext().getCacheDir(), uri.getPath());
        int mode = (TextUtils.equals(fileMode, "r") ? ParcelFileDescriptor.MODE_READ_ONLY :
                ParcelFileDescriptor.MODE_WRITE_ONLY
                        | ParcelFileDescriptor.MODE_TRUNCATE
                        | ParcelFileDescriptor.MODE_CREATE);
        return ParcelFileDescriptor.open(file, mode);
    }
}