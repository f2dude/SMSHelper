package com.sp.smshelper.mmsmessages;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.R;
import com.sp.smshelper.databinding.FragmentMmsMediaBinding;
import com.sp.smshelper.main.BaseFragment;
import com.sp.smshelper.readmms.MmsConversationActivity;
import com.sp.smshelper.readmms.MmsViewModel;
import com.sp.smshelper.views.SimpleDividerItemDecoration;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class MmsMediaFragment extends BaseFragment {

    public static final String TAG = MmsMediaFragment.class.getSimpleName();
    private static final String BUNDLE_ARGS_MESSAGE_ID = "args_message_id";

    private FragmentMmsMediaBinding mBinding;
    private MmsViewModel mViewModel;
    private MmsMediaAdapter mAdapter;
    private String mMessageId;

    public static MmsMediaFragment newInstance(String messageId) {

        Bundle args = new Bundle();
        args.putString(BUNDLE_ARGS_MESSAGE_ID, messageId);

        MmsMediaFragment fragment = new MmsMediaFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_mms_media, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Set title
        Objects.requireNonNull(getActivity()).setTitle(R.string.mms_media);

        mViewModel = ((MmsConversationActivity) getActivity()).getViewModel();

        if (null != getArguments()) {
            mMessageId = getArguments().getString(BUNDLE_ARGS_MESSAGE_ID);
            setupUi();

            if (!TextUtils.isEmpty(mMessageId)) {
                getMedia();
            }
        }
    }

    /**
     * Initializes Ui
     */
    private void setupUi() {
        RecyclerView recyclerView = mBinding.mmsDataList;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext(), R.drawable.item_divider));

        mAdapter = new MmsMediaAdapter(mMessageId);
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * Retrieves MMS media asscoiated with message id
     */
    private void getMedia() {
        addToCompositeDisposable(mViewModel.getMmsData(getContext(), mMessageId));
        mViewModel.watchMmsData().observe(getViewLifecycleOwner(), data -> mAdapter.setData(data));
    }
}
