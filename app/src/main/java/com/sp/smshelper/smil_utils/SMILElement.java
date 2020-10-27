package com.sp.smshelper.smil_utils;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * The <code>SMILElement</code> interface is the base for all SMIL element
 * types. It follows the model of the <code>HTMLElement</code> in the HTML
 * DOM, extending the base <code>Element</code> class to denote SMIL-specific
 * elements.
 * <p> Note that the <code>SMILElement</code> interface overlaps with the
 * <code>HTMLElement</code> interface. In practice, an integrated document
 * profile that include HTML and SMIL modules will effectively implement both
 * interfaces (see also the DOM documentation discussion of  Inheritance vs
 * Flattened Views of the API ).  // etc. This needs attention
 */
public interface SMILElement extends Element {
    /**
     * The unique id.
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getId();

    void setId(String id)
            throws DOMException;

}
