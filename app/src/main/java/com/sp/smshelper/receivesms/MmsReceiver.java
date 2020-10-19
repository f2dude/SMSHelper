package com.sp.smshelper.receivesms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;

import com.sp.smshelper.pdu_utils.ContentType;
import com.sp.smshelper.pdu_utils.GenericPdu;
import com.sp.smshelper.pdu_utils.MmsConfig;
import com.sp.smshelper.pdu_utils.MmsException;
import com.sp.smshelper.pdu_utils.NotificationInd;
import com.sp.smshelper.pdu_utils.PduParser;
import com.sp.smshelper.repository.MmsRepository;
import com.sp.smshelper.repository.PduPersister;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

import static android.provider.Telephony.Sms.Intents.WAP_PUSH_DELIVER_ACTION;
import static android.provider.Telephony.Sms.Intents.WAP_PUSH_RECEIVED_ACTION;
import static com.sp.smshelper.pdu_utils.PduHeaders.MESSAGE_TYPE_DELIVERY_IND;
import static com.sp.smshelper.pdu_utils.PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND;
import static com.sp.smshelper.pdu_utils.PduHeaders.MESSAGE_TYPE_READ_ORIG_IND;

public class MmsReceiver extends BroadcastReceiver {

    private static final String TAG = MmsReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        Observable.fromAction(() -> {
            Log.d(TAG, intent.getAction() + " " + intent.getType());

            MmsConfig.init(context);

            if ((intent.getAction().equals(WAP_PUSH_DELIVER_ACTION)
                    || intent.getAction().equals(WAP_PUSH_RECEIVED_ACTION))
                    && ContentType.MMS_MESSAGE.equals(intent.getType())) {

                Bundle bundle = intent.getExtras();
                int subscriptionId = bundle.getInt("subscription", -1);
                Log.d(TAG, "Slot: " + bundle.getInt("slot", -1)
                        + " , Phone: " + bundle.getInt("phone", -1)
                        + " , Subscription: " + subscriptionId);


                // Get raw PDU push-data from the message and parse it
                byte[] pushData = intent.getByteArrayExtra("data");
                PduParser parser = new PduParser(pushData);
                GenericPdu pdu = parser.parse();
                if (null == pdu) {
                    Log.e(TAG, "Invalid PUSH data");
                    return;
                }
                int type = pdu.getMessageType();
                Log.d(TAG, "PDU message type: " + type);
                //Process pdu
                processPdu(context, type, subscriptionId, pdu);
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

    /**
     * Process the PDU object
     *
     * @param context        Activity context
     * @param type           MMS type
     * @param subscriptionId Subscription Id
     * @param pdu            PDU object
     */
    private void processPdu(Context context, int type, int subscriptionId, GenericPdu pdu) {
        try {
            switch (type) {
                case MESSAGE_TYPE_DELIVERY_IND:
                case MESSAGE_TYPE_READ_ORIG_IND:
                    // TODO Pending implementation
                    break;
                case MESSAGE_TYPE_NOTIFICATION_IND:
                    Log.d(TAG, "MESSAGE_TYPE_NOTIFICATION_IND");
                    NotificationInd nInd = (NotificationInd) pdu;
                    //Get carrier configuration values
                    Bundle configOverrides = SmsManager.getSmsManagerForSubscriptionId(subscriptionId).getCarrierConfigValues();
                    boolean appendTransactionId = configOverrides.getBoolean(SmsManager.MMS_CONFIG_APPEND_TRANSACTION_ID);

                    if (MmsConfig.getTransIdEnabled() || appendTransactionId) {
                        byte[] contentLocation = nInd.getContentLocation();
                        if ('=' == contentLocation[contentLocation.length - 1]) {
                            byte[] transactionId = nInd.getTransactionId();
                            byte[] contentLocationWithId = new byte[contentLocation.length
                                    + transactionId.length];
                            System.arraycopy(contentLocation, 0, contentLocationWithId,
                                    0, contentLocation.length);
                            System.arraycopy(transactionId, 0, contentLocationWithId,
                                    contentLocation.length, transactionId.length);
                            nInd.setContentLocation(contentLocationWithId);
                        }
                    }
                    downloadMessage(context, subscriptionId, pdu, nInd);
                    break;
                default:
                    Log.e(TAG, "Received unrecognized PDU.");
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in processPdu(): " + e);
        }
    }

    private void downloadMessage(Context context, int subscriptionId, GenericPdu pdu, NotificationInd nInd) {
        try {
            PduPersister p = PduPersister.getPduPersister(context);
            if (!isDuplicateNotification(context, nInd)) {
//                Uri uri = p.persist(pdu, Telephony.Mms.Inbox.CONTENT_URI,
//                        true,
//                        true,
//                        null,
//                        subscriptionId);

                String contentLocation = p.getContentLocationFromPduHeader(pdu);
//                try {
//                    contentLocation = getContentLocation(context, uri);
//                } catch (MmsException ex) {
//                    contentLocation = p.getContentLocationFromPduHeader(pdu);
//                    if (TextUtils.isEmpty(contentLocation)) {
//                        throw ex;
//                    }
//                }
                Log.d(TAG, "Content location: " + contentLocation);

                DownloadManager.getInstance().downloadMultimediaMessage(context, contentLocation, null, true, subscriptionId);
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in downloadMessage(): " + e);
        }
    }

    /**
     * Gets the content location
     *
     * @param context Activity context
     * @param uri     MMS uri
     * @return Content location
     * @throws MmsException
     */
    private String getContentLocation(Context context, Uri uri) throws MmsException {
        MmsRepository mmsRepository = new MmsRepository();
        return mmsRepository.getContentLocation(context, uri);
    }

    /**
     * Checks if the received MMS notification is duplicate
     *
     * @param context Activity context
     * @param nInd    MMS notification indication
     * @return True if it exists, False otherwise
     */
    private boolean isDuplicateNotification(Context context, NotificationInd nInd) {
        byte[] rawLocation = nInd.getContentLocation();
        if (rawLocation != null) {
            String location = new String(rawLocation);
            //Query table
            MmsRepository mmsRepository = new MmsRepository();
            return mmsRepository.isContentLocationExists(context, location);
        }
        return false;
    }
}
