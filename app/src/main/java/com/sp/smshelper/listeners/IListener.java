package com.sp.smshelper.listeners;

import com.sp.smshelper.model.Conversation;
import com.sp.smshelper.model.MmsConversation;

public interface IListener {

    interface IConversationsFragment {
        void onConversationItemClick(Conversation conversation, int position);
    }

    interface ISmsMessageFragment {
        void onSmsMessageItemClick(String messageId, int position);
    }

    interface IMmsConversationFragment {
        void onMmsConversationItemClick(MmsConversation mmsConversation, int position);
    }

    interface IMmsMessagesFragment {
        void onMmsMessageItemClick(String messageId, int position);
    }
}
