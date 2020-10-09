package com.sp.smshelper.readmms;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.databinding.MmsConversationItemBinding;
import com.sp.smshelper.model.MmsConversation;

import java.util.List;

public class MmsConversationsAdapter extends RecyclerView.Adapter<MmsConversationsAdapter.MmsConversationViewHolder> {

    private List<MmsConversation> mmsConversationList;

    @NonNull
    @Override
    public MmsConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MmsConversationItemBinding binding = MmsConversationItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MmsConversationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MmsConversationViewHolder holder, int position) {
        holder.bind(mmsConversationList.get(position));
    }

    @Override
    public int getItemCount() {
        return mmsConversationList != null ? mmsConversationList.size() : 0;
    }

    public void setData(List<MmsConversation> conversationList) {
        this.mmsConversationList = conversationList;
        notifyDataSetChanged();
    }

    static class MmsConversationViewHolder extends RecyclerView.ViewHolder {

        private MmsConversationItemBinding mBinding;

        MmsConversationViewHolder(@NonNull MmsConversationItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        void bind(MmsConversation mmsConversation) {
            mBinding.setMmsConversation(mmsConversation);
            mBinding.executePendingBindings();
        }
    }
}
