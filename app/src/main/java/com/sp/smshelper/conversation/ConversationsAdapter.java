package com.sp.smshelper.conversation;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.databinding.ConversationItemBinding;
import com.sp.smshelper.listeners.IListener;
import com.sp.smshelper.model.Conversation;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationsViewHolder> {

    private List<Conversation> mConversations;
    private IListener.IConversationsFragment mConversationsListener;
    private SparseBooleanArray mSelectedItems = new SparseBooleanArray();

    ConversationsAdapter(IListener.IConversationsFragment conversationsListener) {
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

    void setData(List<Conversation> conversationList) {
        this.mConversations = conversationList;
        notifyDataSetChanged();
    }

    /**
     * Item selection
     *
     * @param position of item
     */
    void toggleSelection(int position) {
        if (mSelectedItems.get(position, false)) {
            mSelectedItems.delete(position);
        } else {
            mSelectedItems.put(position, true);
        }
        notifyItemChanged(position);
    }

    /**
     * Clears the selection
     */
    void clearSelections() {
        mSelectedItems.clear();
        notifyDataSetChanged();
    }

    /**
     * Returns the selected items
     *
     * @return Size of selected items
     */
    int getSelectedItemsSize() {
        return mSelectedItems.size();
    }

    /**
     * Returns the selected threads
     * @return List of selected threads
     */
    Single<List<String>> getSelectedThreadIds() {

        return Single.fromCallable(() -> {
            List<String> threadIdsList = new ArrayList<>(mSelectedItems.size());
            for (int i = 0; i < mSelectedItems.size(); i++) {
                threadIdsList.add(mConversations.get(mSelectedItems.keyAt(i)).getThreadId());
            }
            return threadIdsList;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    class ConversationsViewHolder extends RecyclerView.ViewHolder {

        private ConversationItemBinding mBinding;

        public ConversationsViewHolder(@NonNull ConversationItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(Conversation conversation) {
            mBinding.setConversation(conversation);
            mBinding.setConversationsListener(mConversationsListener);
            mBinding.setPosition(getAdapterPosition());
            mBinding.setIsSelected(mSelectedItems.get(getAdapterPosition(), false));
            mBinding.executePendingBindings();
        }
    }
}
