package com.sp.smshelper.smil_utils;

import org.w3c.dom.DOMException;

/**
 * This interface is used by SMIL elements root-layout, top-layout and region.
 */
public interface ElementLayout {
    /**
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getTitle();

    void setTitle(String title)
            throws DOMException;

    /**
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getBackgroundColor();

    void setBackgroundColor(String backgroundColor)
            throws DOMException;

    /**
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    int getHeight();

    void setHeight(int height)
            throws DOMException;

    /**
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    int getWidth();

    void setWidth(int width)
            throws DOMException;

}
