package com.sp.smshelper.model;

public class MmsConversation extends BaseModel{

    public enum MessageType {
        ALL, DRAFT, FAILED, INBOX, OUTBOX, SENT
    }

    private String threadId;
    private String date;
    private String text;
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

    public MessageType getMessageBoxType() {
        return messageBoxType;
    }

    public void setMessageBoxType(MessageType messageBoxType) {
        this.messageBoxType = messageBoxType;
    }

    public String getAddressString() {
        StringBuilder sb = new StringBuilder();
        if (getAddressList() != null && !getAddressList().isEmpty()) {
            int size = getAddressList().size();
            for (int i = 0; i < size; i++) {
                sb.append(getAddressList().get(i));
                if ((i + 1) != size) {
                    sb.append(", ");
                }
            }
        }
        return sb.toString();
    }
}
