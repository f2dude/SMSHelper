package com.sp.smshelper.model;

import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.sp.smshelper.R;
import com.sp.smshelper.pdu_utils.ContentType;
import com.sp.smshelper.repository.MmsRepository;

public class MmsMessage extends BaseModel {

    private String text;
    private MmsConversation.MessageType messageBoxType;
    private String messageContentType;//Do not use this for finding out media type.
    private String deliveryTime;
    private String dateSent;
    private String contentClass;
    private String contentLocation;
    private String creator;
    private String deliveryReport;
    private String expiry;
    private String messageClass;
    private String mmsMessageId;
    private String messageSize;
    private String messageType;
    private String mmsVersion;
    private String priority;
    private boolean readReport;
    private String readStatus;
    private boolean reportAllowed;
    private String responseStatus;
    private String responseText;
    private String retrieveStatus;
    private String retrieveText;
    private String retrieveTextCharset;
    private String status;
    private String subject;
    private String subjectCharset;
    private String transactionId;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public MmsConversation.MessageType getMessageBoxType() {
        return messageBoxType;
    }

    public void setMessageBoxType(MmsConversation.MessageType messageBoxType) {
        this.messageBoxType = messageBoxType;
    }

    public String getMessageContentType() {
        return messageContentType;
    }

    public void setMessageContentType(String messageContentType) {
        this.messageContentType = messageContentType;
    }

    public String getDeliveryTime() {
        return deliveryTime;
    }

    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }

    public String getDateSent() {
        return dateSent;
    }

    public void setDateSent(String dateSent) {
        this.dateSent = dateSent;
    }

    public String getContentClass() {
        return contentClass;
    }

    public void setContentClass(String contentClass) {
        this.contentClass = contentClass;
    }

    public String getContentLocation() {
        return contentLocation;
    }

    public void setContentLocation(String contentLocation) {
        this.contentLocation = contentLocation;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getDeliveryReport() {
        return deliveryReport;
    }

    public void setDeliveryReport(String deliveryReport) {
        this.deliveryReport = deliveryReport;
    }

    public String getExpiry() {
        return expiry;
    }

    public void setExpiry(String expiry) {
        this.expiry = expiry;
    }

    public String getMessageClass() {
        return messageClass;
    }

    public void setMessageClass(String messageClass) {
        this.messageClass = messageClass;
    }

    public String getMmsMessageId() {
        return mmsMessageId;
    }

    public void setMmsMessageId(String messageId) {
        this.mmsMessageId = messageId;
    }

    public String getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(String messageSize) {
        this.messageSize = messageSize;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMmsVersion() {
        return mmsVersion;
    }

    public void setMmsVersion(String mmsVersion) {
        this.mmsVersion = mmsVersion;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isReadReport() {
        return readReport;
    }

    public void setReadReport(boolean readReport) {
        this.readReport = readReport;
    }

    public boolean isReportAllowed() {
        return reportAllowed;
    }

    public void setReportAllowed(boolean reportAllowed) {
        this.reportAllowed = reportAllowed;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseText() {
        return responseText;
    }

    public void setResponseText(String responseText) {
        this.responseText = responseText;
    }

    public String getRetrieveStatus() {
        return retrieveStatus;
    }

    public void setRetrieveStatus(String retrieveStatus) {
        this.retrieveStatus = retrieveStatus;
    }

    public String getRetrieveText() {
        return retrieveText;
    }

    public void setRetrieveText(String retrieveText) {
        this.retrieveText = retrieveText;
    }

    public String getRetrieveTextCharset() {
        return retrieveTextCharset;
    }

    public void setRetrieveTextCharset(String retrieveTextCharset) {
        this.retrieveTextCharset = retrieveTextCharset;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubjectCharset() {
        return subjectCharset;
    }

    public void setSubjectCharset(String subjectCharset) {
        this.subjectCharset = subjectCharset;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }

    public String getFromAddressString() {
        StringBuilder sb = new StringBuilder();
        if (getAddressList() != null && !getAddressList().isEmpty()) {
            for (int i = 0; i < getAddressList().size(); i++) {
                if (getAddressList().get(i).contains("From")) {
                    sb.append(getAddressList().get(i));
                }
            }
        }
        return !TextUtils.isEmpty(sb.toString()) ? sb.toString() : "From: Self";
    }

    @BindingAdapter({"partId", "contentType"})
    public static void loadImage(ImageView imageView, String partId, String contentType) {
        if (!TextUtils.isEmpty(partId)) {
            try {
                if (ContentType.isImageType(contentType)) {
                    MmsRepository mmsRepository = new MmsRepository();
                    Glide.with(imageView.getContext())
                            .load(mmsRepository.extractImage(imageView.getContext(), partId))
                            .into(imageView);
                } else {
                    imageView.setImageResource(R.drawable.attachment);
                }
            } catch (Exception e) {
                Log.e("MmsMessage", "Exception in loadImage(): " + e);
            }
        } else {
            imageView.setVisibility(View.GONE);
        }
    }
}
