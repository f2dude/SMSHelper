package com.sp.smshelper.messages;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.databinding.SmsMessageItemBinding;
import com.sp.smshelper.listeners.IListener;
import com.sp.smshelper.model.SmsMessage;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class SmsMessagesAdapter extends RecyclerView.Adapter<SmsMessagesAdapter.SmsMessagesViewHolder> {

    private List<SmsMessage> mSmsMessageList;
    private IListener.ISmsMessageFragment mSmsMessagesListener;
    private SparseBooleanArray mSelectedItems = new SparseBooleanArray();

    SmsMessagesAdapter(IListener.ISmsMessageFragment smsMessagesListener) {
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

    void setData(List<SmsMessage> smsMessageList) {
        this.mSmsMessageList = smsMessageList;
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
    Single<List<String>> getSelectedMessagesIds() {

        return Single.fromCallable(() -> {
            List<String> messageIdsList = new ArrayList<>(mSelectedItems.size());
            for (int i = 0; i < mSelectedItems.size(); i++) {
                messageIdsList.add(mSmsMessageList.get(mSelectedItems.keyAt(i)).getMessageId());
            }
            return messageIdsList;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    class SmsMessagesViewHolder extends RecyclerView.ViewHolder {

        private SmsMessageItemBinding mBinding;

        SmsMessagesViewHolder(@NonNull SmsMessageItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        void bind(SmsMessage smsMessage) {
            mBinding.setMessage(smsMessage);
            mBinding.setSmsMessagesListener(mSmsMessagesListener);
            mBinding.setPosition(getAdapterPosition());
            mBinding.setIsSelected(mSelectedItems.get(getAdapterPosition(), false));
            mBinding.executePendingBindings();
        }
    }
}
