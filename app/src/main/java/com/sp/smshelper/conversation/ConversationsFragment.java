package com.sp.smshelper.conversation;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.R;
import com.sp.smshelper.databinding.ConversationsFragmentBinding;
import com.sp.smshelper.listeners.IListener;
import com.sp.smshelper.main.BaseFragment;
import com.sp.smshelper.model.Conversation;

import io.reactivex.rxjava3.disposables.Disposable;

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
        setHasOptionsMenu(true);

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
    public void onConversationItemClick(Conversation conversation, int position) {
        Log.d(TAG, "onConversationItemClick()");

        if (mActionMode == null) {
            ((ConversationsActivity)getActivity()).startSmsFragment(conversation.getThreadId());
        } else {//Multi selection
            mAdapter.toggleSelection(position);
            mActionMode.setTitle(String.valueOf(mAdapter.getSelectedItemsSize()));
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.conversations, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.select) {
            startActionMode((AppCompatActivity) getActivity(), R.menu.conversations_action_menu, getString(R.string.title_zero));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected boolean onActionItemClick(int itemId) {
        super.onActionItemClick(itemId);
        switch(itemId) {
            case R.id.contextItemDelete:
                Disposable disposable = mAdapter.getSelectedThreadIds().subscribe(strings -> {
                    mViewModel.deleteSmsThreads(strings);
                    mAdapter.clearSelections();
                });
                addToCompositeDisposable(disposable);
                return true;
            default:
                return false;
        }
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
