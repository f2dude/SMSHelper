package com.sp.smshelper.receivesms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import com.sp.smshelper.repository.ConversationsRepository;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = SmsReceiver.class.getSimpleName();
    public static final String PDU_TYPE = "pdus";

    @Override
    public void onReceive(Context context, Intent intent) {

        Observable.fromAction(() -> {
            if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
                //Get the sms message
                Bundle bundle = intent.getExtras();
                SmsMessage[] msgs;
                StringBuilder message = new StringBuilder();
                boolean isReplyPathPresent = false;
                assert bundle != null;
                String format = bundle.getString("format");
                //Retrieve the sms message received
                Object[] pdus = (Object[]) bundle.get(PDU_TYPE);
                if (null != pdus) {
                    String address = null;
                    msgs = new SmsMessage[pdus.length];
                    for (int i = 0; i < msgs.length; i++) {
                        msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                        //Build the message to show
                        if (TextUtils.isEmpty(address)) {
                            address = msgs[i].getOriginatingAddress();
                            isReplyPathPresent = msgs[i].isReplyPathPresent();
                        }
                        message.append(msgs[i].getMessageBody());
                    }
                    int subscription = bundle.getInt("subscription", -1);
                    Log.d(TAG, "Slot: " + bundle.getInt("slot", -1)
                            + " , Phone: " + bundle.getInt("phone", -1)
                            + " , Subscription: " + subscription);
                    Log.d(TAG, "onReceive: " + message);
                    //Save the message in data base
                    ConversationsRepository conversationsRepository = new ConversationsRepository();
                    conversationsRepository.saveIncomingSmsMessage(context, address, message.toString(), isReplyPathPresent, subscription);
                }
            }else if (intent.getAction().equals("android.provider.Telephony.SMS_DELIVER")) {
                //Do nothing
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
