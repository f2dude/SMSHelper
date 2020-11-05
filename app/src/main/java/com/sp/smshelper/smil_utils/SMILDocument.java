package com.sp.smshelper.smil_utils;

import org.w3c.dom.Document;

/**
 * A SMIL document is the root of the SMIL Hierarchy and holds the entire
 * content. Beside providing access to the hierarchy, it also provides some
 * convenience methods for accessing certain sets of information from the
 * document.  Cover document timing, document locking?, linking modality and
 * any other document level issues. Are there issues with nested SMIL files?
 * Is it worth talking about different document scenarios, corresponding to
 * differing profiles? E.g. Standalone SMIL, HTML integration, etc.
 */
public interface SMILDocument extends Document, ElementSequentialTimeContainer {

    /**
     * Returns the element that contains the layout node of this document,
     * i.e. the <code>HEAD</code> element.
     */
    SMILElement getHead();

    /**
     * Returns the element that contains the par's of the document, i.e. the
     * <code>BODY</code> element.
     */
    SMILElement getBody();

    /**
     * Returns the element that contains the layout information of the presentation,
     * i.e. the <code>LAYOUT</code> element.
     */
    SMILLayoutElement getLayout();
}
