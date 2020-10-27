package com.sp.smshelper.smil_utils;

import org.w3c.dom.DOMException;

/**
 * Declares media content.
 */
public interface SMILMediaElement extends ElementTime, SMILElement {
    /**
     * See the  abstract attribute from  .
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getAbstractAttr();

    void setAbstractAttr(String abstractAttr)
            throws DOMException;

    /**
     * See the  alt attribute from  .
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getAlt();

    void setAlt(String alt)
            throws DOMException;

    /**
     * See the  author attribute from  .
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getAuthor();

    void setAuthor(String author)
            throws DOMException;

    /**
     * See the  clipBegin attribute from  .
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getClipBegin();

    void setClipBegin(String clipBegin)
            throws DOMException;

    /**
     * See the  clipEnd attribute from  .
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getClipEnd();

    void setClipEnd(String clipEnd)
            throws DOMException;

    /**
     * See the  copyright attribute from  .
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getCopyright();

    void setCopyright(String copyright)
            throws DOMException;

    /**
     * See the  longdesc attribute from  .
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getLongdesc();

    void setLongdesc(String longdesc)
            throws DOMException;

    /**
     * See the  port attribute from  .
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getPort();

    void setPort(String port)
            throws DOMException;

    /**
     * See the  readIndex attribute from  .
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getReadIndex();

    void setReadIndex(String readIndex)
            throws DOMException;

    /**
     * See the  rtpformat attribute from  .
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getRtpformat();

    void setRtpformat(String rtpformat)
            throws DOMException;

    /**
     * See the  src attribute from  .
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getSrc();

    void setSrc(String src)
            throws DOMException;

    /**
     * See the  stripRepeat attribute from  .
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getStripRepeat();

    void setStripRepeat(String stripRepeat)
            throws DOMException;

    /**
     * See the  title attribute from  .
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getTitle();

    void setTitle(String title)
            throws DOMException;

    /**
     * See the  transport attribute from  .
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getTransport();

    void setTransport(String transport)
            throws DOMException;

    /**
     * See the  type attribute from  .
     *
     * @throws DOMException NO_MODIFICATION_ALLOWED_ERR: Raised if this attribute is readonly.
     */
    String getType();

    void setType(String type)
            throws DOMException;

}
