package com.sp.smshelper.receivesms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.util.Log;

import com.sp.smshelper.pdu_utils.PduHeaders;
import com.sp.smshelper.pdu_utils.Utils;
import com.sp.smshelper.repository.MmsRepository;
import com.sp.smshelper.repository.PduPersister;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MmsReceivedReceiver extends BroadcastReceiver {

    public static final String MMS_RECEIVED = "com.sp.smshelper.receivesms.MMS_RECEIVED";
    public static final String EXTRA_FILE_PATH = "file_path";
    public static final String EXTRA_LOCATION_URL = "location_url";
    public static final String EXTRA_TRIGGER_PUSH = "trigger_push";
    public static final String EXTRA_URI = "notification_ind_uri";
    public static final String SUBSCRIPTION_ID = "subscription_id";
    private static final String TAG = MmsReceivedReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Observable.fromAction(() -> {
            Log.d(TAG, "MMS has finished downloading. Now, saving it to data base");

            final String path = intent.getStringExtra(EXTRA_FILE_PATH);
            final int subscriptionId = intent.getIntExtra(SUBSCRIPTION_ID, Utils.getDefaultSubscriptionId());
            Log.d(TAG, path);

            FileInputStream reader = null;
            Uri messageUri = null;

            try {
                File mDownloadFile = new File(path);
                if (!mDownloadFile.exists()) {
                    Log.e(TAG, "File does not exists.");
                    return;

                }
                final int nBytes = (int) mDownloadFile.length();
                reader = new FileInputStream(mDownloadFile);
                final byte[] response = new byte[nBytes];
                reader.read(response, 0, nBytes);

                PduPersister p = PduPersister.getPduPersister(context);
                messageUri = p.persist(context, response,
                        intent.getStringExtra(EXTRA_LOCATION_URL),
                        subscriptionId, null);
                Log.d(TAG, "New MMS inserted uri: " + messageUri);

//                List<CommonAsyncTask> tasks = getNotificationTask(context, intent, response);
//
//                messageUri = DownloadRequest.persist(context, response,
//                        new MmsConfig.Overridden(new MmsConfig(context), null),
//                        intent.getStringExtra(EXTRA_LOCATION_URL),
//                        subscriptionId, null);

                Log.d(TAG, "response saved successfully");
                Log.d(TAG, "response length: " + response.length);
                mDownloadFile.delete();

//                if (tasks != null) {
//                    Log.v(TAG, "running the common async notifier for download");
//                    for (CommonAsyncTask task : tasks)
//                        task.executeOnExecutor(RECEIVE_NOTIFICATION_EXECUTOR);
//                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "MMS received, file not found exception: " + e);
            } catch (IOException e) {
                Log.e(TAG, "MMS received, io exception: " + e);
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        Log.e(TAG, "MMS received, io exception", e);
                    }
                }
            }

            handleHttpError(context, intent);
            DownloadManager.finishDownload(intent.getStringExtra(EXTRA_LOCATION_URL));
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

    private void handleHttpError(Context context, Intent intent) {
        String locationSelection =
                Telephony.Mms.MESSAGE_TYPE + "=? AND " + Telephony.Mms.CONTENT_LOCATION + " =?";
        final int httpError = intent.getIntExtra(SmsManager.EXTRA_MMS_HTTP_STATUS, 0);
        if (httpError == 404 || httpError == 400) {
            // Delete the corresponding NotificationInd
            MmsRepository mmsRepository = new MmsRepository();
            mmsRepository.delete(context,
                    context.getContentResolver(),
                    Telephony.Mms.CONTENT_URI,
                    locationSelection,
                    new String[]{
                            Integer.toString(PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND),
                            intent.getStringExtra(EXTRA_LOCATION_URL)
                    });
        }
    }
}
