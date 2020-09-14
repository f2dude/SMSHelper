package com.sp.smshelper.sendsms;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sp.smshelper.R;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SendSmsViewModel extends ViewModel {

    private static final String TAG = SendSmsViewModel.class.getSimpleName();
    private static final String SENT = "SMS_SENT";
    private static final String DELIVERED = "SMS_DELIVERED";

    private MutableLiveData<List<SubscriptionInfo>> mMutableSimCardsList = new MutableLiveData<>();
    private MutableLiveData<String> mMutableMessageSent = new MutableLiveData<>();
    private MutableLiveData<String> mMutableMessageDelivered = new MutableLiveData<>();
    private int mSubscriptionId = -1;
    private Context mContext;

    /**
     * Returns sim card numbers from device.
     * If dual sim, returns phone numbers from 2 sim's
     *
     * @return Disposable object
     */
     Disposable getDeviceNumbersList() {
        Log.d(TAG, "getDeviceNumbersList()");
        return Single.fromCallable(() -> {
            List<SubscriptionInfo> subscriptionInfoList = new ArrayList<>();
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                SubscriptionManager subscriptionManager = SubscriptionManager.from(mContext);
                subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            } else {
                Log.e(TAG, "READ_PHONE_STATE permission not granted");
            }
            return subscriptionInfoList;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resultObject ->
                                //set data
                                mMutableSimCardsList.setValue(resultObject),
                        error -> Log.e(TAG, "Error in getDeviceNumbersList(): " + error));
    }

    /**
     * Creates an observer for sim card list
     *
     * @return Live data subscription list
     */
    LiveData<List<SubscriptionInfo>> watchSimCardsList() {
        return mMutableSimCardsList;
    }

    /**
     * Creates an observer for message sent
     *
     * @return Live data
     */
    LiveData<String> watchMessageSentStatus() {
        return mMutableMessageSent;
    }

    /**
     * Creates an observer for message delivered
     *
     * @return Live data
     */
    LiveData<String> watchMessageDeliveredStatus() {
        return mMutableMessageDelivered;
    }

    /**
     * Sets activity context
     *
     * @param mContext Activity context
     */
    public void setContext(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * Sets the subscription id for fetching phone number
     *
     * @param mSubscriptionId Id
     */
    void setSubscriptionId(int mSubscriptionId) {
        this.mSubscriptionId = mSubscriptionId;
    }

    void registerReceiver() {
        mContext.registerReceiver(sentBR, new IntentFilter(SENT));
        mContext.registerReceiver(deliveredBr, new IntentFilter(DELIVERED));
    }

    void unRegisterReceiver() {
        mContext.unregisterReceiver(sentBR);
        mContext.unregisterReceiver(deliveredBr);
    }

    /**
     * Method to send the message
     *
     * @param remoteNumber Remote party number
     * @param message      Message to send
     */
    Disposable sendSMS(String remoteNumber, String message) {

        return Single.fromCallable(() -> {
            boolean status = false;
            if (mSubscriptionId > -1) {
                status = true;
                PendingIntent sentPi = PendingIntent.getBroadcast(mContext, 0, new Intent(SENT), 0);
                PendingIntent deliveredPi = PendingIntent.getBroadcast(mContext, 0, new Intent(DELIVERED), 0);

                SmsManager smsManager = SmsManager.getSmsManagerForSubscriptionId(mSubscriptionId);
                ArrayList<String> parts = smsManager.divideMessage(message);

                ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                ArrayList<PendingIntent> deliveryIntents = new ArrayList<>();

                for (int i = 0; i < parts.size(); i++) {
                    sentIntents.add(sentPi);
                    deliveryIntents.add(deliveredPi);
                }

                smsManager.sendMultipartTextMessage(remoteNumber, null, parts, sentIntents, deliveryIntents);
            }
            return status;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status ->
                        {
                            if (Boolean.FALSE.equals(status)) {
                                Toast.makeText(mContext, mContext.getText(R.string.select_phone_number), Toast.LENGTH_SHORT).show();
                            }
                        },
                        error -> Log.e(TAG, "Error in sendSMS(): " + error));
    }

    private BroadcastReceiver sentBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "=====Sent broadcast receiver=====");
            String status = "NULL";
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    status = "SENT";
                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    status = "RESULT_ERROR_GENERIC_FAILURE";
                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    status = "RESULT_ERROR_NO_SERVICE";
                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    status = "RESULT_ERROR_NULL_PDU";
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    status = "RESULT_ERROR_RADIO_OFF";
                    break;
                default:
                    break;
            }
            mMutableMessageSent.setValue(context.getString(R.string.message_sent));
            Toast.makeText(mContext, status, Toast.LENGTH_SHORT).show();
        }
    };

    private BroadcastReceiver deliveredBr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "=====Delivered broadcast receiver=====");
            String status = "NULL";
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    status = "DELIVERED";
                    break;
                case Activity.RESULT_CANCELED:
                    status = "NOT DELIVERED";
                    break;
                default:
                    break;
            }
            mMutableMessageDelivered.setValue(context.getString(R.string.message_delivered));
            Toast.makeText(mContext, status, Toast.LENGTH_SHORT).show();
        }
    };
}
