package com.sp.smshelper.readmms;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sp.smshelper.model.MmsConversation;
import com.sp.smshelper.model.MmsMessage;
import com.sp.smshelper.repository.MmsRepository;

import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MmsViewModel extends ViewModel {

    private static final String TAG = MmsViewModel.class.getSimpleName();

    private Context mContext;

    private MutableLiveData<List<MmsConversation>> mMutableMmsConversations = new MutableLiveData<>();
    private MutableLiveData<List<MmsMessage>> mMutableMmsMessages = new MutableLiveData<>();

    public void setContext(Context context) {
        this.mContext = context;
    }

    /**
     * Returns all MMS conversations
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
     * @return MMS conversation list
     */
    LiveData<List<MmsConversation>> watchMmsConversations() {
        return mMutableMmsConversations;
    }

    /**
     * Returns list of MMS messages mapped based on thread id
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
     * @return MMS message list
     */
    LiveData<List<MmsMessage>> watchMmsMessages() {
        return mMutableMmsMessages;
    }
}
