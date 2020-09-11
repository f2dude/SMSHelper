package com.sp.smshelper.messages;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.listeners.IListener;
import com.sp.smshelper.databinding.SmsMessageItemBinding;
import com.sp.smshelper.model.SmsMessage;

import java.util.List;

public class SmsMessagesAdapter extends RecyclerView.Adapter<SmsMessagesAdapter.SmsMessagesViewHolder> {

    private List<SmsMessage> mSmsMessageList;
    private IListener.ISmsMessageFragment mSmsMessagesListener;

    public SmsMessagesAdapter(IListener.ISmsMessageFragment smsMessagesListener) {
        this.mSmsMessagesListener = smsMessagesListener;
    }

    @NonNull
    @Override
    public SmsMessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        SmsMessageItemBinding binding = SmsMessageItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new SmsMessagesViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull SmsMessagesViewHolder holder, int position) {
        holder.bind(mSmsMessageList.get(position));
    }

    @Override
    public int getItemCount() {
        return mSmsMessageList != null ? mSmsMessageList.size() : 0;
    }

    protected void setData(List<SmsMessage> smsMessageList) {
        this.mSmsMessageList = smsMessageList;
        notifyDataSetChanged();
    }

    public class SmsMessagesViewHolder extends RecyclerView.ViewHolder {

        private SmsMessageItemBinding mBinding;

        public SmsMessagesViewHolder(@NonNull SmsMessageItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(SmsMessage smsMessage) {
            mBinding.setMessage(smsMessage);
            mBinding.setSmsMessagesListener(mSmsMessagesListener);
            mBinding.executePendingBindings();
        }
    }
}
