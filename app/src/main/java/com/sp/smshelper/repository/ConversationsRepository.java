package com.sp.smshelper.repository;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.Telephony;
import android.util.Log;

import com.sp.smshelper.model.Conversation;
import com.sp.smshelper.model.SmsMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ConversationsRepository {

    private static final String TAG = ConversationsRepository.class.getSimpleName();

    /**
     * Returns all the conversations
     *
     * @param context Activity context
     * @return Conversation list
     */
    /*public List<Conversation> getAllConversations(Context context) {
        Log.d(TAG, "getAllConversations()");

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(Telephony.Sms.Conversations.CONTENT_URI,
                null,
                null,
                null,
                Telephony.Sms.Conversations.DEFAULT_SORT_ORDER);
        List<Conversation> conversationList = new ArrayList<>();
        try {
            if (null != cursor) {
                Log.d(TAG, "Cursor count: " + cursor.getCount());
                while (cursor.moveToNext()) {
                    Conversation conversation = new Conversation();

                    conversation.setThreadId(getValue(cursor, Telephony.Sms.Conversations.THREAD_ID));
                    conversation.setSnippet(getValue(cursor, Telephony.Sms.Conversations.SNIPPET));
                    conversation.setTotalMessageCount(getValue(cursor, Telephony.Sms.Conversations.MESSAGE_COUNT));

                    conversationList.add(conversation);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getAllConversations(): " + e);
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return conversationList;
    }*/
    public List<Conversation> getAllConversations(Context context) {
        Log.d(TAG, "getAllConversations()");
        String[] projection = {"DISTINCT " + Telephony.Sms.THREAD_ID,
                Telephony.Sms.BODY,
                Telephony.Sms.DATE,
                Telephony.Sms.ADDRESS,
                Telephony.Sms.READ};
        String selection = Telephony.Sms.THREAD_ID + " IS NOT NULL) GROUP BY (" + Telephony.Sms.THREAD_ID;
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(Telephony.Sms.CONTENT_URI,
                projection,
                selection,
                null,
                Telephony.Sms.DEFAULT_SORT_ORDER);
        List<Conversation> conversationList = new ArrayList<>();
        try {
            if (null != cursor) {
                Log.d(TAG, "getAllConversations(), Cursor count: " + cursor.getCount());
                while (cursor.moveToNext()) {
                    Conversation conversation = new Conversation();
                    conversation.setThreadId(getValue(cursor, Telephony.Sms.THREAD_ID));
                    conversation.setSnippet(getValue(cursor, Telephony.Sms.BODY));

                    String smsDate = getValue(cursor, Telephony.Sms.DATE);
                    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
                    conversation.setDate(sdf.format(Long.parseLong(smsDate)));
                    conversation.setAddress(getValue(cursor, Telephony.Sms.ADDRESS));
                    boolean read = false;
                    if (Integer.parseInt(getValue(cursor, Telephony.Sms.READ)) == 1) {
                        read = true;
                    }
                    conversation.setRead(read);

                    conversationList.add(conversation);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getAllConversations(): " + e);
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return conversationList;
    }

    /**
     * Returns all the messages associated by thread id
     *
     * @param context  Activity context
     * @param threadId Thread id
     * @return Sms messages list
     */
    public List<SmsMessage> getSmsMessagesByThreadId(Context context, String threadId) {
        Log.d(TAG, "getSmsMessagesByThreadId(), Thread id: " + threadId);

        ContentResolver contentResolver = context.getContentResolver();
        String selection = Telephony.Sms.THREAD_ID + " = ?";
        String[] selectionArgs = new String[]{threadId};
        Cursor cursor = contentResolver.query(Telephony.Sms.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                Telephony.Sms.DEFAULT_SORT_ORDER);
        List<SmsMessage> smsMessageList = new ArrayList<>();
        try {
            if (null != cursor) {
                Log.d(TAG, "Cursor count: " + cursor.getCount());
                while (cursor.moveToNext()) {
                    SmsMessage smsMessage = new SmsMessage();

                    smsMessage.setThreadId(getValue(cursor, Telephony.Sms.THREAD_ID));
                    smsMessage.setMessageId(getValue(cursor, Telephony.Sms._ID));
                    smsMessage.setAddress(getValue(cursor, Telephony.Sms.ADDRESS));
                    smsMessage.setBody(getValue(cursor, Telephony.Sms.BODY));

                    String smsDate = getValue(cursor, Telephony.Sms.DATE);
                    Date dateFormat = new Date(Long.parseLong(smsDate));
                    smsMessage.setDate(dateFormat);

                    SmsMessage.MessageType type = null;
                    switch (Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                        case Telephony.Sms.MESSAGE_TYPE_ALL:
                            type = SmsMessage.MessageType.ALL;
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_DRAFT:
                            type = SmsMessage.MessageType.DRAFT;
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_FAILED:
                            type = SmsMessage.MessageType.FAILED;
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_INBOX:
                            type = SmsMessage.MessageType.INBOX;
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                            type = SmsMessage.MessageType.OUTBOX;
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_QUEUED:
                            type = SmsMessage.MessageType.QUEUED;
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_SENT:
                            type = SmsMessage.MessageType.SENT;
                            break;
                        default:
                            break;
                    }
                    smsMessage.setType(type);
                    smsMessage.setProtocol(getValue(cursor, Telephony.Sms.PROTOCOL));
                    boolean read = false;
                    if (Integer.parseInt(getValue(cursor, Telephony.Sms.READ)) == 1) {
                        read = true;
                    }
                    smsMessage.setRead(read);
                    SmsMessage.MessageStatus status = null;
                    switch (Integer.parseInt(getValue(cursor, Telephony.Sms.STATUS))) {
                        case Telephony.Sms.STATUS_COMPLETE:
                            status = SmsMessage.MessageStatus.COMPLETE;
                            break;
                        case Telephony.Sms.STATUS_FAILED:
                            status = SmsMessage.MessageStatus.FAILED;
                            break;
                        case Telephony.Sms.STATUS_NONE:
                            status = SmsMessage.MessageStatus.NONE;
                            break;
                        case Telephony.Sms.STATUS_PENDING:
                            status = SmsMessage.MessageStatus.PENDING;
                            break;
                        default:
                            break;
                    }
                    smsMessage.setStatus(status);
                    smsMessage.setReplyPathPresent(getValue(cursor, Telephony.Sms.REPLY_PATH_PRESENT));
                    smsMessage.setSubject(getValue(cursor, Telephony.Sms.SUBJECT));
                    smsMessage.setCreator(getValue(cursor, Telephony.Sms.CREATOR));
                    dateFormat = new Date(Long.parseLong(getValue(cursor, Telephony.Sms.DATE_SENT)));
                    smsMessage.setDateSent(dateFormat);
                    smsMessage.setErrorCode(getValue(cursor, Telephony.Sms.ERROR_CODE));
                    boolean locked = false;
                    if (Integer.parseInt(getValue(cursor, Telephony.Sms.LOCKED)) == 1) {
                        locked = true;
                    }
                    smsMessage.setLocked(locked);
                    smsMessage.setPerson(getValue(cursor, Telephony.Sms.PERSON));

                    smsMessageList.add(smsMessage);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getSmsMessagesByThreadId(): " + e);
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return smsMessageList;
    }

    /**
     * Returns a messages object based on message if
     *
     * @param context   Activity context
     * @param messageId Message id
     * @return Sms message object
     */
    public SmsMessage getSmsMessageByMessageId(Context context, String messageId) {
        Log.d(TAG, "getSmsMessageByMessageId(), Message id: " + messageId);

        ContentResolver contentResolver = context.getContentResolver();
        String selection = Telephony.Sms._ID + " = ?";
        String[] selectionArgs = new String[]{messageId};
        Cursor cursor = contentResolver.query(Telephony.Sms.CONTENT_URI,
                null,
                selection,
                selectionArgs,
                Telephony.Sms.DEFAULT_SORT_ORDER);
        SmsMessage smsMessage = new SmsMessage();
        try {
            if (null != cursor) {
                Log.d(TAG, "Cursor count: " + cursor.getCount());
                if (cursor.moveToFirst()) {

                    smsMessage.setThreadId(getValue(cursor, Telephony.Sms.THREAD_ID));
                    smsMessage.setMessageId(getValue(cursor, Telephony.Sms._ID));
                    smsMessage.setAddress(getValue(cursor, Telephony.Sms.ADDRESS));
                    smsMessage.setBody(getValue(cursor, Telephony.Sms.BODY));

                    String smsDate = getValue(cursor, Telephony.Sms.DATE);
                    Date dateFormat = new Date(Long.parseLong(smsDate));
                    smsMessage.setDate(dateFormat);

                    SmsMessage.MessageType type = null;
                    switch (Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Sms.TYPE)))) {
                        case Telephony.Sms.MESSAGE_TYPE_ALL:
                            type = SmsMessage.MessageType.ALL;
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_DRAFT:
                            type = SmsMessage.MessageType.DRAFT;
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_FAILED:
                            type = SmsMessage.MessageType.FAILED;
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_INBOX:
                            type = SmsMessage.MessageType.INBOX;
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_OUTBOX:
                            type = SmsMessage.MessageType.OUTBOX;
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_QUEUED:
                            type = SmsMessage.MessageType.QUEUED;
                            break;
                        case Telephony.Sms.MESSAGE_TYPE_SENT:
                            type = SmsMessage.MessageType.SENT;
                            break;
                        default:
                            break;
                    }
                    smsMessage.setType(type);
                    smsMessage.setProtocol(getValue(cursor, Telephony.Sms.PROTOCOL));
                    boolean read = false;
                    if (Integer.parseInt(getValue(cursor, Telephony.Sms.READ)) == 1) {
                        read = true;
                    }
                    smsMessage.setRead(read);
                    SmsMessage.MessageStatus status = null;
                    switch (Integer.parseInt(getValue(cursor, Telephony.Sms.STATUS))) {
                        case Telephony.Sms.STATUS_COMPLETE:
                            status = SmsMessage.MessageStatus.COMPLETE;
                            break;
                        case Telephony.Sms.STATUS_FAILED:
                            status = SmsMessage.MessageStatus.FAILED;
                            break;
                        case Telephony.Sms.STATUS_NONE:
                            status = SmsMessage.MessageStatus.NONE;
                            break;
                        case Telephony.Sms.STATUS_PENDING:
                            status = SmsMessage.MessageStatus.PENDING;
                            break;
                        default:
                            break;
                    }
                    smsMessage.setStatus(status);
                    smsMessage.setReplyPathPresent(getValue(cursor, Telephony.Sms.REPLY_PATH_PRESENT));
                    smsMessage.setSubject(getValue(cursor, Telephony.Sms.SUBJECT));
                    smsMessage.setCreator(getValue(cursor, Telephony.Sms.CREATOR));
                    dateFormat = new Date(Long.parseLong(getValue(cursor, Telephony.Sms.DATE_SENT)));
                    smsMessage.setDateSent(dateFormat);
                    smsMessage.setErrorCode(getValue(cursor, Telephony.Sms.ERROR_CODE));
                    boolean locked = false;
                    if (Integer.parseInt(getValue(cursor, Telephony.Sms.LOCKED)) == 1) {
                        locked = true;
                    }
                    smsMessage.setLocked(locked);
                    smsMessage.setPerson(getValue(cursor, Telephony.Sms.PERSON));
                    smsMessage.setSubscriptionId(getValue(cursor, Telephony.Sms.SUBSCRIPTION_ID));
                    smsMessage.setSeen(getValue(cursor, Telephony.Sms.SEEN));
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in getSmsMessageByMessageId(): " + e);
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return smsMessage;
    }

    /**
     * Gets value from column using cursor
     *
     * @param cursor     Cursor object
     * @param columnName Name of column
     * @return The actual value
     */
    private String getValue(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
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
            String where = Telephony.Sms.THREAD_ID + " = ? AND "
                    + Telephony.Sms.TYPE + " = ? AND "
                    + Telephony.Sms.READ + " = ?";
            ContentValues contentValues = new ContentValues();
            contentValues.put(Telephony.Sms.READ, 1);

            rowsUpdated = context.getContentResolver().update(Telephony.Sms.CONTENT_URI,
                    contentValues,
                    where,
                    new String[]{threadId, String.valueOf(Telephony.Sms.MESSAGE_TYPE_INBOX), "0"});

        } catch (Exception e) {
            Log.e(TAG, "markMessagesAsReadUsingThreadId: " + e);
        }
        return rowsUpdated;
    }

    /**
     * Marks a message as read
     *
     * @param context   Activity context
     * @param messageId Message id
     * @return Should return 1 as single row is updated
     */
    public int markMessageAsRead(Context context, String messageId) {
        Log.d(TAG, "markMessageAsRead()");
        int rowsUpdated = 0;
        try {
            String where = Telephony.Sms._ID + " = ? AND "
                    + Telephony.Sms.TYPE + " = ? AND "
                    + Telephony.Sms.READ + " = ?";
            ContentValues contentValues = new ContentValues();
            contentValues.put(Telephony.Sms.READ, 1);

            rowsUpdated = context.getContentResolver().update(Telephony.Sms.CONTENT_URI,
                    contentValues,
                    where,
                    new String[]{messageId, String.valueOf(Telephony.Sms.MESSAGE_TYPE_INBOX), "0"});

        } catch (Exception e) {
            Log.e(TAG, "markMessageAsRead: " + e);
        }
        return rowsUpdated;
    }

    /**
     * Deletes sms threads
     * @param context Activity context
     * @param threadIds List of thread ids
     * @return Content provider results
     */
    public ContentProviderResult[] deleteSmsThreads(Context context, List<String> threadIds) {
        Log.d(TAG, "deleteSmsThreads()");

        ContentProviderResult[] results = null;
        String selection = Telephony.Sms.THREAD_ID + " = ?";
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        try {
            for (String threadId : threadIds) {
                String[] selectionArgs = new String[]{threadId};
                ops.add(ContentProviderOperation.newDelete(Telephony.Sms.CONTENT_URI)
                        .withSelection(selection, selectionArgs)
                        .withYieldAllowed(true)
                        .build());
            }
            results = context.getContentResolver().applyBatch(Objects.requireNonNull(Telephony.Sms.CONTENT_URI.getAuthority()), ops);
        } catch (Exception e) {
            Log.e(TAG, "deleteSmsThreads(): " + e);
        }
        return results;
    }

    /**
     * Deletes sms messages
     * @param context Activity context
     * @param messageIds List of message ids
     * @return Content provider results
     */
    public ContentProviderResult[] deleteSmsMessages(Context context, List<String> messageIds) {
        Log.d(TAG, "deleteSmsThreads()");

        ContentProviderResult[] results = null;
        String selection = Telephony.Sms._ID + " = ?";
        ArrayList<ContentProviderOperation> ops = new ArrayList<>();
        try {
            for (String messageId : messageIds) {
                String[] selectionArgs = new String[]{messageId};
                ops.add(ContentProviderOperation.newDelete(Telephony.Sms.CONTENT_URI)
                        .withSelection(selection, selectionArgs)
                        .withYieldAllowed(true)
                        .build());
            }
            results = context.getContentResolver().applyBatch(Objects.requireNonNull(Telephony.Sms.CONTENT_URI.getAuthority()), ops);
        } catch (Exception e) {
            Log.e(TAG, "deleteSmsMessages(): " + e);
        }
        return results;
    }
}
