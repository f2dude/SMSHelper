package com.sp.smshelper.smil_utils;

import org.w3c.dom.NodeList;

/**
 * Declares layout type for the document. See the  LAYOUT element definition .
 */
public interface SMILLayoutElement extends SMILElement {
    /**
     * The mime type of the layout langage used in this layout element.The
     * default value of the type attribute is "text/smil-basic-layout".
     */
    String getType();

    /**
     * <code>true</code> if the player can understand the mime type,
     * <code>false</code> otherwise.
     */
    boolean getResolved();

    /**
     * Returns the root layout element of this document.
     */
    SMILRootLayoutElement getRootLayout();

    /**
     * Return the region elements of this document.
     */
    NodeList getRegions();
}
