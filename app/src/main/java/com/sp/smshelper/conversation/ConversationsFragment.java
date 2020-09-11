package com.sp.smshelper.conversation;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.R;
import com.sp.smshelper.databinding.ConversationsFragmentBinding;
import com.sp.smshelper.listeners.IListener;
import com.sp.smshelper.main.BaseFragment;
import com.sp.smshelper.model.Conversation;

public class ConversationsFragment extends BaseFragment implements IListener.IConversationsFragment {

    public static final String TAG = ConversationsFragment.class.getSimpleName();

    private ConversationsViewModel mViewModel;
    private ConversationsFragmentBinding mBinding;
    private ConversationsAdapter mAdapter;

    public static ConversationsFragment newInstance() {
        return new ConversationsFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.conversations_fragment, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Set title
        getActivity().setTitle(R.string.conversations);

        mViewModel = ((ConversationsActivity) getActivity()).getViewModel();

        setupUi();
        readConversations();
    }

    private void setupUi() {
        //Bind RecyclerView
        RecyclerView recyclerView = mBinding.conversationsList;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        mAdapter = new ConversationsAdapter(this);
        recyclerView.setAdapter(mAdapter);
    }

    private void readConversations() {
        addToCompositeDisposable(mViewModel.getAllConversations());
        //Observe on data
        mViewModel.watchConversations().observe(getViewLifecycleOwner(),
                conversationList -> mAdapter.setData(conversationList));
    }

    @Override
    public void onConversationItemClick(Conversation conversation) {
        Log.d(TAG, "onConversationItemClick()");
        ((ConversationsActivity)getActivity()).startSmsFragment(conversation.getThreadId());
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
}
