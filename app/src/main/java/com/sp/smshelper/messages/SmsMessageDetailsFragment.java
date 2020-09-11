package com.sp.smshelper.messages;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.sp.smshelper.R;
import com.sp.smshelper.conversation.ConversationsActivity;
import com.sp.smshelper.conversation.ConversationsViewModel;
import com.sp.smshelper.databinding.FragmentSmsDetailsBinding;
import com.sp.smshelper.main.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class SmsMessageDetailsFragment extends BaseFragment {

    public static final String TAG = SmsMessageDetailsFragment.class.getSimpleName();
    private static final String BUNDLE_ARGS_MESSAGE_ID = "args_message_id";

    private FragmentSmsDetailsBinding mBinding;
    private ConversationsViewModel mViewModel;

    public static SmsMessageDetailsFragment newInstance(String messageId) {

        Bundle args = new Bundle();
        args.putString(BUNDLE_ARGS_MESSAGE_ID, messageId);
        SmsMessageDetailsFragment fragment = new SmsMessageDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_sms_details, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Set title
        getActivity().setTitle(R.string.message_details);

        mViewModel = ((ConversationsActivity) getActivity()).getViewModel();

        if (null != getArguments()) {
            String messageId = getArguments().getString(BUNDLE_ARGS_MESSAGE_ID);
            if (!TextUtils.isEmpty(messageId)) {
                getSmsMessageDetails(messageId);
            }
        }
    }

    private void getSmsMessageDetails(String messageId) {
        addToCompositeDisposable(mViewModel.getMessageDetailsById(messageId));
        //Observe on data
        mViewModel.watchSmsMessageDetails().observe(getViewLifecycleOwner(),
                messageDetails -> {
                    mBinding.setMessageDetails(messageDetails);
                });
    }
}
