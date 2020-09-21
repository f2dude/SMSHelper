package com.sp.smshelper.conversation;

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

import com.sp.smshelper.messages.SmsMessagesFragment;
import com.sp.smshelper.model.Conversation;
import com.sp.smshelper.model.SmsMessage;
import com.sp.smshelper.repository.ConversationsObserver;
import com.sp.smshelper.repository.ConversationsRepository;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ConversationsViewModel extends ViewModel {

    private static final String TAG = ConversationsViewModel.class.getSimpleName();

    private MutableLiveData<List<Conversation>> mMutableConversationData = new MutableLiveData<>();
    private MutableLiveData<List<SmsMessage>> mMutableSmsMessageData = new MutableLiveData<>();
    private MutableLiveData<String> mMutableSmsMessageDetails = new MutableLiveData<>();

    private ConversationsObserver mConversationsObserver;
    private Context mContext;
    private Fragment mActiveFragment;

    public ConversationsViewModel() {
        mConversationsObserver = new ConversationsObserver(mSmsHandler);
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    protected Disposable getAllConversations() {

        return Single.fromCallable(() -> {
            ConversationsRepository conversationsRepository = new ConversationsRepository();
            return conversationsRepository.getAllConversations(mContext);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultObject ->
                                //set data
                                mMutableConversationData.setValue(resultObject),
                        error -> Log.e(TAG, "Error in readConversationsFromDb(): " + error));
    }

    protected LiveData<List<Conversation>> watchConversations() {
        return mMutableConversationData;
    }

    public Disposable getSmsMessagesByThreadId(String threadId) {

        return Single.fromCallable(() -> {
            ConversationsRepository conversationsRepository = new ConversationsRepository();
            return conversationsRepository.getSmsMessagesByThreadId(mContext, threadId);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultObject ->
                                //set data
                                mMutableSmsMessageData.setValue(resultObject),
                        error -> Log.e(TAG, "Error in getSmsMessagesByThreadId(): " + error));
    }

    public LiveData<List<SmsMessage>> watchSmsMessages() {
        return mMutableSmsMessageData;
    }

    public Disposable getMessageDetailsById(String messageId) {
        return Observable.fromCallable(() -> {
            ConversationsRepository conversationsRepository = new ConversationsRepository();
            return conversationsRepository.getSmsMessageByMessageId(mContext, messageId);
        }).flatMap((Function<SmsMessage, ObservableSource<String>>) smsMessage -> Observable.just(generateMessageData(smsMessage)))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultObject ->
                                //Set data
                                mMutableSmsMessageDetails.setValue(resultObject),
                        error -> Log.e(TAG, "Error in getSmsMessagesByThreadId(): " + error));
    }

    private String generateMessageData(SmsMessage messageDetails) {
        String newLine = "\n";
        StringBuilder sb = new StringBuilder();
        sb.append("Thread id: ").append(messageDetails.getThreadId());
        sb.append(newLine);
        sb.append("Message id: ").append(messageDetails.getMessageId());
        sb.append(newLine);
        sb.append("Address: ").append(messageDetails.getAddress());
        sb.append(newLine);
        sb.append("Body: ").append(messageDetails.getBody());
        sb.append(newLine);
        sb.append("Date: ").append(messageDetails.getDate());
        sb.append(newLine);
        sb.append("Message type: ").append(messageDetails.getType().name());
        sb.append(newLine);
        sb.append("Protocol: ").append(messageDetails.getProtocol());
        sb.append(newLine);
        sb.append("Read status: ").append(messageDetails.getRead());
        sb.append(newLine);
        sb.append("Message status: ").append(messageDetails.getStatus());
        sb.append(newLine);
        sb.append("Reply path present: ").append(messageDetails.isReplyPathPresent());
        sb.append(newLine);
        sb.append("Subject: ").append(messageDetails.getSubject());
        sb.append(newLine);
        sb.append("Creator: ").append(messageDetails.getCreator());
        sb.append(newLine);
        sb.append("Date sent: ").append(messageDetails.getDateSent());
        sb.append(newLine);
        sb.append("Error code: ").append(messageDetails.getErrorCode());
        sb.append(newLine);
        sb.append("Locked: ").append(messageDetails.getLocked());
        sb.append(newLine);
        sb.append("Person: ").append(messageDetails.getPerson());
        sb.append(newLine);
        sb.append("Subscription id: ").append(messageDetails.getSubscriptionId());
        sb.append(newLine);
        sb.append("Is seen: ").append(messageDetails.isSeen());

        return sb.toString();
    }

    public LiveData<String> watchSmsMessageDetails() {
        return mMutableSmsMessageDetails;
    }

    public Disposable markAllMessagesAsRead(String threadId) {
        Log.d(TAG, "markAllMessagesAsRead()");

        return Single.fromCallable(() -> {
            ConversationsRepository conversationsRepository = new ConversationsRepository();
            return conversationsRepository.markAllMessagesAsReadUsingThreadId(mContext, threadId);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> Log.d(TAG, "markAllMessagesAsRead(), Rows updated: " + o));
    }

    public Disposable markMessageAsRead(String messageId) {
        Log.d(TAG, "markMessageAsRead()");

        return Single.fromCallable(() -> {
            ConversationsRepository conversationsRepository = new ConversationsRepository();
            return conversationsRepository.markMessageAsRead(mContext, messageId);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    Log.d(TAG, "markMessageAsRead(), Rows updated: " + o);
                    if (o > 0) {
                        //Get the message details
                        getMessageDetailsById(messageId);
                    }
                });
    }

    Single<ContentProviderResult[]> deleteSmsThreads(List<String> threadIdsList) {
        Log.d(TAG, "deleteSmsThreads()");

        return Single.fromCallable(() -> {
            ConversationsRepository conversationsRepository = new ConversationsRepository();
            return conversationsRepository.deleteSmsThreads(mContext, threadIdsList);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Registers for conversation table
     *
     * @param fragment Current active fragment
     */
    public void registerSmsMessages(Fragment fragment) {
        mContext.getContentResolver().registerContentObserver(Telephony.Sms.CONTENT_URI,
                true,
                mConversationsObserver);
        this.mActiveFragment = fragment;
    }

    /**
     * Unregister from conversations table
     */
    public void unregisterSmsMessages() {
        mContext.getContentResolver().unregisterContentObserver(mConversationsObserver);
        this.mActiveFragment = null;
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
                    if (!TextUtils.isEmpty(hostUri) && hostUri.equals(Telephony.Sms.Conversations.CONTENT_URI.getHost())) {
                        if (mActiveFragment instanceof ConversationsFragment) {
                            getAllConversations();
                        } else if (mActiveFragment instanceof SmsMessagesFragment &&
                                !TextUtils.isEmpty(((SmsMessagesFragment) mActiveFragment).getThreadId())) {
                            getSmsMessagesByThreadId(((SmsMessagesFragment) mActiveFragment).getThreadId());
                        }
                    }
                }
            }
        }
    };

}
