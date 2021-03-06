package com.sp.smshelper.model;

import java.util.List;

public class BaseModel {

    private String threadId;
    private String date;
    private String messageId;
    private boolean read;
    private String address;
    private boolean textOnly;
    private List<Data> dataList;
    private boolean locked;
    private boolean seen;
    private String subscriptionId;
    private List<String> addressList;

    public boolean isTextOnly() {
        return textOnly;
    }

    public void setTextOnly(boolean textOnly) {
        this.textOnly = textOnly;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
    }

    public List<Data> getDataList() {
        return dataList;
    }

    public void setDataList(List<Data> dataList) {
        this.dataList = dataList;
    }

    public String getContentType() {
        if (isTextOnly()) {
            return "Text Content";
        } else {
            return getDataList() != null && !getDataList().isEmpty() ? getDataList().get(getDataList().size() - 1).getContentType() : "NULL";
        }
    }

    public boolean getLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public List<String> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<String> addressList) {
        this.addressList = addressList;
    }

    public class Data {

        private String contentType;
        private String text;
        private String partId;

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getPartId() {
            return partId;
        }

        public void setPartId(String partId) {
            this.partId = partId;
        }
    }
}
