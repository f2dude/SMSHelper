package com.sp.smshelper.mmsmessages;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.databinding.MmsMessageItemBinding;
import com.sp.smshelper.listeners.IListener;
import com.sp.smshelper.model.MmsMessage;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MmsMessagesAdapter extends RecyclerView.Adapter<MmsMessagesAdapter.MmsMessageViewHolder> {

    private List<MmsMessage> mMmsMessageList;
    private IListener.IMmsMessagesFragment mMmsMessageListener;
    private SparseBooleanArray mSelectedItems = new SparseBooleanArray();

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
     *
     * @return List of selected threads
     */
    Single<List<String>> getSelectedMessagesIds() {

        return Single.fromCallable(() -> {
            List<String> messageIdsList = new ArrayList<>(mSelectedItems.size());
            for (int i = 0; i < mSelectedItems.size(); i++) {
                messageIdsList.add(mMmsMessageList.get(mSelectedItems.keyAt(i)).getMessageId());
            }
            return messageIdsList;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
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
            mBinding.setIsSelected(mSelectedItems.get(getAdapterPosition(), false));
            mBinding.executePendingBindings();
        }
    }
}
