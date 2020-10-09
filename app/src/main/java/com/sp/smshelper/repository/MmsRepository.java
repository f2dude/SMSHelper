package com.sp.smshelper.repository;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.util.Log;

import com.sp.smshelper.model.MmsConversation;

import java.util.ArrayList;
import java.util.List;

public class MmsRepository extends BaseRepository {

    private static final String TAG = MmsRepository.class.getSimpleName();

    /**
     * Returns all MMS conversations
     *
     * @param context Activity context
     */
    public List<MmsConversation> getAllMmsConversations(Context context) {
        Log.d(TAG, "getAllMmsConversations()");

        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = {"DISTINCT " + Telephony.Mms.THREAD_ID,
                Telephony.Mms._ID,
                "max(" + Telephony.Mms.DATE + ")",
                Telephony.Mms.DATE,
                Telephony.Mms.READ,
                Telephony.Mms.CONTENT_TYPE,
                Telephony.Mms.TEXT_ONLY,
                Telephony.Mms.MESSAGE_BOX};
        String selection = Telephony.Mms.THREAD_ID + " IS NOT NULL) GROUP BY (" + Telephony.Mms.THREAD_ID;
        Cursor cursor = contentResolver.query(Telephony.Mms.CONTENT_URI,
                projection,
                selection,
                null,
                Telephony.Mms.DEFAULT_SORT_ORDER);
        List<MmsConversation> mmsConversationList = new ArrayList<>();
        try {
            if (null != cursor) {
                Log.d(TAG, "getAllMmsConversations(), cursor count: " + cursor.getCount());
                while (cursor.moveToNext()) {
                    MmsConversation mmsConversation = new MmsConversation();
                    mmsConversation.setThreadId(getValue(cursor, Telephony.Mms.THREAD_ID));
                    //Bug in MMS date column.That's why had to multiply by 1000
                    mmsConversation.setDate(getFormattedDate(Long.parseLong(getValue(cursor, Telephony.Mms.DATE)) * 1000));
                    boolean read = false;
                    if (Integer.parseInt(getValue(cursor, Telephony.Mms.READ)) == 1) {
                        read = true;
                    }
                    mmsConversation.setRead(read);
                    String mmsId = getValue(cursor, Telephony.Mms._ID);
                    boolean textOnly = false;
                    if (Integer.parseInt(getValue(cursor, Telephony.Mms.TEXT_ONLY)) == 1) {//For text
                        textOnly = true;
                        //get text
                        mmsConversation.setText(getMmsText(context, mmsId, mmsConversation));
                    } else {//For everything else
                        mmsConversation.setData(getMmsData(context, mmsId, mmsConversation));
                    }
                    mmsConversation.setTextOnly(textOnly);
                    mmsConversation.setAddressList(getMmsAddress(context, mmsId));

                    MmsConversation.MessageType type = null;
                    switch (Integer.parseInt(getValue(cursor, Telephony.Mms.MESSAGE_BOX))) {
                        case Telephony.Mms.MESSAGE_BOX_ALL:
                            type = MmsConversation.MessageType.ALL;
                            break;
                        case Telephony.Mms.MESSAGE_BOX_DRAFTS:
                            type = MmsConversation.MessageType.DRAFT;
                            break;
                        case Telephony.Mms.MESSAGE_BOX_FAILED:
                            type = MmsConversation.MessageType.FAILED;
                            break;
                        case Telephony.Mms.MESSAGE_BOX_INBOX:
                            type = MmsConversation.MessageType.INBOX;
                            break;
                        case Telephony.Mms.MESSAGE_BOX_OUTBOX:
                            type = MmsConversation.MessageType.OUTBOX;
                            break;
                        case Telephony.Mms.MESSAGE_BOX_SENT:
                            type = MmsConversation.MessageType.SENT;
                            break;
                        default:
                            break;
                    }
                    mmsConversation.setMessageBoxType(type);

                    mmsConversationList.add(mmsConversation);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getAllMmsConversations(): " + e);
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return mmsConversationList;
    }

    /**
     * Returns MMS text associated with MMS message
     *
     * @param context Activity context
     * @param mmsId   MMS id
     * @return MMS text
     */
    private String getMmsText(Context context, String mmsId, MmsConversation mmsConversation) {
        StringBuilder sb = new StringBuilder();
        String[] projection = {Telephony.Mms.Part.CONTENT_TYPE,
                Telephony.Mms.Part.TEXT};
        ContentResolver contentResolver = context.getContentResolver();
        String selection = Telephony.Mms.Part.MSG_ID + " = ?";
        String[] selectionArgs = new String[]{mmsId};
        Uri uri = Uri.withAppendedPath(Telephony.Mms.CONTENT_URI, "part");
        Cursor cursor = contentResolver.query(uri,
                projection,
                selection,
                selectionArgs,
                null);
        try {
            if (null != cursor) {
                while (cursor.moveToNext()) {
                    String contentType = getValue(cursor, Telephony.Mms.Part.CONTENT_TYPE);
                    if (contentType.equals("text/plain")) {
                        mmsConversation.setContentType(contentType);

                        String text = getValue(cursor, Telephony.Mms.Part.TEXT);
                        sb.append(text);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getMmsText(): " + e);
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return sb.toString();
    }

    /**
     * Retrieves MMS address list for a givem MMS message
     *
     * @param context Activity context
     * @param mmsId   MMS Id
     * @return Address list
     */
    private List<String> getMmsAddress(Context context, String mmsId) {
        List<String> addressList = new ArrayList<>();
        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = {Telephony.Mms.Addr.ADDRESS};
        String selection = Telephony.Mms.Addr.MSG_ID + " = ?";
        String[] selectionArgs = new String[]{mmsId};

        Uri.Builder builder = Telephony.Mms.CONTENT_URI.buildUpon();
        builder.appendPath(mmsId)
                .appendPath("addr");
        Cursor cursor = contentResolver.query(builder.build(),
                projection,
                selection,
                selectionArgs,
                null);
        try {
            if (null != cursor) {
                while (cursor.moveToNext()) {
                    String address = getValue(cursor, Telephony.Mms.Addr.ADDRESS);
                    if (!address.equals("insert-address-token")) {
                        addressList.add(address);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getMmsAddress(): " + e);
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return addressList;
    }

    private MmsConversation.Data getMmsData(Context context, String mmsId, MmsConversation mmsConversation) {
        MmsConversation.Data data = null;
        String[] projection = {Telephony.Mms.Part.CONTENT_TYPE,
                Telephony.Mms.Part._DATA};
        ContentResolver contentResolver = context.getContentResolver();
        String selection = Telephony.Mms.Part.MSG_ID + " = ?";
        String[] selectionArgs = new String[]{mmsId};
        Uri uri = Uri.withAppendedPath(Telephony.Mms.CONTENT_URI, "part");
        Cursor cursor = contentResolver.query(uri,
                projection,
                selection,
                selectionArgs,
                null);
        try {
            if (null != cursor) {
                while (cursor.moveToNext()) {
                    String contentType = getValue(cursor, Telephony.Mms.Part.CONTENT_TYPE);
                    if (!contentType.equals("application/smil")) {
                        mmsConversation.setContentType(contentType);

                        data = new MmsConversation().new Data();
                        data.setContentType(contentType);
                        data.setDataPath(getValue(cursor, Telephony.Mms.Part._DATA));
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getMmsData(): " + e);
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return data;
    }
}
