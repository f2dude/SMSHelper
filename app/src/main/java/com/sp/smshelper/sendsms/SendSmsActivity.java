package com.sp.smshelper.sendsms;

import android.os.Bundle;
import android.util.Log;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.R;
import com.sp.smshelper.databinding.ActivitySendSmsBinding;
import com.sp.smshelper.main.BaseActivity;

public class SendSmsActivity extends BaseActivity {

    public static final String TAG = SendSmsActivity.class.getSimpleName();

    private SendSmsViewModel mViewModel;
    private ActivitySendSmsBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_send_sms);
        mBinding.setActivity(this);

        mViewModel = new ViewModelProvider(this).get(SendSmsViewModel.class);
        mViewModel.setContext(this);
        setupUI();
    }

    /**
     * Sets up the UI
     */
    private void setupUI() {
        //Set title
        setTitle(R.string.send_sms);

        //Set numbers recycler view
        RecyclerView recyclerView = mBinding.ownNumbersList;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

        NumbersAdapter numbersAdapter = new NumbersAdapter();
        numbersAdapter.watchSubscriptionIdData().observe(this, subscriptionId -> {
            mViewModel.setSubscriptionId(subscriptionId);
        });
        recyclerView.setAdapter(numbersAdapter);

        //Get data
        addToCompositeDisposable(mViewModel.getDeviceNumbersList());
        //Observe on data
        mViewModel.watchSimCardsList().observe(this, numbersAdapter::setData);
        mViewModel.watchMessageSentStatus().observe(this, s -> mBinding.messageSent.setText(s));
        mViewModel.watchMessageDeliveredStatus().observe(this, s -> mBinding.messageDelivered.setText(s));
    }

    /**
     * Called suring button click event
     * Sends message
     */
    public void sendSms(String phoneNumber, String message) {
        Log.d(TAG, "Phone number: " + phoneNumber + " ,Message: " + message);
        addToCompositeDisposable(mViewModel.sendSMS(phoneNumber, message));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart()");
        mViewModel.registerReceiver();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop()");
        mViewModel.unRegisterReceiver();
    }
}
