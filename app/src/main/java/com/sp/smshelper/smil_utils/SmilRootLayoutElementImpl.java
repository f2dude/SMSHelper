package com.sp.smshelper.smil_utils;

import org.w3c.dom.DOMException;

public class SmilRootLayoutElementImpl extends SmilElementImpl implements
        SMILRootLayoutElement {

    private static final String WIDTH_ATTRIBUTE_NAME = "width";
    private static final String HEIGHT_ATTRIBUTE_NAME = "height";
    private static final String BACKGROUND_COLOR_ATTRIBUTE_NAME = "backgroundColor";
    private static final String TITLE_ATTRIBUTE_NAME = "title";

    SmilRootLayoutElementImpl(SmilDocumentImpl owner, String tagName) {
        super(owner, tagName);
    }

    public String getBackgroundColor() {
        return this.getAttribute(BACKGROUND_COLOR_ATTRIBUTE_NAME);
    }

    public void setBackgroundColor(String backgroundColor) throws DOMException {
        this.setAttribute(BACKGROUND_COLOR_ATTRIBUTE_NAME, backgroundColor);
    }

    public int getHeight() {
        String heightString = this.getAttribute(HEIGHT_ATTRIBUTE_NAME);
        return parseAbsoluteLength(heightString);
    }

    public void setHeight(int height) throws DOMException {
        this.setAttribute(HEIGHT_ATTRIBUTE_NAME, height + "px");

    }

    public String getTitle() {
        return this.getAttribute(TITLE_ATTRIBUTE_NAME);
    }

    public void setTitle(String title) throws DOMException {
        this.setAttribute(TITLE_ATTRIBUTE_NAME, title);
    }

    public int getWidth() {
        String widthString = this.getAttribute(WIDTH_ATTRIBUTE_NAME);
        return parseAbsoluteLength(widthString);
    }

    public void setWidth(int width) throws DOMException {
        this.setAttribute(WIDTH_ATTRIBUTE_NAME, width + "px");
    }

    /*
     * Internal Interface
     */

    private int parseAbsoluteLength(String length) {
        if (length.endsWith("px")) {
            length = length.substring(0, length.indexOf("px"));
        }
        try {
            return Integer.parseInt(length);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
