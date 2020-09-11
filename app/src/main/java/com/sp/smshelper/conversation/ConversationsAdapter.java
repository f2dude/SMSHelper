package com.sp.smshelper.conversation;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.databinding.ConversationItemBinding;
import com.sp.smshelper.listeners.IListener;
import com.sp.smshelper.model.Conversation;

import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationsViewHolder> {

    private static final String TAG = ConversationsAdapter.class.getSimpleName();

    private List<Conversation> mConversations;
    private IListener.IConversationsFragment mConversationsListener;

    public ConversationsAdapter(IListener.IConversationsFragment conversationsListener) {
        this.mConversationsListener = conversationsListener;
    }

    @NonNull
    @Override
    public ConversationsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConversationItemBinding binding = ConversationItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ConversationsViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationsViewHolder holder, int position) {
        holder.bind(mConversations.get(position));
    }

    @Override
    public int getItemCount() {
        return mConversations != null ? mConversations.size() : 0;
    }

    protected void setData(List<Conversation> conversationList) {
        this.mConversations = conversationList;
        notifyDataSetChanged();
    }

    public class ConversationsViewHolder extends RecyclerView.ViewHolder {

        private ConversationItemBinding mBinding;

        public ConversationsViewHolder(@NonNull ConversationItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(Conversation conversation) {
            mBinding.setConversation(conversation);
            mBinding.setConversationsListener(mConversationsListener);
            mBinding.executePendingBindings();
        }
    }
}
