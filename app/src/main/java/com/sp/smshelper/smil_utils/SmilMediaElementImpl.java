package com.sp.smshelper.smil_utils;

import android.util.Log;

import org.w3c.dom.DOMException;

public class SmilMediaElementImpl extends SmilElementImpl implements SMILMediaElement {

    public final static String SMIL_MEDIA_START_EVENT = "SmilMediaStart";
    public final static String SMIL_MEDIA_END_EVENT = "SmilMediaEnd";
    public final static String SMIL_MEDIA_PAUSE_EVENT = "SmilMediaPause";
    public final static String SMIL_MEDIA_SEEK_EVENT = "SmilMediaSeek";
    private final static String TAG = SmilMediaElementImpl.class.getSimpleName();
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = false;

    ElementTime mElementTime = new ElementTimeImpl(this) {
        private Event createEvent(String eventType) {
            DocumentEvent doc =
                    (DocumentEvent) SmilMediaElementImpl.this.getOwnerDocument();
            Event event = doc.createEvent("Event");
            event.initEvent(eventType, false, false);
            if (LOCAL_LOGV) {
                Log.d(TAG, "Dispatching 'begin' event to "
                        + SmilMediaElementImpl.this.getTagName() + " "
                        + SmilMediaElementImpl.this.getSrc() + " at "
                        + System.currentTimeMillis());
            }
            return event;
        }

        private Event createEvent(String eventType, int seekTo) {
            DocumentEvent doc =
                    (DocumentEvent) SmilMediaElementImpl.this.getOwnerDocument();
            EventImpl event = (EventImpl) doc.createEvent("Event");
            event.initEvent(eventType, false, false, seekTo);
            if (LOCAL_LOGV) {
                Log.d(TAG, "Dispatching 'begin' event to "
                        + SmilMediaElementImpl.this.getTagName() + " "
                        + SmilMediaElementImpl.this.getSrc() + " at "
                        + System.currentTimeMillis());
            }
            return event;
        }

        public boolean beginElement() {
            Event startEvent = createEvent(SMIL_MEDIA_START_EVENT);
            dispatchEvent(startEvent);
            return true;
        }

        public boolean endElement() {
            Event endEvent = createEvent(SMIL_MEDIA_END_EVENT);
            dispatchEvent(endEvent);
            return true;
        }

        public void resumeElement() {
            Event resumeEvent = createEvent(SMIL_MEDIA_START_EVENT);
            dispatchEvent(resumeEvent);
        }

        public void pauseElement() {
            Event pauseEvent = createEvent(SMIL_MEDIA_PAUSE_EVENT);
            dispatchEvent(pauseEvent);
        }

        public void seekElement(float seekTo) {
            Event seekEvent = createEvent(SMIL_MEDIA_SEEK_EVENT, (int) seekTo);
            dispatchEvent(seekEvent);
        }

        @Override
        public float getDur() {
            float dur = super.getDur();
            if (dur == 0) {
                // Duration is not specified, So get the implicit duration.
                String tag = getTagName();
                if (tag.equals("video") || tag.equals("audio")) {
                    // Continuous media
                    // FIXME Should get the duration of the media. "indefinite" instead here.
                    dur = -1.0F;
                } else if (tag.equals("text") || tag.equals("img")) {
                    // Discrete media
                    dur = 0;
                } else {
                    Log.d(TAG, "Unknown media type");
                }
            }
            return dur;
        }

        @Override
        ElementTime getParentElementTime() {
            return ((SmilParElementImpl) mSmilElement.getParentNode()).mParTimeContainer;
        }
    };

    /*
     * Internal Interface
     */

    SmilMediaElementImpl(SmilDocumentImpl owner, String tagName) {
        super(owner, tagName);
    }

    /*
     * SMILMediaElement Interface
     */

    public String getAbstractAttr() {
        return this.getAttribute("abstract");
    }

    public void setAbstractAttr(String abstractAttr) throws DOMException {
        this.setAttribute("abstract", abstractAttr);
    }

    public String getAlt() {
        return this.getAttribute("alt");
    }

    public void setAlt(String alt) throws DOMException {
        this.setAttribute("alt", alt);
    }

    public String getAuthor() {
        return this.getAttribute("author");
    }

    public void setAuthor(String author) throws DOMException {
        this.setAttribute("author", author);
    }

    public String getClipBegin() {
        return this.getAttribute("clipBegin");
    }

    public void setClipBegin(String clipBegin) throws DOMException {
        this.setAttribute("clipBegin", clipBegin);
    }

    public String getClipEnd() {
        return this.getAttribute("clipEnd");
    }

    public void setClipEnd(String clipEnd) throws DOMException {
        this.setAttribute("clipEnd", clipEnd);
    }

    public String getCopyright() {
        return this.getAttribute("copyright");
    }

    public void setCopyright(String copyright) throws DOMException {
        this.setAttribute("copyright", copyright);
    }

    public String getLongdesc() {
        return this.getAttribute("longdesc");
    }

    public void setLongdesc(String longdesc) throws DOMException {
        this.setAttribute("longdesc", longdesc);

    }

    public String getPort() {
        return this.getAttribute("port");
    }

    public void setPort(String port) throws DOMException {
        this.setAttribute("port", port);
    }

    public String getReadIndex() {
        return this.getAttribute("readIndex");
    }

    public void setReadIndex(String readIndex) throws DOMException {
        this.setAttribute("readIndex", readIndex);
    }

    public String getRtpformat() {
        return this.getAttribute("rtpformat");
    }

    public void setRtpformat(String rtpformat) throws DOMException {
        this.setAttribute("rtpformat", rtpformat);
    }

    public String getSrc() {
        return this.getAttribute("src");
    }

    public void setSrc(String src) throws DOMException {
        this.setAttribute("src", src);
    }

    public String getStripRepeat() {
        return this.getAttribute("stripRepeat");
    }

    public void setStripRepeat(String stripRepeat) throws DOMException {
        this.setAttribute("stripRepeat", stripRepeat);
    }

    public String getTitle() {
        return this.getAttribute("title");
    }

    public void setTitle(String title) throws DOMException {
        this.setAttribute("title", title);
    }

    public String getTransport() {
        return this.getAttribute("transport");
    }

    public void setTransport(String transport) throws DOMException {
        this.setAttribute("transport", transport);
    }

    public String getType() {
        return this.getAttribute("type");
    }

    public void setType(String type) throws DOMException {
        this.setAttribute("type", type);
    }

    /*
     * TimeElement Interface
     */

    public boolean beginElement() {
        return mElementTime.beginElement();
    }

    public boolean endElement() {
        return mElementTime.endElement();
    }

    public TimeList getBegin() {
        return mElementTime.getBegin();
    }

    public void setBegin(TimeList begin) throws DOMException {
        mElementTime.setBegin(begin);
    }

    public float getDur() {
        return mElementTime.getDur();
    }

    public void setDur(float dur) throws DOMException {
        mElementTime.setDur(dur);
    }

    public TimeList getEnd() {
        return mElementTime.getEnd();
    }

    public void setEnd(TimeList end) throws DOMException {
        mElementTime.setEnd(end);
    }

    public short getFill() {
        return mElementTime.getFill();
    }

    public void setFill(short fill) throws DOMException {
        mElementTime.setFill(fill);
    }

    public short getFillDefault() {
        return mElementTime.getFillDefault();
    }

    public void setFillDefault(short fillDefault) throws DOMException {
        mElementTime.setFillDefault(fillDefault);
    }

    public float getRepeatCount() {
        return mElementTime.getRepeatCount();
    }

    public void setRepeatCount(float repeatCount) throws DOMException {
        mElementTime.setRepeatCount(repeatCount);
    }

    public float getRepeatDur() {
        return mElementTime.getRepeatDur();
    }

    public void setRepeatDur(float repeatDur) throws DOMException {
        mElementTime.setRepeatDur(repeatDur);
    }

    public short getRestart() {
        return mElementTime.getRestart();
    }

    public void setRestart(short restart) throws DOMException {
        mElementTime.setRestart(restart);
    }

    public void pauseElement() {
        mElementTime.pauseElement();
    }

    public void resumeElement() {
        mElementTime.resumeElement();
    }

    public void seekElement(float seekTo) {
        mElementTime.seekElement(seekTo);
    }
}
