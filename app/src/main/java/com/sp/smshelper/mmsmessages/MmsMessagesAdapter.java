package com.sp.smshelper.mmsmessages;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.databinding.MmsMessageItemBinding;
import com.sp.smshelper.listeners.IListener;
import com.sp.smshelper.model.MmsMessage;

import java.util.List;

public class MmsMessagesAdapter extends RecyclerView.Adapter<MmsMessagesAdapter.MmsMessageViewHolder> {

    private List<MmsMessage> mMmsMessageList;
    private IListener.IMmsMessagesFragment mMmsMessageListener;

    MmsMessagesAdapter(IListener.IMmsMessagesFragment mmsMessageListener) {
        this.mMmsMessageListener = mmsMessageListener;
    }

    @NonNull
    @Override
    public MmsMessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MmsMessageItemBinding binding = MmsMessageItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MmsMessageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MmsMessageViewHolder holder, int position) {
        holder.bind(mMmsMessageList.get(position));
    }

    @Override
    public int getItemCount() {
        return mMmsMessageList != null ? mMmsMessageList.size() : 0;
    }

    void setData(List<MmsMessage> mmsMessageList) {
        this.mMmsMessageList = mmsMessageList;
        notifyDataSetChanged();
    }

    class MmsMessageViewHolder extends RecyclerView.ViewHolder {

        private MmsMessageItemBinding mBinding;

        MmsMessageViewHolder(@NonNull MmsMessageItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        void bind(MmsMessage mmsMessage) {
            mBinding.setMmsMessage(mmsMessage);
            mBinding.setMmsMessageListener(mMmsMessageListener);
            mBinding.setPosition(getAdapterPosition());
            mBinding.setIsSelected(false);
            mBinding.executePendingBindings();
        }
    }
}
