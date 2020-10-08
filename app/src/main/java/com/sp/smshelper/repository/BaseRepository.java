package com.sp.smshelper.repository;

import android.database.Cursor;

import java.text.SimpleDateFormat;
import java.util.Locale;

class BaseRepository {

    /**
     * Gets value from column using cursor
     *
     * @param cursor     Cursor object
     * @param columnName Name of column
     * @return The actual value
     */
    String getValue(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
    }

    /**
     * Returns the formatted date
     *
     * @param time Date time in milliseconds
     * @return Formatted date string
     */
    String getFormattedDate(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy hh:mm a", Locale.getDefault());
        return sdf.format(time);
    }
}
