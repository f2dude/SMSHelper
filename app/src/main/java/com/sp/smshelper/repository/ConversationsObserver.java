package com.sp.smshelper.repository;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony;
import android.util.Log;

import java.util.Objects;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ConversationsObserver extends ContentObserver {

    private static final String TAG = ConversationsObserver.class.getSimpleName();
    public static final String BUNDLE_ARGS_CHANGE_STATUS = "args_change_status";
    public static final String BUNDLE_ARGS_URI = "args_uri";

    private Handler mHandler;

    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public ConversationsObserver(Handler handler) {
        super(handler);
        this.mHandler = handler;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return super.deliverSelfNotifications();
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.d(TAG, "onChange(), Self change: " + selfChange);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        Log.d(TAG, "Self change: " + selfChange + " ,URI: " + uri);

        Observable.fromCallable(() -> isUriPresent(uri))
                .flatMap((Function<Boolean, ObservableSource<Message>>) aBoolean -> Observable.just(Objects.requireNonNull(getMessageData(aBoolean, uri))))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultObject ->
                        {
                            if (null != resultObject) {
                                mHandler.sendMessage(resultObject);
                            }
                        },
                        error -> Log.e(TAG, "Error in onChange(): " + error));
    }

    private Message getMessageData(boolean aBoolean, Uri uri) {
        if (aBoolean) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(BUNDLE_ARGS_CHANGE_STATUS, true);
            bundle.putString(BUNDLE_ARGS_URI, uri.getHost());

            Message message = new Message();
            message.setData(bundle);
            return message;
        }
        return null;
    }

    private boolean isUriPresent(Uri uri) {
        if (Objects.requireNonNull(uri.getHost()).equals(Objects.requireNonNull(Telephony.Sms.CONTENT_URI.getHost()))) {
            Log.d(TAG, "=====URI matched=====" + uri.getHost());
            return true;
        }
        return false;
    }
}
