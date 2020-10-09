package com.sp.smshelper.readmms;

import android.os.Bundle;
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
import com.sp.smshelper.databinding.FragmentReadMmsBinding;
import com.sp.smshelper.listeners.IListener;
import com.sp.smshelper.main.BaseFragment;
import com.sp.smshelper.model.MmsConversation;
import com.sp.smshelper.views.SimpleDividerItemDecoration;

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

        mViewModel = ((MmsConversationActivity) getActivity()).getViewModel();

        setupUi();
        readMmsConversations();
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
    }
}