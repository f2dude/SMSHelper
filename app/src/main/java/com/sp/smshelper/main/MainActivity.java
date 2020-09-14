package com.sp.smshelper.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.sp.smshelper.R;
import com.sp.smshelper.conversation.ConversationsActivity;
import com.sp.smshelper.databinding.ActivityMainBinding;
import com.sp.smshelper.sendsms.SendSmsActivity;

public class MainActivity extends BaseActivity implements IMainActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int READ_SMS_REQUEST_CODE = 1001;
    private static final int SEND_Receive_SMS_PHONE_STATE_REQUEST_CODE = 1002;

    private MainActivityViewModel mViewModel;
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.setIMainActivity(this);

        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
    }

    /**
     * Navigates to read sms screen
     */
    @Override
    public void readSms() {
        String[] permissions = {
                Manifest.permission.READ_SMS
        };

        if (hasPermissions(READ_SMS_REQUEST_CODE, permissions)) {
            Log.d(TAG, "readSms()");
            //start activity
            startActivity(new Intent(this, ConversationsActivity.class));
        }
    }

    /**
     * Navigates to send sms screen
     */
    @Override
    public void sendSms() {
        String[] permissions = {
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECEIVE_SMS
        };

        if (hasPermissions(SEND_Receive_SMS_PHONE_STATE_REQUEST_CODE, permissions)) {
            Log.d(TAG, "sendSms()");
            //start activity
            startActivity(new Intent(this, SendSmsActivity.class));
        }
    }

    /**
     * Checks whether the permissions are approved
     * @param requestCode Request code
     * @param permissions Permission type
     * @return
     */
    public boolean hasPermissions(int requestCode, String... permissions) {
        if (null != permissions) {
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, requestCode);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Permission dialog approval/denial callback
     * @param requestCode Request code
     * @param permissions Permissions
     * @param grantResults Results
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            boolean isGranted = true;
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    isGranted = false;
                    break;
                }
            }
            if (isGranted) {
                Toast.makeText(this, R.string.permission_granted, Toast.LENGTH_SHORT).show();
                if (requestCode == READ_SMS_REQUEST_CODE) {
                    readSms();
                } else if (requestCode == SEND_Receive_SMS_PHONE_STATE_REQUEST_CODE) {
                    sendSms();
                }
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
