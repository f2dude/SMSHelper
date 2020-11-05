package com.sp.smshelper.sendmms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sp.smshelper.R;
import com.sp.smshelper.databinding.ActivitySendMmsBinding;
import com.sp.smshelper.main.BaseActivity;
import com.sp.smshelper.pdu_utils.Utils;
import com.sp.smshelper.sendsms.NumbersAdapter;

import java.io.File;
import java.util.ArrayList;

import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class SendMmsActivity extends BaseActivity {

    private static final String TAG = SendMmsActivity.class.getSimpleName();

    private ActivitySendMmsBinding mBinding;
    private SendMmsViewModel mViewModel;
    private StringBuilder mSb = new StringBuilder();
    private ArrayList<String> mFilePaths = new ArrayList<>();

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
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            boolean isMobileDataEnabled = Utils.isDataEnabled(telephonyManager, mViewModel.getSubscriptionId());
            Log.d(TAG, "Is mobile data enabled: " + isMobileDataEnabled);
            if (isMobileDataEnabled) {
                String address = mBinding.phoneNumber.getText().toString().trim();
                String subject = mBinding.subject.getText().toString().trim();
                String messageBody = mBinding.mmsBody.getText().toString().trim();
                if (TextUtils.isEmpty(mSb.toString())) {
                    mSb.append(address);
                }
                if (!TextUtils.isEmpty(mSb.toString().trim()) && mFilePaths.size() == 1) {
                    addToCompositeDisposable(mViewModel.sendMmsMessage(mSb.toString().trim(), subject, messageBody, mFilePaths.get(0)));
                }
            } else {
                Toast.makeText(this, getString(R.string.enable_mobile_data), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getText(R.string.select_phone_number), Toast.LENGTH_SHORT).show();
        }
    }

    public void onAddClicked() {
        Log.d(TAG, "onAddClicked");
        mSb.append(mBinding.phoneNumber.getText());
        mSb.append(" ");

        mBinding.addedNumbers.setText(mSb.toString().trim());
        mBinding.phoneNumber.setText("");
    }

    public void onAttachClicked() {
        Log.d(TAG, "onAttachClicked");
//        FilePickerBuilder.getInstance().setMaxCount(1)
//                .setSelectedFiles(mFilePaths)
//                .setActivityTheme(R.style.AppTheme)
//                .pickPhoto(this);

        FilePickerBuilder.getInstance().setMaxCount(1)
                .setSelectedFiles(mFilePaths)
                .setActivityTheme(R.style.AppTheme)
                .pickDocument(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FilePickerConst.REQUEST_CODE:
                if (resultCode == RESULT_OK && data != null) {
                    mFilePaths.clear();
                    mFilePaths = data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_PHOTOS);
                    Log.d(TAG, "File paths: " + mFilePaths);
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < mFilePaths.size(); i++) {
                        File file = new File(mFilePaths.get(i));
                        sb.append(file.getName());
                        if ((i + 1) < mFilePaths.size()) {
                            sb.append("\n");
                        }
                    }
                    mBinding.fileAttached.setText(sb.toString());
                }
        }
    }
}
