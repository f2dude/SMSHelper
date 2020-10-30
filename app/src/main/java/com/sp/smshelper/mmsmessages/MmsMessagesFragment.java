package com.sp.smshelper.mmsmessages;

import android.content.ContentProviderResult;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.sp.smshelper.databinding.FragmentMmsMessagesBinding;
import com.sp.smshelper.listeners.IListener;
import com.sp.smshelper.main.BaseFragment;
import com.sp.smshelper.readmms.MmsConversationActivity;
import com.sp.smshelper.readmms.MmsViewModel;
import com.sp.smshelper.views.SimpleDividerItemDecoration;

import java.util.List;
import java.util.Objects;

import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Function;

/**
 * A simple {@link Fragment} subclass.
 */
public class MmsMessagesFragment extends BaseFragment implements IListener.IMmsMessagesFragment {

    public static final String TAG = MmsMessagesFragment.class.getSimpleName();
    private static final String BUNDLE_ARGS_THREAD_ID = "args_thread_id";

    private FragmentMmsMessagesBinding mBinding;
    private String mThreadId;
    private MmsViewModel mViewModel;
    private MmsMessagesAdapter mAdapter;

    /**
     * Creates a new instance of the fragment
     * @param threadId MMS messages thread Id
     * @return Fragment object
     */
    public static MmsMessagesFragment newInstance(String threadId) {

        Bundle args = new Bundle();
        args.putString(BUNDLE_ARGS_THREAD_ID, threadId);

        MmsMessagesFragment fragment = new MmsMessagesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_mms_messages, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //Set title
        Objects.requireNonNull(getActivity()).setTitle(R.string.mms_messages);
        setHasOptionsMenu(true);

        mViewModel = ((MmsConversationActivity) getActivity()).getViewModel();

        if (null != getArguments()) {
            setupUi();

            mThreadId = getArguments().getString(BUNDLE_ARGS_THREAD_ID);
            if (!TextUtils.isEmpty(mThreadId)) {
                readMMSMessages();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.sms_messages, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mark_all_read:

                return true;
            case R.id.select:
                startActionMode((AppCompatActivity) getActivity(), R.menu.sms_messages_action_menu, getString(R.string.title_zero));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected boolean onActionItemClick(int itemId) {
        super.onActionItemClick(itemId);
        switch (itemId) {
            case R.id.contextItemDelete:
                Disposable disposable = mAdapter.getSelectedMessagesIds()
                        .flatMap((Function<List<String>, SingleSource<ContentProviderResult[]>>) strings -> mViewModel.deleteMmsMessages(strings))
                        .subscribe(results -> Log.d(TAG, "Messages deleted: " + results.length));
                addToCompositeDisposable(disposable);
                return true;
            default:
                mAdapter.clearSelections();
                return false;
        }
    }

    /**
     * Sets up UI
     */
    private void setupUi() {
        RecyclerView recyclerView = mBinding.mmsMessagesList;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext(), R.drawable.item_divider));

        mAdapter = new MmsMessagesAdapter(this);
        recyclerView.setAdapter(mAdapter);
    }

    /**
     * Read MMS messages mapped with thread Id
     */
    private void readMMSMessages() {
        addToCompositeDisposable(mViewModel.getMmsMessagesByThreadId(mThreadId));
        //Observe on data
        mViewModel.watchMmsMessages().observe(getViewLifecycleOwner(), mmsMessages -> mAdapter.setData(mmsMessages));
    }

    @Override
    public void onMmsMessageItemClick(String messageId, int position) {
        Log.d(TAG, "onMmsMessageItemClick(), Position: " + position);
        if (mActionMode == null) {
            ((MmsConversationActivity) Objects.requireNonNull(getActivity())).startMmsDetailsFragment(messageId);
        } else {
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

    /**
     * Returns the thread id
     */
    public String getThreadId() {
        return mThreadId;
    }
}
