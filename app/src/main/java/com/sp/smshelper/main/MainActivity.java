package com.sp.smshelper.main;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.role.RoleManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.sp.smshelper.R;
import com.sp.smshelper.conversation.ConversationsActivity;
import com.sp.smshelper.databinding.ActivityMainBinding;
import com.sp.smshelper.readmms.MmsConversationActivity;
import com.sp.smshelper.sendmms.SendMmsActivity;
import com.sp.smshelper.sendsms.SendSmsActivity;

import io.reactivex.rxjava3.disposables.Disposable;

public class MainActivity extends BaseActivity implements IMainActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int SMS_PERMISSIONS_REQUEST_CODE = 2001;
    private static final int REQUEST_DEFAULT_APP = 5001;

    private MainActivityViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setIMainActivity(this);

        mViewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
    }

    @Override
    protected void onStart() {
        super.onStart();

        checkPermissions();
    }

    /**
     * Checks application permissions
     */
    private void checkPermissions() {
        String[] permissions = {
                Manifest.permission.READ_SMS,
                Manifest.permission.SEND_SMS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.RECEIVE_MMS,
                Manifest.permission.RECEIVE_WAP_PUSH,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        Disposable disposable = mViewModel.hasPermissions(this, SMS_PERMISSIONS_REQUEST_CODE, permissions)
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        requestDefaultApp();
                    }
                });
        addToCompositeDisposable(disposable);
    }

    /**
     * Navigates to read sms screen
     */
    @Override
    public void readSms() {
        Log.d(TAG, "readSms()");
        //start activity
        startActivity(new Intent(this, ConversationsActivity.class));
    }

    /**
     * Navigates to send sms screen
     */
    @Override
    public void sendSms() {
        Log.d(TAG, "sendSms()");
        //start activity
        startActivity(new Intent(this, SendSmsActivity.class));
    }

    /**
     * Navigates to read mms screen
     */
    @Override
    public void readMms() {
        Log.d(TAG, "readMms()");
        //start activity
        startActivity(new Intent(this, MmsConversationActivity.class));
    }

    /**
     * Navigates to send mms screen
     */
    @Override
    public void sendMms() {
        Log.d(TAG, "sendMms()");
        startActivity(new Intent(this, SendMmsActivity.class));
    }

    /**
     * Clears the app defaults
     */
    @Override
    public void clearDefaults() {
        Disposable disposable = mViewModel.checkDefaultApp(this)
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        mViewModel.changeComponentSetting(this, PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
                    }
                });
        addToCompositeDisposable(disposable);
    }

    /**
     * Requests for default app
     */
    private void requestDefaultApp() {
        Disposable disposable = mViewModel.checkDefaultApp(this)
                .subscribe(aBoolean -> {
                    if (!aBoolean) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            @SuppressLint("WrongConstant")
                            RoleManager roleManager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
                            assert roleManager != null;
                            if (roleManager.isRoleAvailable(RoleManager.ROLE_SMS)) {
                                Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS);
                                startActivityForResult(intent, REQUEST_DEFAULT_APP);
                            }
                        } else {
                            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
                            startActivityForResult(intent, REQUEST_DEFAULT_APP);
                        }
                    }
                });
        addToCompositeDisposable(disposable);
    }

    /**
     * Permission dialog approval/denial callback
     *
     * @param requestCode  Request code
     * @param permissions  Permissions
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
                requestDefaultApp();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DEFAULT_APP) {
            if (resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, getString(R.string.default_app), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.cancelled), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
