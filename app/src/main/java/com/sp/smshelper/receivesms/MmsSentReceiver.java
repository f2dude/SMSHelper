package com.sp.smshelper.receivesms;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;

import com.sp.smshelper.repository.MmsRepository;

import java.io.File;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MmsSentReceiver extends BroadcastReceiver {

    public static final String MMS_SENT = "com.sp.smshelper.receivesms.MMS_SENT";
    public static final String EXTRA_CONTENT_URI = "content_uri";
    public static final String EXTRA_FILE_PATH = "file_path";
    private static final String TAG = MmsSentReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "MMS has finished sending, marking it as so, in the database");

        Observable.fromAction(() -> {
            Uri uri = Uri.parse(intent.getStringExtra(EXTRA_CONTENT_URI));
            Log.d(TAG, uri.toString());

            ContentValues values = new ContentValues(1);
            values.put(Telephony.Mms.MESSAGE_BOX, Telephony.Mms.MESSAGE_BOX_SENT);

            MmsRepository mmsRepository = new MmsRepository();
            mmsRepository.update(context, context.getContentResolver(), uri, values,
                    null, null);

            String filePath = intent.getStringExtra(EXTRA_FILE_PATH);
            Log.d(TAG, "File path: " + filePath);
            if (!TextUtils.isEmpty(filePath)) {
                File file = new File(filePath);
                if (file.exists()) {
                    boolean isDeleted = file.delete();
                    Log.d(TAG, "Is deleted: " + isDeleted);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        Log.d(TAG, "onSubscribe(), is disposed: " + d.isDisposed());
                    }

                    @Override
                    public void onNext(@NonNull Object o) {
                        Log.d(TAG, "onNext(): " + o);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e(TAG, "onError(): " + e);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete()");
                    }
                });
    }
}
