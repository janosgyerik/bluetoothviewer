package net.bluetoothviewer.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import net.bluetoothviewer.library.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class EmailUtils {

    private static final String TAG = EmailUtils.class.getSimpleName();

    private static final String MESSAGE_TYPE = "message/rfc822";

    private static final String HORIZONTAL_RULE = "\n\n---\n\n";

    private EmailUtils() {
        // prevent creating utility class
    }

    public static Intent prepareDeviceRecording(Context context, String defaultEmail, String deviceName, byte[] bytes) {
        String subject = String.format(context.getString(R.string.fmt_subject_recorded_data), deviceName);
        String messageHeader = String.format(context.getString(R.string.fmt_recorded_from), deviceName);

        StringBuilder builder = new StringBuilder();
        builder.append(messageHeader).append(HORIZONTAL_RULE);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(MESSAGE_TYPE);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{defaultEmail});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (!addAttachmentToIntent(context, deviceName, bytes, intent)) {
            builder.append(new String(bytes)).append(HORIZONTAL_RULE);
        }
        builder.append(getPackageInfoString(context));

        intent.putExtra(Intent.EXTRA_TEXT, builder.toString());
        return intent;
    }

    private static String getPackageInfoString(Context context) {
        PackageInfo info = getPackageInfo(context);
        return String.format(Locale.US, "[App: %s Version: %d/%s]",
                context.getPackageName(), info.versionCode, info.versionName);
    }

    private static PackageInfo getPackageInfo(Context context) {
        String packageName = context.getPackageName();
        PackageManager manager = context.getPackageManager();
        if (manager != null) {
            try {
                return manager.getPackageInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                Log.e(TAG, "Could not get package info", e);
            }
        }
        return new PackageInfo();
    }

    private static boolean addAttachmentToIntent(Context context, String deviceName, byte[] bytes, Intent intent) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("_yyyyMMdd_HHmm", Locale.UK);
        String filename = deviceName + dateFormat.format(new Date()) + ".dat";
        File basedir = context.getExternalCacheDir();
        File attachment = new File(basedir, filename);
        try {
            FileOutputStream ostream = new FileOutputStream(attachment);
            ostream.write(bytes);
            ostream.close();
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(attachment));
            return true;
        } catch (IOException e) {
            Log.e(TAG, "could not create temp file for attachment :(", e);
            return false;
        }
    }
}
