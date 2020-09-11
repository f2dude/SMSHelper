package com.sp.smshelper.sendsms;

import android.telephony.SubscriptionInfo;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.databinding.OwnNumberListItemBinding;

import java.util.List;

public class NumbersAdapter extends RecyclerView.Adapter<NumbersAdapter.NumbersViewHolder> {

    private List<SubscriptionInfo> mSubscriptionInfoList;
    private int mSelectedPosition = -1;
    private MutableLiveData<Integer> mSubscriptionId = new MutableLiveData<>();

    @NonNull
    @Override
    public NumbersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        OwnNumberListItemBinding binding = OwnNumberListItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new NumbersViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NumbersViewHolder holder, int position) {
        holder.bind(mSubscriptionInfoList.get(position), position);
    }

    @Override
    public int getItemCount() {
        return mSubscriptionInfoList != null ? mSubscriptionInfoList.size() : 0;
    }

    public void setData(List<SubscriptionInfo> subscriptionInfoList) {
        this.mSubscriptionInfoList = subscriptionInfoList;
        notifyDataSetChanged();
    }

    public LiveData<Integer> watchSubscriptionIdData() {
        return mSubscriptionId;
    }

    public class NumbersViewHolder extends RecyclerView.ViewHolder {

        private OwnNumberListItemBinding mBinding;

        public NumbersViewHolder(@NonNull OwnNumberListItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        public void bind(SubscriptionInfo subscriptionInfo, int position) {
            mBinding.setInfo(subscriptionInfo);
            mBinding.setHolder(this);
            if (mSelectedPosition == position){
                mBinding.ownNumberRadioButton.setChecked(true);
            } else {
                mBinding.ownNumberRadioButton.setChecked(false);
            }
        }

        public void onItemClick(SubscriptionInfo subscriptionInfo) {
            mSelectedPosition = getAdapterPosition();
            mSubscriptionId.setValue(subscriptionInfo.getSubscriptionId());
            notifyDataSetChanged();
        }
    }
}
