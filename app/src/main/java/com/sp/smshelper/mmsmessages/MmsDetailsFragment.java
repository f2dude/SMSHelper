package com.sp.smshelper.mmsmessages;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sp.smshelper.R;
import com.sp.smshelper.databinding.FragmentMmsDetailsBinding;
import com.sp.smshelper.main.BaseFragment;
import com.sp.smshelper.readmms.MmsConversationActivity;
import com.sp.smshelper.readmms.MmsViewModel;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class MmsDetailsFragment extends BaseFragment {

    public static final String TAG = MmsDetailsFragment.class.getSimpleName();
    private static final String BUNDLE_ARGS_MESSAGE_ID = "args_message_id";

    private FragmentMmsDetailsBinding mBinding;
    private String mMessageId;
    private MmsViewModel mViewModel;

    public static MmsDetailsFragment newInstance(String messageId) {
        
        Bundle args = new Bundle();
        args.putString(BUNDLE_ARGS_MESSAGE_ID, messageId);
        
        MmsDetailsFragment fragment = new MmsDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_mms_details, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Set title
        Objects.requireNonNull(getActivity()).setTitle(R.string.mms_details);

        mViewModel = ((MmsConversationActivity) getActivity()).getViewModel();

        if (null != getArguments()) {
            mMessageId = getArguments().getString(BUNDLE_ARGS_MESSAGE_ID);
            if (!TextUtils.isEmpty(mMessageId)) {
                getMmsMessageDetails();
            }
        }
    }

    /**
     * Returns MMS message and displays on screen
     */
    private void getMmsMessageDetails() {
        addToCompositeDisposable(mViewModel.getMmsMessageByMessageId(mMessageId));
        //Observe on data
        mViewModel.watchMmsMessageDetails().observe(getViewLifecycleOwner(), mmsMessage -> mBinding.mmsMessage.setText(mmsMessage));
    }
}
