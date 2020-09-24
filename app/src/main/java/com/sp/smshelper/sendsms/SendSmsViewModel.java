package com.sp.smshelper.sendsms;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sp.smshelper.R;
import com.sp.smshelper.repository.ConversationsRepository;

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
    private static final String REMOTE_NUMBER = "remote_number";
    private static final String MESSAGE_BODY = "message_body";
    private static final String MESSAGE_URI = "message_uri";

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
            if (TextUtils.isEmpty(remoteNumber)) {
                return false;
            }

            if (TextUtils.isEmpty(message)) {
                return false;
            }
            if (mSubscriptionId > -1) {
                //Save outgoing message in the beginning
                Uri uri = saveOutgoingSmsMessage(remoteNumber, message);

                status = true;
                Intent sentIntent = new Intent(SENT);
                PendingIntent sentPi = PendingIntent.getBroadcast(mContext,
                        0,
                        sentIntent,
                        PendingIntent.FLAG_ONE_SHOT);

                Intent deliverIntent = new Intent(DELIVERED);
                deliverIntent.putExtra(REMOTE_NUMBER, remoteNumber);
                deliverIntent.putExtra(MESSAGE_BODY, message);
                deliverIntent.putExtra(MESSAGE_URI, uri.toString());
                PendingIntent deliveredPi = PendingIntent.getBroadcast(mContext,
                        0,
                        deliverIntent,
                        PendingIntent.FLAG_ONE_SHOT);

                SmsManager smsManager = SmsManager.getSmsManagerForSubscriptionId(mSubscriptionId);
                ArrayList<String> parts = smsManager.divideMessage(message);

                ArrayList<PendingIntent> sentIntents = new ArrayList<>();
                ArrayList<PendingIntent> deliveryIntents = new ArrayList<>();

                sentIntents.add(sentPi);
                deliveryIntents.add(deliveredPi);

                smsManager.sendMultipartTextMessage(remoteNumber, null, parts, sentIntents, deliveryIntents);
            }
            return status;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status ->
                        {
                            if (Boolean.FALSE.equals(status)) {
                                if (TextUtils.isEmpty(remoteNumber)) {
                                    Toast.makeText(mContext, mContext.getText(R.string.enter_destination_number), Toast.LENGTH_SHORT).show();
                                } else if (TextUtils.isEmpty(message)) {
                                    Toast.makeText(mContext, mContext.getText(R.string.enter_message), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(mContext, mContext.getText(R.string.select_phone_number), Toast.LENGTH_SHORT).show();
                                }
                            }
                        },
                        error -> Log.e(TAG, "Error in sendSMS(): " + error));
    }

    private Uri saveOutgoingSmsMessage(String address, String message) {
        Log.d(TAG, "saveOutgoingSmsMessage()");
        //Save outgoing SMS message
        ConversationsRepository conversationsRepository = new ConversationsRepository();
        return conversationsRepository.saveOutgoingSmsMessage(mContext, address, message, mSubscriptionId);
    }

    private BroadcastReceiver sentBR = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "=====Sent broadcast receiver=====");
            String status = translateSentResult(getResultCode());
            mMutableMessageSent.setValue(status);
            Toast.makeText(mContext, status, Toast.LENGTH_SHORT).show();
        }

        /**
         * Translates status codes of SMS sent status
         *
         * @param resultCode SMS sent status result code
         * @return Status of SMS message
         */
        private String translateSentResult(int resultCode) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    return "Sent";
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    return "RESULT_ERROR_GENERIC_FAILURE";
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    return "RESULT_ERROR_RADIO_OFF";
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    return "RESULT_ERROR_NULL_PDU";
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    return "RESULT_ERROR_NO_SERVICE";
                default:
                    return "Unknown error code";
            }
        }
    };

    private BroadcastReceiver deliveredBr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "=====Delivered broadcast receiver=====");

            Single.fromCallable(() -> {
                //Get SMS status
                SmsMessage sms = null;
                // A delivery result comes from the service center as a simple SMS in a single PDU.
                byte[] pdu = intent.getByteArrayExtra("pdu");
                String format = intent.getStringExtra("format");

                // Construct the SmsMessage from the PDU.
                sms = SmsMessage.createFromPdu(pdu, format);

                // getResultCode() is not reliable for delivery results.
                // We need to get the status from the SmsMessage.
                String deliveryResult = "Delivery result : " + translateDeliveryStatus(sms.getStatus());
                Log.d(TAG, "Status: " + sms.getStatus() + " , Delivery result: " + deliveryResult);

                String uri = intent.getStringExtra(MESSAGE_URI);
                Log.d(TAG, "Message uri: " + uri
                        + " ,Number: " + intent.getStringExtra(REMOTE_NUMBER)
                        + " ,Message: " + intent.getStringExtra(MESSAGE_BODY));

                //Update delivery status
                if (!TextUtils.isEmpty(uri)) {
                    ConversationsRepository conversationsRepository = new ConversationsRepository();
                    conversationsRepository.updateDeliveryStatusOfSentMessage(mContext, Uri.parse(uri), sms.getStatus());
                }

                //Get result code status
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
                return status;
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(s -> {
                        mMutableMessageDelivered.setValue(s);
                        Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
                    }, error -> Log.e(TAG, "Error in broadcast receiver deliveredBr: " + error));
        }

        /**
         * Translates the status code of delivery status
         *
         * @param status Delivery status code
         * @return Delivery status
         */
        private String translateDeliveryStatus(int status) {
            switch (status) {
                case Telephony.Sms.STATUS_COMPLETE:
                    return "Sms.STATUS_COMPLETE";
                case Telephony.Sms.STATUS_FAILED:
                    return "Sms.STATUS_FAILED";
                case Telephony.Sms.STATUS_PENDING:
                    return "Sms.STATUS_PENDING";
                case Telephony.Sms.STATUS_NONE:
                    return "Sms.STATUS_NONE";
                default:
                    return "Unknown status code";
            }
        }
    };
}
