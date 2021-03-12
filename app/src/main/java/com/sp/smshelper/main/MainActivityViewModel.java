package com.sp.smshelper.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Telephony;
import android.text.TextUtils;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModel;

import com.sp.smshelper.BuildConfig;
import com.sp.smshelper.receivesms.SmsReceiver;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivityViewModel extends ViewModel {

    private static final String TAG = MainActivityViewModel.class.getSimpleName();

    /**
     * Checks whether the permissions are approved
     *
     * @param context     activity context
     * @param requestCode Request code
     * @param permissions Permission type
     * @return true/false
     */
    Single<Boolean> hasPermissions(Context context, int requestCode, String... permissions) {

        return Single.fromCallable(() -> {
            if (null != permissions) {
                for (String permission : permissions) {
                    if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) context, permissions, requestCode);
                        return false;
                    }
                }
            }
            return true;
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Checks for default app
     *
     * @param context Activity context
     * @return true/false
     */
    Single<Boolean> checkDefaultApp(Context context) {
        Log.d(TAG, "checkDefaultApp()");
        return Single.fromCallable(() -> {

            String defaultPackage = Telephony.Sms.getDefaultSmsPackage(context);
            Log.d(TAG, "Default sms package: " + defaultPackage);
            String appPackageName = BuildConfig.APPLICATION_ID;
            Log.d(TAG, "App package name:" + appPackageName);

            return appPackageName.equals(defaultPackage);
        });
    }

    /**
     * Checks the Component setting
     *
     * @param context Activity context
     * @return true if component is enabled or false otherwise
     */
    Single<Boolean> checkComponentState(Context context) {
        return Single.fromCallable(() -> {
            int state = context.getPackageManager().getComponentEnabledSetting(new ComponentName(context, SmsReceiver.class));
            return state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    || state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
        });
    }

    /**
     * Changes the setting of component
     *
     * @param context Activity context
     * @param state   true to enable component, false to disable the component
     */
    void changeComponentSetting(Context context, boolean state) {
        String defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context);//before component state change
        //When default sms package name is not null, then only enable the component or else don't
        if (!TextUtils.isEmpty(defaultSmsPackage)) {
            Log.d(TAG, "Component state changed!");
            context.getPackageManager()
                    .setComponentEnabledSetting(new ComponentName(context, SmsReceiver.class),
                            state ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                            PackageManager.DONT_KILL_APP);
        } else {
            Log.d(TAG, "Component state cannot be changed!");
        }
        defaultSmsPackage = Telephony.Sms.getDefaultSmsPackage(context);//after component state change
        Log.d(TAG, "Component disabled! Default app package name is: " + defaultSmsPackage);
    }
}
