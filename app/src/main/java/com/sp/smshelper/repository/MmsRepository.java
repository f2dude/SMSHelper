package com.sp.smshelper.repository;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;

import com.sp.smshelper.model.BaseModel;
import com.sp.smshelper.model.MmsConversation;
import com.sp.smshelper.model.MmsMessage;
import com.sp.smshelper.pdu_utils.ContentType;
import com.sp.smshelper.pdu_utils.MmsException;
import com.sp.smshelper.pdu_utils.PduHeaders;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

                    String mmsId = getValue(cursor, Telephony.Mms._ID);
                    boolean textOnly = false;
                    if (Integer.parseInt(getValue(cursor, Telephony.Mms.TEXT_ONLY)) == 1) {//For text
                        textOnly = true;
                        //get text
                        mmsConversation.setText(getMmsText(context, mmsId));
                    } else {//For everything else
                        mmsConversation.setDataList(getMmsData(context, mmsId));
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
     * Get all MMS messages using thread id
     *
     * @param context  Activity context
     * @param threadId MMS thread Id
     * @return MMS message list
     */
    public List<MmsMessage> getMmsMessagesByThreadId(Context context, String threadId) {
        Log.d(TAG, "getMmsMessagesByThreadId()");

        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = {Telephony.Mms.THREAD_ID,
                Telephony.Mms._ID,
                Telephony.Mms.DATE,
                Telephony.Mms.TEXT_ONLY,
                Telephony.Mms.MESSAGE_BOX};
        String selection = Telephony.Mms.THREAD_ID + " = ?";
        String[] selectionArgs = new String[]{threadId};
        Cursor cursor = contentResolver.query(Telephony.Mms.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                Telephony.Mms.DEFAULT_SORT_ORDER);

        List<MmsMessage> mmsMessageList = new ArrayList<>();
        try {
            if (null != cursor) {
                Log.d(TAG, "Cursor count: " + cursor.getCount());
                while (cursor.moveToNext()) {
                    MmsMessage mmsMessage = new MmsMessage();
                    mmsMessage.setThreadId(getValue(cursor, Telephony.Mms.THREAD_ID));
                    String mmsId = getValue(cursor, Telephony.Mms._ID);
                    mmsMessage.setMessageId(mmsId);
                    mmsMessage.setDate(getFormattedDate(Long.parseLong(getValue(cursor, Telephony.Mms.DATE)) * 1000));
                    boolean textOnly = false;
                    if (Integer.parseInt(getValue(cursor, Telephony.Mms.TEXT_ONLY)) == 1) {//For text
                        textOnly = true;
                        //get text
                        mmsMessage.setText(getMmsText(context, mmsId));
                    } else {//For everything else
                        mmsMessage.setDataList(getMmsData(context, mmsId));
                    }
                    mmsMessage.setTextOnly(textOnly);
                    mmsMessage.setAddressList(getMmsAddress(context, mmsId));

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
                    mmsMessage.setMessageBoxType(type);

                    mmsMessageList.add(mmsMessage);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getMmsMessagesByThreadId(): " + e);
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return mmsMessageList;
    }

    public MmsMessage getMmsMessageByMessageId(Context context, String messageId) {
        Log.d(TAG, "getMmsMessageByMessageId(), Message id: " + messageId);

        ContentResolver contentResolver = context.getContentResolver();
        String selection = Telephony.Mms._ID + " = ?";
        String[] selectionArgs = new String[]{messageId};
        Cursor cursor = contentResolver.query(Telephony.Mms.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                Telephony.Mms.DEFAULT_SORT_ORDER);
        MmsMessage mmsMessage = new MmsMessage();
        try {
            if (null != cursor) {
                Log.d(TAG, "getMmsMessageByMessageId(), Cursor count: " + cursor.getCount());
                while (cursor.moveToNext()) {
                    mmsMessage.setThreadId(getValue(cursor, Telephony.Mms.THREAD_ID));
                    String mmsId = getValue(cursor, Telephony.Mms._ID);
                    mmsMessage.setMessageId(mmsId);
                    mmsMessage.setDate(getFormattedDate(Long.parseLong(getValue(cursor, Telephony.Mms.DATE)) * 1000));
                    boolean textOnly = false;
                    if (Integer.parseInt(getValue(cursor, Telephony.Mms.TEXT_ONLY)) == 1) {//For text
                        textOnly = true;
                        //get text
                        mmsMessage.setText(getMmsText(context, mmsId));
                    } else {//For everything else
                        mmsMessage.setDataList(getMmsData(context, mmsId));
                    }
                    mmsMessage.setTextOnly(textOnly);
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
                    mmsMessage.setMessageBoxType(type);
                    mmsMessage.setMessageContentType(getValue(cursor, Telephony.Mms.CONTENT_TYPE));
                    mmsMessage.setDeliveryTime(getValue(cursor, Telephony.Mms.DELIVERY_TIME));
                    mmsMessage.setDateSent(getFormattedDate(Long.parseLong(getValue(cursor, Telephony.Mms.DATE_SENT)) * 1000));
                    mmsMessage.setContentClass(getValue(cursor, Telephony.Mms.CONTENT_CLASS));
                    mmsMessage.setContentLocation(getValue(cursor, Telephony.Mms.CONTENT_LOCATION));
                    mmsMessage.setCreator(getValue(cursor, Telephony.Mms.CREATOR));
                    mmsMessage.setDeliveryReport(getValue(cursor, Telephony.Mms.DELIVERY_REPORT));
                    String expiry = getValue(cursor, Telephony.Mms.EXPIRY);
                    if (!TextUtils.isEmpty(expiry)) {
                        mmsMessage.setExpiry(getFormattedDate(Long.parseLong(expiry)));
                    }

                    boolean locked = false;
                    if (Integer.parseInt(getValue(cursor, Telephony.Mms.LOCKED)) == 1) {
                        locked = true;
                    }
                    mmsMessage.setLocked(locked);

                    mmsMessage.setMessageClass(getValue(cursor, Telephony.Mms.MESSAGE_CLASS));
                    mmsMessage.setMmsMessageId(getValue(cursor, Telephony.Mms.MESSAGE_ID));
                    mmsMessage.setMessageSize(getValue(cursor, Telephony.Mms.MESSAGE_SIZE));
                    mmsMessage.setMessageType(getValue(cursor, Telephony.Mms.MESSAGE_TYPE));
                    mmsMessage.setMmsVersion(getValue(cursor, Telephony.Mms.MMS_VERSION));
                    mmsMessage.setPriority(getValue(cursor, Telephony.Mms.PRIORITY));

                    boolean read = false;
                    if (Integer.parseInt(getValue(cursor, Telephony.Mms.READ)) == 1) {
                        read = true;
                    }
                    mmsMessage.setRead(read);

                    boolean readReport = false;
                    String readReportValue = getValue(cursor, Telephony.Mms.READ_REPORT);
                    if (!TextUtils.isEmpty(readReportValue) && Integer.parseInt(readReportValue) == 1) {
                        readReport = true;
                    }
                    mmsMessage.setReadReport(readReport);

                    mmsMessage.setReadStatus(getValue(cursor, Telephony.Mms.READ_STATUS));

                    boolean reportAllowed = false;
                    String reportAllowedValue = getValue(cursor, Telephony.Mms.REPORT_ALLOWED);
                    if (!TextUtils.isEmpty(reportAllowedValue) && Integer.parseInt(reportAllowedValue) == 1) {
                        reportAllowed = true;
                    }
                    mmsMessage.setReportAllowed(reportAllowed);
                    mmsMessage.setResponseStatus(getValue(cursor, Telephony.Mms.RESPONSE_STATUS));
                    mmsMessage.setResponseText(getValue(cursor, Telephony.Mms.RESPONSE_TEXT));
                    mmsMessage.setRetrieveStatus(getValue(cursor, Telephony.Mms.RETRIEVE_STATUS));
                    mmsMessage.setRetrieveText(getValue(cursor, Telephony.Mms.RETRIEVE_TEXT));
                    mmsMessage.setRetrieveTextCharset(getValue(cursor, Telephony.Mms.RETRIEVE_TEXT_CHARSET));

                    boolean isSeen = false;
                    if (Integer.parseInt(getValue(cursor, Telephony.Mms.SEEN)) == 1) {
                        isSeen = true;
                    }
                    mmsMessage.setSeen(isSeen);

                    mmsMessage.setStatus(getValue(cursor, Telephony.Mms.STATUS));
                    mmsMessage.setSubject(getValue(cursor, Telephony.Mms.SUBJECT));
                    mmsMessage.setSubjectCharset(getValue(cursor, Telephony.Mms.SUBJECT_CHARSET));
                    mmsMessage.setSubscriptionId(getValue(cursor, Telephony.Mms.SUBSCRIPTION_ID));
                    mmsMessage.setTransactionId(getValue(cursor, Telephony.Mms.TRANSACTION_ID));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getMmsMessageByMessageId(): " + e);
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return mmsMessage;
    }

    /**
     * Returns MMS text associated with MMS message
     *
     * @param context Activity context
     * @param mmsId   MMS id
     * @return MMS text
     */
    private String getMmsText(Context context, String mmsId) {
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
        String[] projection = {Telephony.Mms.Addr.ADDRESS,
                Telephony.Mms.Addr.TYPE};
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
                        String type = getValue(cursor, Telephony.Mms.Addr.TYPE);
                        String header = null;
                        switch (Integer.parseInt(type)) {
                            case PduHeaders.TO:
                                header = "To";
                                break;
                            case PduHeaders.FROM:
                                header = "From";
                                break;
                            case PduHeaders.CC:
                                header = "CC";
                                break;
                            case PduHeaders.BCC:
                                header = "BCC";
                                break;
                            default:
                                break;
                        }
                        addressList.add(header + " : " + address);
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

    /**
     * Retrieves list of files associated with a message
     *
     * @param context Activity context
     * @param mmsId   MMS message Id
     * @return Files list
     */
    public List<BaseModel.Data> getMmsData(Context context, String mmsId) {
        List<BaseModel.Data> dataList = new ArrayList<>();
        String[] projection = {Telephony.Mms.Part.CONTENT_TYPE,
                Telephony.Mms.Part._ID,
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

                    BaseModel.Data data;
                    if (ContentType.isSupportedImageType(contentType)) {
                        data = new BaseModel().new Data();
                        data.setContentType(contentType);
                        data.setPartId(getValue(cursor, Telephony.Mms.Part._ID));

                        dataList.add(data);
                    } else if (ContentType.isTextType(contentType)) {
                        data = new BaseModel().new Data();
                        data.setContentType(contentType);
                        data.setText(getValue(cursor, Telephony.Mms.Part.TEXT));

                        dataList.add(data);
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
        return dataList;
    }

    /**
     * Extracts image using part id
     *
     * @param context Activity context
     * @param partId  Part id
     * @return Bitmap image
     */
    public Bitmap extractImage(Context context, String partId) {
        Bitmap bitmap = null;
        InputStream is = null;
        Uri.Builder builder = Telephony.Mms.CONTENT_URI.buildUpon();
        builder.appendPath("part")
                .appendPath(partId);
        try {
            is = context.getContentResolver().openInputStream(builder.build());
            bitmap = BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e(TAG, "IOException in extractImage(): " + e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(TAG, "IOException in finally of extractImage(): " + e);
                }
            }
        }
        return bitmap;
    }

    /**
     * Checks if the content location exists in MMS table
     *
     * @param context  Activity context
     * @param location Content location
     * @return True if it exists, False otherwise
     */
    public boolean isContentLocationExists(Context context, String location) {
        Log.d(TAG, "isContentLocationExists()");
        boolean exists = false;
        ContentResolver contentResolver = context.getContentResolver();
        String[] projection = new String[]{Telephony.Mms._ID};
        String selection = Telephony.Mms.CONTENT_LOCATION + " = ?";
        String[] selectionArgs = new String[]{location};
        Cursor cursor = contentResolver.query(Telephony.Mms.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                Telephony.Mms.DEFAULT_SORT_ORDER);
        if (null != cursor) {
            try {
                if (cursor.getCount() > 0) {
                    // We already received the same notification before.
                    cursor.close();
                    exists = true;
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception in isContentLocationExists(): " + e);
            } finally {
                cursor.close();
            }
        }
        return exists;
    }

    /**
     * Get the content location based on uri
     *
     * @param context Activity context
     * @param uri     MMS uri
     * @return content location
     * @throws MmsException
     */
    public String getContentLocation(Context context, Uri uri) throws MmsException {
        String[] projection = new String[]{
                Telephony.Mms.CONTENT_LOCATION
        };
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(uri, projection, null, null, null);

        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    String location = getValue(cursor, Telephony.Mms.CONTENT_LOCATION);
                    cursor.close();
                    return location;
                }
            } finally {
                cursor.close();
            }
        }
        throw new MmsException("Cannot get X-Mms-Content-Location from: " + uri);
    }

    /**
     * Query for a URI in MMS table
     *
     * @param context       Activity context
     * @param resolver      Content resolver
     * @param uri           MMS table uri
     * @param projection    Projected columns
     * @param selection     Selections
     * @param selectionArgs Selection values
     * @param sortOrder     Sort order
     * @return Cursor object
     */
    public Cursor query(Context context, ContentResolver resolver, Uri uri,
                        String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        try {
            return resolver.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (SQLiteException e) {
            Log.e(TAG, "Catch a SQLiteException when query: ", e);
            return null;
        }
    }

    /**
     * Inserts data in the table
     *
     * @param context  Activity Context
     * @param resolver Content resolver
     * @param uri      Table uri
     * @param values   Content values
     * @return Uri after successful insertion
     */
    public Uri insert(Context context, ContentResolver resolver,
                      Uri uri, ContentValues values) {
        try {
            return resolver.insert(uri, values);
        } catch (SQLiteException e) {
            Log.e(TAG, "Catch a SQLiteException when insert: ", e);
            return null;
        }
    }

    /**
     * Carries out update operation
     *
     * @param context       Activity context
     * @param resolver      Content resolver
     * @param uri           Table uri
     * @param values        Content values
     * @param where         Selection criteria
     * @param selectionArgs Selection values
     * @return Number of updated records
     */
    public int update(Context context, ContentResolver resolver, Uri uri,
                      ContentValues values, String where, String[] selectionArgs) {
        try {
            return resolver.update(uri, values, where, selectionArgs);
        } catch (SQLiteException e) {
            Log.e(TAG, "Catch a SQLiteException when update: ", e);
            return -1;
        }
    }

    /**
     * Carries out delete operation
     *
     * @param context       Activity context
     * @param resolver      Content resolver
     * @param uri           Table uri
     * @param where         Selection criteria
     * @param selectionArgs Selection values
     * @return Number of deleted records
     */
    public int delete(Context context, ContentResolver resolver, Uri uri,
                      String where, String[] selectionArgs) {
        try {
            return resolver.delete(uri, where, selectionArgs);
        } catch (SQLiteException e) {
            Log.e(TAG, "Catch a SQLiteException when delete: ", e);
            return -1;
        }
    }

    /**
     * Deletes Mms threads
     *
     * @param context   Activity context
     * @param threadIds List of thread ids
     * @return Content provider results
     */
    public ContentProviderResult[] deleteMmsThreads(Context context, List<String> threadIds) {
        Log.d(TAG, "deleteMmsThreads()");

        ContentProviderResult[] results = null;
        String selection = Telephony.Mms.THREAD_ID + " = ?";
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        try {
            for (String threadId : threadIds) {
                String[] selectionArgs = new String[]{threadId};
                ops.add(ContentProviderOperation.newDelete(Telephony.Mms.CONTENT_URI)
                        .withSelection(selection, selectionArgs)
                        .withYieldAllowed(true)
                        .build());
            }
            results = context.getContentResolver().applyBatch(Objects.requireNonNull(Telephony.Mms.CONTENT_URI.getAuthority()), ops);
        } catch (Exception e) {
            Log.e(TAG, "deleteMmsThreads(): " + e);
        }
        return results;
    }

    /**
     * Deletes mms messages
     *
     * @param context    Activity context
     * @param messageIds List of message ids
     * @return Content provider results
     */
    public ContentProviderResult[] deleteMmsMessages(Context context, List<String> messageIds) {
        Log.d(TAG, "deleteMmsMessages()");

        ContentProviderResult[] results = null;
        String selection = Telephony.Mms._ID + " = ?";
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        try {
            for (String messageId : messageIds) {
                String[] selectionArgs = new String[]{messageId};
                ops.add(ContentProviderOperation.newDelete(Telephony.Mms.CONTENT_URI)
                        .withSelection(selection, selectionArgs)
                        .withYieldAllowed(true)
                        .build());
            }
            results = context.getContentResolver().applyBatch(Objects.requireNonNull(Telephony.Mms.CONTENT_URI.getAuthority()), ops);
        } catch (Exception e) {
            Log.e(TAG, "deleteMmsMessages(): " + e);
        }
        return results;
    }

    /**
     * Marks all messages as read using thread id
     * Message of type INBOX and whose READ status is 0 are marked as read
     *
     * @param context  Activity context
     * @param threadId Thread id
     */
    public int markAllMessagesAsReadUsingThreadId(Context context, String threadId) {
        Log.d(TAG, "markMessagesAsReadUsingThreadId()");
        int rowsUpdated = 0;
        try {
            String where = Telephony.Mms.THREAD_ID + " = ? AND "
                    + Telephony.Mms.READ + " = ?";
            ContentValues contentValues = new ContentValues();
            contentValues.put(Telephony.Mms.READ, 1);
            contentValues.put(Telephony.Mms.SEEN, 1);

            rowsUpdated = context.getContentResolver().update(Telephony.Mms.CONTENT_URI,
                    contentValues,
                    where,
                    new String[]{threadId, "0"});

        } catch (Exception e) {
            Log.e(TAG, "markMessagesAsReadUsingThreadId: " + e);
        }
        return rowsUpdated;
    }
}
