package com.sp.smshelper.main;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.provider.Telephony;
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
     * @param context activity context
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
     * @param context Activity context
     * @return true/false
     */
    Single<Boolean> checkDefaultApp(Context context) {
        Log.d(TAG, "checkDefaultApp()");
        return Single.fromCallable(() -> {
            checkComponentEnabledSetting(context);

            String defaultPackage = Telephony.Sms.getDefaultSmsPackage(context);
            Log.d(TAG, "Default sms package: " + defaultPackage);
            String appPackageName = BuildConfig.APPLICATION_ID;
            Log.d(TAG, "App package name:" + appPackageName);

            return appPackageName.equals(defaultPackage);
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Checks the setting of component
     *
     * @param context Activity context
     */
    private void checkComponentEnabledSetting(Context context) {
        int isStateDisabled = context.getPackageManager().getComponentEnabledSetting(new ComponentName(context, SmsReceiver.class));
        if (isStateDisabled == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            changeComponentSetting(context, PackageManager.COMPONENT_ENABLED_STATE_ENABLED);
            Log.d(TAG, "Enabled SMSReceiver component");
        }
    }

    /**
     * Changes the setting of component
     *
     * @param context Activity context
     * @param setting Setting to set
     */
    void changeComponentSetting(Context context, int setting) {
        context.getPackageManager()
                .setComponentEnabledSetting(new ComponentName(context, SmsReceiver.class),
                        setting,
                        PackageManager.DONT_KILL_APP);
    }
}
