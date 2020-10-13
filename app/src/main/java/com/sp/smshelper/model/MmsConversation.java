package com.sp.smshelper.model;

import java.util.List;

public class MmsConversation extends BaseModel{

    public enum MessageType {
        ALL, DRAFT, FAILED, INBOX, OUTBOX, SENT
    }

    private String threadId;
    private String date;
    private String text;
    private List<String> addressList;
    private MessageType messageBoxType;

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getAddressList() {
        return addressList;
    }

    public String getAddressString() {
        StringBuilder sb = new StringBuilder();
        if (addressList != null && !addressList.isEmpty()) {
            int size = addressList.size();
            for (int i = 0; i < size; i++) {
                sb.append(addressList.get(i));
                if (!((i +1) == size)) {
                    sb.append(", ");
                }
            }
        }
        return sb.toString();
    }

    public void setAddressList(List<String> addressList) {
        this.addressList = addressList;
    }

    public MessageType getMessageBoxType() {
        return messageBoxType;
    }

    public void setMessageBoxType(MessageType messageBoxType) {
        this.messageBoxType = messageBoxType;
    }
}
