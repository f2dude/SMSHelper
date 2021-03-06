package com.sp.smshelper.sendmms;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.sp.smshelper.R;
import com.sp.smshelper.model.MMSPart;
import com.sp.smshelper.model.MmsSenderObject;
import com.sp.smshelper.pdu_utils.CharacterSets;
import com.sp.smshelper.pdu_utils.ContentType;
import com.sp.smshelper.pdu_utils.EncodedStringValue;
import com.sp.smshelper.pdu_utils.InvalidHeaderValueException;
import com.sp.smshelper.pdu_utils.MmsConfig;
import com.sp.smshelper.pdu_utils.PduBody;
import com.sp.smshelper.pdu_utils.PduComposer;
import com.sp.smshelper.pdu_utils.PduHeaders;
import com.sp.smshelper.pdu_utils.PduPart;
import com.sp.smshelper.pdu_utils.SendReq;
import com.sp.smshelper.pdu_utils.Utils;
import com.sp.smshelper.receivesms.MmsSentReceiver;
import com.sp.smshelper.repository.BroadcastUtils;
import com.sp.smshelper.repository.PduPersister;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.MaybeSource;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SendMmsViewModel extends ViewModel {

    private static final String TAG = SendMmsViewModel.class.getSimpleName();
    private static final long DEFAULT_EXPIRY_TIME = 7 * 24 * 60 * 60;
    private static final int DEFAULT_PRIORITY = PduHeaders.PRIORITY_NORMAL;

    private Context mContext;
    private Intent mExplicitSentMmsReceiver;
    private int mSubscriptionId = -1;

    private MutableLiveData<List<SubscriptionInfo>> mMutableSimCardsList = new MutableLiveData<>();
    private static final String SMIL_TEXT_PART_FILENAME = "text_0.txt";
    private static final String SMIL_TEXT =
            "<smil>" +
                    "<head>" +
                    "<layout>" +
                    "<root-layout/>" +
                    "<region height=\"100%%\" id=\"Text\" left=\"0%%\" top=\"0%%\" width=\"100%%\"/>" +
                    "</layout>" +
                    "</head>" +
                    "<body>" +
                    "<par dur=\"8000ms\">" +
                    "<text src=\"%s\" region=\"Text\"/>" +
                    "</par>" +
                    "</body>" +
                    "</smil>";

    void setContext(Context context) {
        this.mContext = context;
    }

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
     * Returns subscription id
     *
     * @return Subscription id
     */
    int getSubscriptionId() {
        return mSubscriptionId;
    }

    /**
     * Sets the subscription id for fetching phone number
     *
     * @param mSubscriptionId Id
     */
    void setSubscriptionId(int mSubscriptionId) {
        this.mSubscriptionId = mSubscriptionId;
    }

    Disposable sendMmsMessage(String address, String subject, String messageBody, String filePath) {

        Log.d(TAG, "Address: " + address
                + " , Subject: " + subject
                + " , Message body: " + messageBody
                + " , File path: " + filePath
                + " ,Subscription id: " + mSubscriptionId);

        return Maybe.fromCallable(() -> {
            MmsSenderObject mmsSenderObject = new MmsSenderObject(messageBody, address);
            File file = new File(filePath);
            String mimeType = getMimeType(filePath);
            if (ContentType.isImageType(mimeType)) {//Image type
                mmsSenderObject.setImage(BitmapFactory.decodeFile(filePath));
            } else if (ContentType.isAudioType(mimeType)) {//Audio
                mmsSenderObject.addAudio(convertToByteArray(file), file.getName());
            } else if (ContentType.isVideoType(mimeType)) {//Video
                mmsSenderObject.addVideo(convertToByteArray(file), file.getName());
            } else {//Unspecified
                mmsSenderObject.addMedia(convertToByteArray(file), mimeType, file.getName());
            }
            mmsSenderObject.setSave(true);
            mmsSenderObject.setSubject(subject);
            return mmsSenderObject;
        }).flatMap((Function<MmsSenderObject, MaybeSource<Boolean>>) mmsSenderObject -> {
            boolean isSent;
            if (null != mmsSenderObject && checkMMS(mmsSenderObject)) {
                sendMmsMessage(mmsSenderObject.getText(), mmsSenderObject.getAddresses(),
                        mmsSenderObject.getImages(), mmsSenderObject.getImageNames(), mmsSenderObject.getParts(), mmsSenderObject.getSubject(),
                        mmsSenderObject.getSave());
                isSent = true;
            } else {
                Log.e(TAG, "Not a MMS message");
                isSent = false;
            }
            return Maybe.just(isSent);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> {
                    if (Boolean.FALSE.equals(status)) {
                        Toast.makeText(mContext, mContext.getText(R.string.mms_sending_failed), Toast.LENGTH_SHORT).show();
                    }
                }, error -> Log.e(TAG, "Error in sendMmsMessage(): " + error));
    }

    private void sendMmsMessage(String text, String[] addresses, Bitmap[] image,
                                String[] imageNames, List<MmsSenderObject.Part> parts, String subject, boolean save) {

        // create the parts to send
        ArrayList<MMSPart> data = new ArrayList<>();

        for (int i = 0; i < image.length; i++) {
            // turn bitmap into byte array to be stored
            byte[] imageBytes = MmsSenderObject.bitmapToByteArray(image[i]);

            MMSPart part = new MMSPart();
            part.MimeType = "image/jpeg";
            part.Name = (imageNames != null) ? imageNames[i] : ("image_" + System.currentTimeMillis());
            part.Data = imageBytes;
            data.add(part);
        }

        // add any extra media according to their mimeType set in the message
        //      eg. videos, audio, contact cards, location maybe?
        if (parts != null) {
            for (MmsSenderObject.Part p : parts) {
                MMSPart part = new MMSPart();
                if (p.getName() != null) {
                    part.Name = p.getName();
                } else {
                    part.Name = p.getContentType().split("/")[0];
                }
                part.MimeType = p.getContentType();
                part.Data = p.getMedia();
                data.add(part);
            }
        }

        if (text != null && !text.equals("")) {
            // add text to the end of the part and send
            MMSPart part = new MMSPart();
            part.Name = "text";
            part.MimeType = "text/plain";
            part.Data = text.getBytes();
            data.add(part);
        }

        sendMmsThroughSystem(mContext, subject, data, addresses, mExplicitSentMmsReceiver, save);
    }

    private void sendMmsThroughSystem(Context context, String subject, List<MMSPart> parts,
                                      String[] addresses, Intent explicitSentMmsReceiver, boolean save) {
        try {
            final String fileName = "send." + Math.abs(new Random().nextLong()) + ".dat";
            File mSendFile = new File(context.getCacheDir(), fileName);

            SendReq sendReq = buildPdu(context, addresses, subject, parts);

            Intent intent;
            if (explicitSentMmsReceiver == null) {
                intent = new Intent(MmsSentReceiver.MMS_SENT);
                BroadcastUtils.addClassName(context, intent, MmsSentReceiver.MMS_SENT);
            } else {
                intent = explicitSentMmsReceiver;
            }
            if (save) {
                PduPersister persister = PduPersister.getPduPersister(context);
                Uri messageUri = persister.persist(sendReq, Telephony.Mms.Outbox.CONTENT_URI,
                        true, true, null, mSubscriptionId);

                intent.putExtra(MmsSentReceiver.EXTRA_CONTENT_URI, messageUri.toString());
            }
            intent.putExtra(MmsSentReceiver.EXTRA_FILE_PATH, mSendFile.getPath());
            final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

            Uri writerUri = (new Uri.Builder())
                    .authority(context.getPackageName() + ".provider.MmsFileProvider")
                    .path(fileName)
                    .scheme(ContentResolver.SCHEME_CONTENT)
                    .build();
            FileOutputStream writer = null;
            Uri contentUri = null;
            try {
                writer = new FileOutputStream(mSendFile);
                writer.write(new PduComposer(context, sendReq).make());
                contentUri = writerUri;
            } catch (final IOException e) {
                Log.e(TAG, "Error writing send file", e);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                    }
                }
            }

            Bundle configOverrides = new Bundle();
            configOverrides.putBoolean(SmsManager.MMS_CONFIG_GROUP_MMS_ENABLED, true);
            String httpParams = MmsConfig.getHttpParams();
            if (!TextUtils.isEmpty(httpParams)) {
                configOverrides.putString(SmsManager.MMS_CONFIG_HTTP_PARAMS, httpParams);
            }
            configOverrides.putInt(SmsManager.MMS_CONFIG_MAX_MESSAGE_SIZE, MmsConfig.getMaxMessageSize());

            if (contentUri != null) {
                SmsManager.getSmsManagerForSubscriptionId(mSubscriptionId).sendMultimediaMessage(context,
                        contentUri, null, configOverrides, pendingIntent);
            } else {
                Log.e(TAG, "Error writing sending Mms");
                try {
                    pendingIntent.send(SmsManager.MMS_ERROR_IO_ERROR);
                } catch (PendingIntent.CanceledException ex) {
                    Log.e(TAG, "Mms pending intent cancelled?", ex);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "error using system sending method", e);
        }
    }

    private SendReq buildPdu(Context context, String[] recipients, String subject,
                             List<MMSPart> parts) {
        final SendReq req = new SendReq();
        // From, per spec
        req.prepareFromAddress(context, mSubscriptionId);
        // To
        for (String recipient : recipients) {
            req.addTo(new EncodedStringValue(recipient));
        }
        // Subject
        if (!TextUtils.isEmpty(subject)) {
            req.setSubject(new EncodedStringValue(subject));
        }
        // Date
        req.setDate(System.currentTimeMillis() / 1000);
        // Body
        PduBody body = new PduBody();
        // Add text part. Always add a smil part for compatibility, without it there
        // may be issues on some carriers/client apps
        int size = 0;
        for (int i = 0; i < parts.size(); i++) {
            MMSPart part = parts.get(i);
            size += addTextPart(body, part);
        }
        //NOTE: BELOW 2 LINE OF CODE ARE COMMITTED SO, ENTIRE CLASSES FROM smil_utils PACKAGE IS NOT REQUIRED

        // add a SMIL document for compatibility
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        SmilXmlSerializer.serialize(SmilHelper.createSmilDocument(body), out);
        final String smil = String.format(SMIL_TEXT, SMIL_TEXT_PART_FILENAME);
        PduPart smilPart = new PduPart();
        smilPart.setContentId("smil".getBytes());
        smilPart.setContentLocation("smil.xml".getBytes());
        smilPart.setContentType(ContentType.APP_SMIL.getBytes());
        smilPart.setData(smil.getBytes());
        body.addPart(0, smilPart);

        req.setBody(body);
        // Message size
        req.setMessageSize(size);
        // Message class
        req.setMessageClass(PduHeaders.MESSAGE_CLASS_PERSONAL_STR.getBytes());
        // Expiry
        req.setExpiry(DEFAULT_EXPIRY_TIME);
        try {
            // Priority
            req.setPriority(DEFAULT_PRIORITY);
            // Delivery report
            req.setDeliveryReport(PduHeaders.VALUE_NO);
            // Read report
            req.setReadReport(PduHeaders.VALUE_NO);
        } catch (InvalidHeaderValueException e) {
            Log.e(TAG, "InvalidHeaderValueException: " + e);
        }

        return req;
    }

    /**
     * Adds text part in MMS message
     *
     * @param pb Body object
     * @param p  MMS part
     * @return Length of created part
     */
    private int addTextPart(PduBody pb, MMSPart p) {
        String filename = p.Name;
        final PduPart part = new PduPart();
        // Set Charset if it's a text media.
        if (p.MimeType.startsWith("text")) {
            part.setCharset(CharacterSets.UTF_8);
        }
        // Set Content-Type.
        part.setContentType(p.MimeType.getBytes());
        // Set Content-Location.
        part.setContentLocation(filename.getBytes());
        int index = filename.lastIndexOf(".");
        String contentId = (index == -1) ? filename
                : filename.substring(0, index);
        part.setContentId(contentId.getBytes());
        part.setData(p.Data);
        pb.addPart(part);

        return part.getData().length;
    }

    /**
     * A method for checking whether or not a certain message will be sent as mms depending on its contents and the settings
     *
     * @param message is the message that you are checking against
     * @return true if the message will be mms, otherwise false
     */
    private boolean checkMMS(MmsSenderObject message) {
        return message.getImages().length != 0 ||
                (!message.getParts().isEmpty()) ||
                (message.getAddresses().length > 1) ||
                message.getSubject() != null;
    }

    /**
     * Fetches the mime type from file path.
     * URL can also be used
     *
     * @param filePath
     * @return Mime type of file
     */
    private String getMimeType(String filePath) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(removeWhiteSpaces(filePath));
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    /**
     * Converts file to byte array
     *
     * @param file file object
     * @return Byte array
     */
    private byte[] convertToByteArray(File file) {
        byte[] fileBytes = new byte[(int) file.length()];
        try (FileInputStream inputStream = new FileInputStream(file)) {
            inputStream.read(fileBytes);
        } catch (Exception ex) {
            Log.e(TAG, "Exception in convertUsingJavaNIO(): " + ex);
        }
        return fileBytes;
    }

    /**
     * Removes space characters present in the string
     *
     * @param name Name of the file
     * @return Name of the file w/o space characters
     */
    private String removeWhiteSpaces(String name) {
        return name.replaceAll("\\s+", "");
    }

    /**
     * Checks whether the cellular nework is available or not
     *
     * @param context Activity context
     * @return True if available, False otherwise
     */
    boolean isCellularAvailable(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String operatorName = telephonyManager.getNetworkOperatorName();
        Log.d(TAG, "#### isCellularAvailable(): NetworkOperatorName is: " + operatorName);
        if (operatorName.compareTo("") == 0) {
            Log.d(TAG, "#### isCellularAvailable(): NOPE");
            return false;
        } else {
            Log.d(TAG, "#### isCellularAvailable(): YES!");
            return true;
        }
    }

    /**
     * Checks if the mobile data is enabled or not
     *
     * @param context Activity context
     * @return True if available, False otherwise
     */
    public boolean isMobileDataEnabled(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return Utils.isDataEnabled(telephonyManager, getSubscriptionId());
    }
}
