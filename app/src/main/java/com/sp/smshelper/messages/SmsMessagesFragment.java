package com.sp.smshelper.messages;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.R;
import com.sp.smshelper.conversation.ConversationsActivity;
import com.sp.smshelper.conversation.ConversationsViewModel;
import com.sp.smshelper.databinding.FragmentSmsMessagesBinding;
import com.sp.smshelper.listeners.IListener;
import com.sp.smshelper.main.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class SmsMessagesFragment extends BaseFragment implements IListener.ISmsMessageFragment {

    public static final String TAG = SmsMessagesFragment.class.getSimpleName();
    private static final String BUNDLE_ARGS_THREAD_ID = "args_thread_id";

    private ConversationsViewModel mViewModel;
    private FragmentSmsMessagesBinding mBinding;
    private SmsMessagesAdapter mAdapter;
    private String mThreadId;

    public static SmsMessagesFragment newInstance(String threadId) {

        Bundle args = new Bundle();
        args.putString(BUNDLE_ARGS_THREAD_ID, threadId);

        SmsMessagesFragment fragment = new SmsMessagesFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_sms_messages, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Set title
        getActivity().setTitle(R.string.sms_message);

        mViewModel = ((ConversationsActivity) getActivity()).getViewModel();

        if (null != getArguments()) {
            setupUi();
            mThreadId = getArguments().getString(BUNDLE_ARGS_THREAD_ID);
            if (!TextUtils.isEmpty(mThreadId)) {
                readSmsMessages();
            }
        }
    }

    private void setupUi() {
        //Bind RecyclerView
        RecyclerView recyclerView = mBinding.messagesList;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        mAdapter = new SmsMessagesAdapter(this);
        recyclerView.setAdapter(mAdapter);
    }

    private void readSmsMessages() {
        addToCompositeDisposable(mViewModel.getSmsMessagesByThreadId(mThreadId));
        //Observe on data
        mViewModel.watchSmsMessages().observe(getViewLifecycleOwner(),
                smsMessageList -> mAdapter.setData(smsMessageList));
    }

    @Override
    public void onSmsMessageItemClick(String messageId) {
        Log.d(TAG, "onSmsMessageItemClick(), Message Id: " + messageId);

        ((ConversationsActivity)getActivity()).startMessageDetailsFragment(messageId);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        //register observer
        mViewModel.registerSmsMessages(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        //Unregister observer
        mViewModel.unregisterSmsMessages();
    }

    /**
     * Returns the thread id
     */
    public String getThreadId() {
        return mThreadId;
    }
}
