package com.sp.smshelper.readmms;

import android.content.ContentProviderResult;
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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.R;
import com.sp.smshelper.databinding.FragmentReadMmsBinding;
import com.sp.smshelper.listeners.IListener;
import com.sp.smshelper.main.BaseFragment;
import com.sp.smshelper.model.MmsConversation;
import com.sp.smshelper.views.SimpleDividerItemDecoration;

import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;

/**
 * A simple {@link Fragment} subclass.
 */
public class MmsConversationFragment extends BaseFragment implements IListener.IMmsConversationFragment {

    protected static final String TAG = MmsConversationFragment.class.getSimpleName();

    private FragmentReadMmsBinding mBinding;
    private MmsViewModel mViewModel;
    private MmsConversationsAdapter mAdapter;

    static MmsConversationFragment newInstance() {

        Bundle args = new Bundle();

        MmsConversationFragment fragment = new MmsConversationFragment();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_read_mms, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Set title
        getActivity().setTitle(R.string.mms_conversations);
        setHasOptionsMenu(true);

        mViewModel = ((MmsConversationActivity) getActivity()).getViewModel();

        setupUi();
        readMmsConversations();
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
        switch (itemId) {
            case R.id.contextItemDelete:
                Disposable disposable = mAdapter.getSelectedThreadIds()
                        .flatMap((Function<List<String>, SingleSource<ContentProviderResult[]>>) strings -> mViewModel.deleteMmsThreads(strings))
                        .subscribe(results -> Log.d(TAG, "Threads deleted: " + results.length));
                addToCompositeDisposable(disposable);
                return true;
            default:
                mAdapter.clearSelections();
                return false;
        }
    }

    private void setupUi() {
        //Bind RecyclerView
        RecyclerView recyclerView = mBinding.mmsConversationsList;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext(), R.drawable.item_divider));

        mAdapter = new MmsConversationsAdapter(this);
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * Reads MMS conversations and displays them on screen
     */
    private void readMmsConversations() {
        Log.d(TAG, "readMmsConversations()");
        addToCompositeDisposable(mViewModel.getAllMmsConversations());
        //Observe on data
        mViewModel.watchMmsConversations().observe(getViewLifecycleOwner(), mmsConversations -> mAdapter.setData(mmsConversations));
    }

    @Override
    public void onMmsConversationItemClick(MmsConversation mmsConversation, int position) {
        Log.d(TAG, "onMmsConversationItemClick(), Position: " + position);
        if (mActionMode == null) {
            ((MmsConversationActivity) Objects.requireNonNull(getActivity())).startMmsMessagesFragment(mmsConversation.getThreadId());
        } else {//Multi selection
            mAdapter.toggleSelection(position);
            mActionMode.setTitle(String.valueOf(mAdapter.getSelectedItemsSize()));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        //register observer
        mViewModel.registerMmsMessages(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        //Unregister observer
        mViewModel.unregisterMmsMessages();
    }
}
