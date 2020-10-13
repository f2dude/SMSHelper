package com.sp.smshelper.model;

public class Conversation extends  BaseModel{


    private String snippet;
    private String address;

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
