package com.sp.smshelper.model;

public class MmsMessage extends BaseModel{

    private String text;
    private MmsConversation.MessageType messageBoxType;
    private MmsConversation.Data data;

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

    public MmsConversation.Data getData() {
        return data;
    }

    public void setData(MmsConversation.Data data) {
        this.data = data;
    }
}
