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
import com.sp.smshelper.main.BaseFragment;
import com.sp.smshelper.views.SimpleDividerItemDecoration;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReadMmsFragment extends BaseFragment {

    protected static final String TAG = ReadMmsFragment.class.getSimpleName();

    private FragmentReadMmsBinding mBinding;
    private MmsViewModel mViewModel;
    private MmsConversationsAdapter mAdapter;

    static ReadMmsFragment newInstance() {

        Bundle args = new Bundle();

        ReadMmsFragment fragment = new ReadMmsFragment();
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

        mViewModel = ((ReadMmsActivity) getActivity()).getViewModel();

        setupUi();
        readMmsConversations();
    }

    private void setupUi() {
        //Bind RecyclerView
        RecyclerView recyclerView = mBinding.mmsConversationsList;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getContext(), R.drawable.item_divider));

        mAdapter = new MmsConversationsAdapter();
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
}
