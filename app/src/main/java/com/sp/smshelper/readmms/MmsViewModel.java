package com.sp.smshelper.readmms;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.sp.smshelper.model.MmsConversation;
import com.sp.smshelper.repository.MmsRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MmsViewModel extends ViewModel {

    private static final String TAG = MmsViewModel.class.getSimpleName();

    private Context mContext;

    public void setContext(Context context) {
        this.mContext = context;
    }

    Disposable getAllMmsConversations() {

        return Single.fromCallable(() -> {
            MmsRepository mmsRepository = new MmsRepository();
            return mmsRepository.getAllMmsConversations(mContext);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultObject -> {
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
}
