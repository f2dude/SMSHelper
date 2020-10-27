package com.sp.smshelper.sendmms;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.R;
import com.sp.smshelper.databinding.ActivitySendMmsBinding;
import com.sp.smshelper.main.BaseActivity;
import com.sp.smshelper.sendsms.NumbersAdapter;

public class SendMmsActivity extends BaseActivity {

    private static final String TAG = SendMmsActivity.class.getSimpleName();

    private ActivitySendMmsBinding mBinding;
    private SendMmsViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_send_mms);
        mBinding.setMSendMmsActivity(this);

        mViewModel = new ViewModelProvider(this).get(SendMmsViewModel.class);
        mViewModel.setContext(this);

        setupUi();
    }

    /**
     * Sets up the UI
     */
    private void setupUi() {
        //Set title
        setTitle(R.string.send_mms);

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
    }

    public void onSendClicked() {
        Log.d(TAG, "onSendClicked()");
        if (mViewModel.getSubscriptionId() > -1) {
            String address = mBinding.phoneNumber.getText().toString().trim();
            String subject = mBinding.subject.getText().toString().trim();
            String messageBody = mBinding.mmsBody.getText().toString().trim();

            if (!TextUtils.isEmpty(address)) {
                mViewModel.sendMmsMessage(address, subject, messageBody, R.drawable.android);
            }
        } else {
            Toast.makeText(this, getText(R.string.select_phone_number), Toast.LENGTH_SHORT).show();
        }
    }
}
