package com.sp.smshelper.readmms;

import android.content.ContentProviderResult;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sp.smshelper.mmsmessages.MmsMessagesFragment;
import com.sp.smshelper.model.BaseModel;
import com.sp.smshelper.model.MmsConversation;
import com.sp.smshelper.model.MmsMessage;
import com.sp.smshelper.repository.ConversationsObserver;
import com.sp.smshelper.repository.MmsRepository;

import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MmsViewModel extends ViewModel {

    private static final String TAG = MmsViewModel.class.getSimpleName();

    private Context mContext;

    private MutableLiveData<List<MmsConversation>> mMutableMmsConversations = new MutableLiveData<>();
    private MutableLiveData<List<MmsMessage>> mMutableMmsMessages = new MutableLiveData<>();
    private MutableLiveData<String> mMutableMms = new MutableLiveData<>();
    private MutableLiveData<List<BaseModel.Data>> mMutableMmsData = new MutableLiveData<>();
    private ConversationsObserver mConversationsObserver;
    private Fragment mActiveFragment;

    public void setContext(Context context) {
        this.mContext = context;
    }

    /**
     * Returns all MMS conversations
     *
     * @return MMS conversation list
     */
    Disposable getAllMmsConversations() {

        return Single.fromCallable(() -> {
            MmsRepository mmsRepository = new MmsRepository();
            return mmsRepository.getAllMmsConversations(mContext);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultObject ->
                                //Set data
                                mMutableMmsConversations.setValue(resultObject),
                        error -> Log.e(TAG, "Error in getAllMmsConversations(): " + error));
    }

    /**
     * Returns MMS conversations
     * Method used to provide live updates on data
     *
     * @return MMS conversation list
     */
    LiveData<List<MmsConversation>> watchMmsConversations() {
        return mMutableMmsConversations;
    }

    /**
     * Returns list of MMS messages mapped based on thread id
     *
     * @param threadId Thread Id
     * @return Disposable object
     */
    public Disposable getMmsMessagesByThreadId(String threadId) {
        return Single.fromCallable(() -> {
            MmsRepository mmsRepository = new MmsRepository();
            return mmsRepository.getMmsMessagesByThreadId(mContext, threadId);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultObject ->
                                //set data
                                mMutableMmsMessages.setValue(resultObject),
                        error -> Log.e(TAG, "Error in getMmsMessagesByThreadId(): " + error));
    }

    /**
     * Returns MMS messages
     * Method used to provide live updates on data
     *
     * @return MMS message list
     */
    public LiveData<List<MmsMessage>> watchMmsMessages() {
        return mMutableMmsMessages;
    }

    /**
     * Fetches MMS message using message id
     *
     * @param messageId MMS message id
     * @return Disposable object
     */
    public Disposable getMmsMessageByMessageId(String messageId) {
        return Single.fromCallable(() -> {
            MmsRepository mmsRepository = new MmsRepository();
            return mmsRepository.getMmsMessageByMessageId(mContext, messageId);
        })
                .flatMap((Function<MmsMessage, SingleSource<String>>) this::generateMmsMessage)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultObject ->
                                //set data
                                mMutableMms.setValue(resultObject),
                        error -> Log.e(TAG, "Error in getMmsMessagesByThreadId(): " + error));
    }

    /**
     * Returns MMS message object
     * Method used to provide live updates on data
     *
     * @return MMS message object
     */
    public LiveData<String> watchMmsMessageDetails() {
        return mMutableMms;
    }

    /**
     * Generates MMS message for displaying on screen
     *
     * @param mmsMessage MMS message object
     * @return String format of MMS message
     */
    private Single<String> generateMmsMessage(MmsMessage mmsMessage) {
        return Single.fromCallable(() -> {
            String newLine = "\n";
            StringBuilder sb = new StringBuilder();
            sb.append("Thread id: " + mmsMessage.getThreadId());
            sb.append(newLine);
            sb.append("Message id: " + mmsMessage.getMessageId());
            sb.append(newLine);
            sb.append("Date: " + mmsMessage.getDate());
            sb.append(newLine);
            sb.append("Text only: " + mmsMessage.isTextOnly());
            sb.append(newLine);
            sb.append("Text: " + mmsMessage.getText());
            sb.append(newLine);
            if (!mmsMessage.isTextOnly()) {
                sb.append("No. of files: " + mmsMessage.getDataList().size());
                sb.append(newLine);
            }
            sb.append("Message box type: " + mmsMessage.getMessageBoxType().name());
            sb.append(newLine);
            sb.append("MMS content type: " + mmsMessage.getMessageContentType());
            sb.append(newLine);
            sb.append("Delivery time: " + mmsMessage.getDeliveryTime());
            sb.append(newLine);
            sb.append("Date sent: " + mmsMessage.getDateSent());
            sb.append(newLine);
            sb.append("Content class: " + mmsMessage.getContentClass());
            sb.append(newLine);
            sb.append("Content location: " + mmsMessage.getContentLocation());
            sb.append(newLine);
            sb.append("Creator: " + mmsMessage.getCreator());
            sb.append(newLine);
            sb.append("Delivery report: " + mmsMessage.getDeliveryReport());
            sb.append(newLine);
            sb.append("Expiry: " + mmsMessage.getExpiry());
            sb.append(newLine);
            sb.append("Locked: " + mmsMessage.getLocked());
            sb.append(newLine);
            sb.append("Message class: " + mmsMessage.getMessageClass());
            sb.append(newLine);
            sb.append("MMS message id: " + mmsMessage.getMmsMessageId());
            sb.append(newLine);
            sb.append("Message size: " + mmsMessage.getMessageSize());
            sb.append(newLine);
            sb.append("Message type: " + mmsMessage.getMessageType());
            sb.append(newLine);
            sb.append("MMS version: " + mmsMessage.getMmsVersion());
            sb.append(newLine);
            sb.append("Priority: " + mmsMessage.getPriority());
            sb.append(newLine);
            sb.append("Read: " + mmsMessage.isRead());
            sb.append(newLine);
            sb.append("Read report: " + mmsMessage.isReadReport());
            sb.append(newLine);
            sb.append("Read status: " + mmsMessage.getReadStatus());
            sb.append(newLine);
            sb.append("Report allowed: " + mmsMessage.isReportAllowed());
            sb.append(newLine);
            sb.append("Response status: " + mmsMessage.getResponseStatus());
            sb.append(newLine);
            sb.append("Response text: " + mmsMessage.getResponseText());
            sb.append(newLine);
            sb.append("Retrieve status: " + mmsMessage.getRetrieveStatus());
            sb.append(newLine);
            sb.append("Retrieve text: " + mmsMessage.getRetrieveText());
            sb.append(newLine);
            sb.append("Retrieve text charset: " + mmsMessage.getRetrieveTextCharset());
            sb.append(newLine);
            sb.append("Seen: " + mmsMessage.isSeen());
            sb.append(newLine);
            sb.append("Status: " + mmsMessage.getStatus());
            sb.append(newLine);
            sb.append("Subject: " + mmsMessage.getSubject());
            sb.append(newLine);
            sb.append("Subject charset: " + mmsMessage.getSubjectCharset());
            sb.append(newLine);
            sb.append("Subscription id: " + mmsMessage.getSubscriptionId());
            sb.append(newLine);
            sb.append("Transaction id: " + mmsMessage.getTransactionId());
            return sb.toString();
        });
    }

    /**
     * Gets MMS data
     *
     * @param context   Activity context
     * @param messageId Message id
     * @return Disposable object
     */
    public Disposable getMmsData(Context context, String messageId) {
        return Single.fromCallable((Callable<Object>) () -> {
            MmsRepository mmsRepository = new MmsRepository();
            return mmsRepository.getMmsData(context, messageId);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> mMutableMmsData.setValue((List<BaseModel.Data>) o));
    }

    /**
     * Returns MMS media data
     * Method used to provide live updates on data
     *
     * @return Live data object
     */
    public LiveData<List<BaseModel.Data>> watchMmsData() {
        return mMutableMmsData;
    }

    public MmsViewModel() {
        mConversationsObserver = new ConversationsObserver(mSmsHandler);
    }

    /**
     * Deletes MMS threads
     *
     * @param threadIdsList Thread Ids list
     * @return Operation results
     */
    Single<ContentProviderResult[]> deleteMmsThreads(List<String> threadIdsList) {
        Log.d(TAG, "deleteMmsThreads()");

        return Single.fromCallable(() -> {
            MmsRepository mmsRepository = new MmsRepository();
            return mmsRepository.deleteMmsThreads(mContext, threadIdsList);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Disposable markAllMessagesAsRead(String threadId) {
        Log.d(TAG, "markAllMessagesAsRead()");

        return Single.fromCallable(() -> {
            MmsRepository mmsRepository = new MmsRepository();
            return mmsRepository.markAllMessagesAsReadUsingThreadId(mContext, threadId);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> Log.d(TAG, "markAllMessagesAsRead(), Rows updated: " + o));
    }

    /**
     * Receives updates from {@link ConversationsObserver}
     */
    private Handler mSmsHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.getData() != null) {
                boolean needChange = msg.getData().getBoolean(ConversationsObserver.BUNDLE_ARGS_CHANGE_STATUS);
                if (needChange) {
                    String hostUri = msg.getData().getString(ConversationsObserver.BUNDLE_ARGS_URI);
                    if (!TextUtils.isEmpty(hostUri) && hostUri.equals(Telephony.Mms.CONTENT_URI.getHost())) {
                        if (mActiveFragment instanceof MmsConversationFragment) {
                            getAllMmsConversations();
                        } else if (mActiveFragment instanceof MmsMessagesFragment &&
                                !TextUtils.isEmpty(((MmsMessagesFragment) mActiveFragment).getThreadId())) {
                            getMmsMessagesByThreadId(((MmsMessagesFragment) mActiveFragment).getThreadId());
                        }
                    }
                }
            }
        }
    };

    /**
     * Registers for conversation table
     *
     * @param fragment Current active fragment
     */
    public void registerMmsMessages(Fragment fragment) {
        mContext.getContentResolver().registerContentObserver(Telephony.Mms.CONTENT_URI,
                true,
                mConversationsObserver);
        this.mActiveFragment = fragment;
    }

    /**
     * Unregister from conversations table
     */
    public void unregisterMmsMessages() {
        mContext.getContentResolver().unregisterContentObserver(mConversationsObserver);
        this.mActiveFragment = null;
    }

    /**
     * Delets MMS messages
     *
     * @param messageIdsList Message Ids list
     * @return Operation results
     */
    public Single<ContentProviderResult[]> deleteMmsMessages(List<String> messageIdsList) {
        Log.d(TAG, "deleteMmsMessages()");

        return Single.fromCallable(() -> {
            MmsRepository mmsRepository = new MmsRepository();
            return mmsRepository.deleteMmsMessages(mContext, messageIdsList);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
