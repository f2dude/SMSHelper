package com.sp.smshelper.readmms;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sp.smshelper.model.MmsConversation;
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
                .subscribe(resultObject -> {
                    //Set data
                    mMutableMmsConversations.setValue(resultObject);

                            for (MmsConversation mmsConversation : resultObject) {
                                    Log.d(TAG, "Thread id: " + mmsConversation.getThreadId());
                                    Log.d(TAG, "Date: " + mmsConversation.getDate());
                                    Log.d(TAG, "Content type: " + mmsConversation.getContentType());
                                    Log.d(TAG, "Read: " + mmsConversation.isRead());
                                    Log.d(TAG, "Text only: " + mmsConversation.isTextOnly());
                                    Log.d(TAG, "Text: " + mmsConversation.getText());
                                    for(String address: mmsConversation.getAddressList()) {
                                        Log.d(TAG, "Address: " + address);
                                    }
                                    if (mmsConversation.getData() != null) {
                                        Log.d(TAG, "Content type: " + mmsConversation.getData().getContentType());
                                        Log.d(TAG, "Data path: " + mmsConversation.getData().getDataPath());
                                    }
                                    Log.d(TAG, "+++++++++++++++++++++++++++++++++++++");
                            }
                        },
                        error -> Log.e(TAG, "Error in getAllMmsConversations(): " + error));
    }

    /**
     * Returns MMS conversations
     * Method used to provide live updates of data
     * @return MMS conversation list
     */
    public LiveData<List<MmsConversation>> watchMmsConversations() {
        return mMutableMmsConversations;
    }
}
