package com.sp.smshelper.mmsmessages;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.databinding.MmsMediaItemBinding;
import com.sp.smshelper.model.BaseModel;

import java.util.List;

public class MmsMediaAdapter extends RecyclerView.Adapter<MmsMediaAdapter.MmsMediaViewHolder> {

    private List<BaseModel.Data> mDataList;

    @NonNull
    @Override
    public MmsMediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MmsMediaItemBinding binding = MmsMediaItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MmsMediaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MmsMediaViewHolder holder, int position) {
        holder.bind(mDataList.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataList != null ? mDataList.size() : 0;
    }

    void setData(List<BaseModel.Data> dataList) {
        this.mDataList = dataList;
        notifyDataSetChanged();
    }

    static class MmsMediaViewHolder extends RecyclerView.ViewHolder {

        private MmsMediaItemBinding mBinding;

        MmsMediaViewHolder(@NonNull MmsMediaItemBinding binding) {
            super(binding.getRoot());
            this.mBinding = binding;
        }

        void bind(BaseModel.Data data) {
            mBinding.setPartId(data.getPartId());
            mBinding.setText(data.getText());
            mBinding.setContentType(data.getContentType());
        }
    }
}
