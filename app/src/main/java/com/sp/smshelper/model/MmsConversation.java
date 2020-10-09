package com.sp.smshelper.model;

import java.util.List;

public class MmsConversation {

    public enum MessageType {
        ALL, DRAFT, FAILED, INBOX, OUTBOX, SENT
    }

    private String threadId;
    private String date;
    private boolean read;
    private String contentType;
    private boolean textOnly;
    private String text;
    private List<String> addressList;
    private Data data;
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

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public boolean isTextOnly() {
        return textOnly;
    }

    public void setTextOnly(boolean textOnly) {
        this.textOnly = textOnly;
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

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public MessageType getMessageBoxType() {
        return messageBoxType;
    }

    public void setMessageBoxType(MessageType messageBoxType) {
        this.messageBoxType = messageBoxType;
    }

    public class Data {

        private String dataPath;
        private String contentType;

        public String getDataPath() {
            return dataPath;
        }

        public void setDataPath(String dataPath) {
            this.dataPath = dataPath;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }
    }
}
