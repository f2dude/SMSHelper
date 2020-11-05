package com.sp.smshelper.readmms;

import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.databinding.MmsConversationItemBinding;
import com.sp.smshelper.listeners.IListener;
import com.sp.smshelper.model.MmsConversation;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MmsConversationsAdapter extends RecyclerView.Adapter<MmsConversationsAdapter.MmsConversationViewHolder> {

    private List<MmsConversation> mmsConversationList;
    private IListener.IMmsConversationFragment mListener;
    private SparseBooleanArray mSelectedItems = new SparseBooleanArray();

    public MmsConversationsAdapter(IListener.IMmsConversationFragment listener) {
        this.mListener = listener;
    }

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
    Single<List<String>> getSelectedThreadIds() {

        return Single.fromCallable(() -> {
            List<String> threadIdsList = new ArrayList<>(mSelectedItems.size());
            for (int i = 0; i < mSelectedItems.size(); i++) {
                threadIdsList.add(mmsConversationList.get(mSelectedItems.keyAt(i)).getThreadId());
            }
            return threadIdsList;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    class MmsConversationViewHolder extends RecyclerView.ViewHolder {

        private MmsConversationItemBinding mBinding;

        MmsConversationViewHolder(@NonNull MmsConversationItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        void bind(MmsConversation mmsConversation) {
            mBinding.setMmsConversation(mmsConversation);
            mBinding.setMmsConversationListener(mListener);
            mBinding.setPosition(getAdapterPosition());
            mBinding.setIsSelected(mSelectedItems.get(getAdapterPosition(), false));
            mBinding.executePendingBindings();
        }
    }
}
