package com.sp.smshelper.smil_utils;

public class EventImpl implements Event {

    private final long mTimeStamp = System.currentTimeMillis();
    // Event type informations
    private String mEventType;
    private boolean mCanBubble;
    private boolean mCancelable;
    // Flags whether the event type information was set
    // FIXME: Can we use mEventType for this purpose?
    private boolean mInitialized;
    // Target of this event
    private EventTarget mTarget;
    // Event status variables
    private short mEventPhase;
    private boolean mStopPropagation;
    private boolean mPreventDefault;
    private EventTarget mCurrentTarget;
    private int mSeekTo;

    public boolean getBubbles() {
        return mCanBubble;
    }

    public boolean getCancelable() {
        return mCancelable;
    }

    public EventTarget getCurrentTarget() {
        return mCurrentTarget;
    }

    void setCurrentTarget(EventTarget currentTarget) {
        mCurrentTarget = currentTarget;
    }

    public short getEventPhase() {
        return mEventPhase;
    }

    void setEventPhase(short eventPhase) {
        mEventPhase = eventPhase;
    }

    public EventTarget getTarget() {
        return mTarget;
    }

    void setTarget(EventTarget target) {
        mTarget = target;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public String getType() {
        return mEventType;
    }

    public void initEvent(String eventTypeArg, boolean canBubbleArg,
                          boolean cancelableArg) {
        mEventType = eventTypeArg;
        mCanBubble = canBubbleArg;
        mCancelable = cancelableArg;
        mInitialized = true;
    }

    /*
     * Internal Interface
     */

    public void initEvent(String eventTypeArg, boolean canBubbleArg, boolean cancelableArg,
                          int seekTo) {
        mSeekTo = seekTo;
        initEvent(eventTypeArg, canBubbleArg, cancelableArg);
    }

    public void preventDefault() {
        mPreventDefault = true;
    }

    public void stopPropagation() {
        mStopPropagation = true;
    }

    boolean isInitialized() {
        return mInitialized;
    }

    boolean isPreventDefault() {
        return mPreventDefault;
    }

    boolean isPropogationStopped() {
        return mStopPropagation;
    }

    public int getSeekTo() {
        return mSeekTo;
    }
}
