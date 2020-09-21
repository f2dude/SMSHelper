package com.sp.smshelper.listeners;

import com.sp.smshelper.model.Conversation;

public interface IListener {

    interface IConversationsFragment {
        void onConversationItemClick(Conversation conversation, int position);
    }

    interface ISmsMessageFragment {
        void onSmsMessageItemClick(String messageId, int position);
    }
}
