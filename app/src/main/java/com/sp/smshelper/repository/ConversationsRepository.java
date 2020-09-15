package com.sp.smshelper.repository;

import android.content.ContentResolver;
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
                    switch (Integer.parseInt(getValue(cursor, Telephony.Sms.READ))) {
                        case 0:
                            read = false;
                            break;
                        case 1:
                            read = true;
                            break;
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
                    switch (Integer.parseInt(getValue(cursor, Telephony.Sms.READ))) {
                        case 0:
                            read = false;
                            break;
                        case 1:
                            read = true;
                            break;
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
                    }
                    smsMessage.setStatus(status);
                    smsMessage.setReplyPathPresent(getValue(cursor, Telephony.Sms.REPLY_PATH_PRESENT));
                    smsMessage.setSubject(getValue(cursor, Telephony.Sms.SUBJECT));
                    smsMessage.setCreator(getValue(cursor, Telephony.Sms.CREATOR));
                    dateFormat = new Date(Long.parseLong(getValue(cursor, Telephony.Sms.DATE_SENT)));
                    smsMessage.setDateSent(dateFormat);
                    smsMessage.setErrorCode(getValue(cursor, Telephony.Sms.ERROR_CODE));
                    boolean locked = false;
                    switch (Integer.parseInt(getValue(cursor, Telephony.Sms.LOCKED))) {
                        case 0:
                            locked = false;
                            break;
                        case 1:
                            locked = true;
                            break;
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
                    switch (Integer.parseInt(getValue(cursor, Telephony.Sms.READ))) {
                        case 0:
                            read = false;
                            break;
                        case 1:
                            read = true;
                            break;
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
                    }
                    smsMessage.setStatus(status);
                    smsMessage.setReplyPathPresent(getValue(cursor, Telephony.Sms.REPLY_PATH_PRESENT));
                    smsMessage.setSubject(getValue(cursor, Telephony.Sms.SUBJECT));
                    smsMessage.setCreator(getValue(cursor, Telephony.Sms.CREATOR));
                    dateFormat = new Date(Long.parseLong(getValue(cursor, Telephony.Sms.DATE_SENT)));
                    smsMessage.setDateSent(dateFormat);
                    smsMessage.setErrorCode(getValue(cursor, Telephony.Sms.ERROR_CODE));
                    boolean locked = false;
                    switch (Integer.parseInt(getValue(cursor, Telephony.Sms.LOCKED))) {
                        case 0:
                            locked = false;
                            break;
                        case 1:
                            locked = true;
                            break;
                    }
                    smsMessage.setLocked(locked);
                    smsMessage.setPerson(getValue(cursor, Telephony.Sms.PERSON));
                    smsMessage.setSubscriptionId(getValue(cursor, Telephony.Sms.SUBSCRIPTION_ID));
                    smsMessage.setSeen(getValue(cursor, Telephony.Sms.SEEN));
                    ;
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

    private String getValue(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
    }
}
