package com.sp.smshelper.receivesms;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;

import com.sp.smshelper.pdu_utils.MmsConfig;
import com.sp.smshelper.repository.BroadcastUtils;

import java.io.File;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadManager {
    private static final String TAG = "DownloadManager";
    private static final ConcurrentHashMap<String, MmsDownloadReceiver> mMap = new ConcurrentHashMap<>();
    private static DownloadManager ourInstance = new DownloadManager();

    private DownloadManager() {

    }

    public static DownloadManager getInstance() {
        return ourInstance;
    }

    public static void finishDownload(String location) {
        if (location != null) {
            mMap.remove(location);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void downloadMultimediaMessage(final Context context, final String location, Uri uri, boolean byPush, int subscriptionId) {
        if (location == null || mMap.get(location) != null) {
            return;
        }

        MmsDownloadReceiver receiver = new MmsDownloadReceiver();
        mMap.put(location, receiver);

        // Use unique action in order to avoid cancellation of notifying download result.
        context.getApplicationContext().registerReceiver(receiver, new IntentFilter(receiver.mAction));

        Log.v(TAG, "receiving with system method");
        final String fileName = "download." + Math.abs(new Random().nextLong()) + ".dat";
        File mDownloadFile = new File(context.getCacheDir(), fileName);
        Uri contentUri = (new Uri.Builder())
                .authority(context.getPackageName() + ".provider.MmsFileProvider")
                .path(fileName)
                .scheme(ContentResolver.SCHEME_CONTENT)
                .build();
        Intent download = new Intent(receiver.mAction);
        download.putExtra(MmsReceivedReceiver.EXTRA_FILE_PATH, mDownloadFile.getPath());
        download.putExtra(MmsReceivedReceiver.EXTRA_LOCATION_URL, location);
        download.putExtra(MmsReceivedReceiver.EXTRA_TRIGGER_PUSH, byPush);
        download.putExtra(MmsReceivedReceiver.EXTRA_URI, uri);
        download.putExtra(MmsReceivedReceiver.SUBSCRIPTION_ID, subscriptionId);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, download, PendingIntent.FLAG_CANCEL_CURRENT);

        final SmsManager smsManager = SmsManager.getSmsManagerForSubscriptionId(subscriptionId);

        Bundle configOverrides = new Bundle();
        String httpParams = MmsConfig.getHttpParams();
        if (!TextUtils.isEmpty(httpParams)) {
            configOverrides.putString(SmsManager.MMS_CONFIG_HTTP_PARAMS, httpParams);
        } else {
            // this doesn't seem to always work...
            // configOverrides = smsManager.getCarrierConfigValues();
        }

        grantUriPermission(context, contentUri);
        smsManager.downloadMultimediaMessage(context, location, contentUri, configOverrides, pendingIntent);
    }

    private void grantUriPermission(Context context, Uri contentUri) {
        context.grantUriPermission(context.getPackageName() + ".provider.MmsFileProvider",
                contentUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    private static class MmsDownloadReceiver extends BroadcastReceiver {
        private static final String ACTION_PREFIX = "com.sp.smshelper.receivesms.DownloadManager$MmsDownloadReceiver.";
        private final String mAction;

        MmsDownloadReceiver() {
            mAction = ACTION_PREFIX + UUID.randomUUID().toString();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "MmsDownloadReceiver()");
            context.unregisterReceiver(this);

            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "smsmms:download-mms-lock");
            wakeLock.acquire(60 * 1000);

            Intent newIntent = (Intent) intent.clone();
            newIntent.setAction(MmsReceivedReceiver.MMS_RECEIVED);
            BroadcastUtils.sendExplicitBroadcast(context, newIntent, MmsReceivedReceiver.MMS_RECEIVED);
        }
    }
}
