package com.sp.smshelper.smil_utils;

import org.w3c.dom.DOMException;

/**
 * Controls the position, size and scaling of media object elements. See the
 * region element definition .
 */
public interface SMILRegionElement extends SMILElement, ElementLayout {
    /**
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getFit();

    void setFit(String fit)
            throws DOMException;

    /**
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    int getLeft();

    void setLeft(int top)
            throws DOMException;

    /**
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    int getTop();

    void setTop(int top)
            throws DOMException;

    /**
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    int getZIndex();

    void setZIndex(int zIndex)
            throws DOMException;

}
