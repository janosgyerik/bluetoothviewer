package net.bluetoothviewer.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import net.bluetoothviewer.full.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public abstract class EmailTools {

    private static final String TAG = EmailTools.class.getSimpleName();

    private static final String MESSAGE_TYPE = "message/rfc822";

    private static final String HORIZONTAL_RULE = "\n\n---\n\n";

    public static void sendDeviceRecording(Context context, String defaultEmail, String deviceName, String recordedContent) {
        String subject = String.format(context.getString(R.string.fmt_subject_recorded_data), deviceName);
        String messageHeader = String.format(context.getString(R.string.fmt_recorded_from), deviceName);

        StringBuilder builder = new StringBuilder();
        builder.append(messageHeader);

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(MESSAGE_TYPE);
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{defaultEmail});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        if (!addAttachmentToIntent(context, deviceName, recordedContent, intent)) {
            builder.append(HORIZONTAL_RULE).append(recordedContent);
        }
        builder.append(HORIZONTAL_RULE).append(getPackageInfoString(context));

        intent.putExtra(Intent.EXTRA_TEXT, builder.toString());
        launchEmailApp(context, intent);
    }

    private static String getPackageInfoString(Context context) {
        PackageInfo info = getPackageInfo(context);
        return String.format("[App: %s Version: %d/%s]",
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

    private static boolean addAttachmentToIntent(Context context, String deviceName, String recordedContent, Intent intent) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("_yyyyMMdd_HHmm");
        String filename = deviceName + dateFormat.format(new Date()) + ".dat";
        try {
            FileOutputStream ostream = context.openFileOutput(filename, Context.MODE_WORLD_READABLE);
            ostream.write(recordedContent.getBytes());
            ostream.close();
            File attachment = context.getFileStreamPath(filename);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(attachment));
            return true;
        } catch (IOException e) {
            Log.e(TAG, "could not create temp file for attachment :(", e);
            return false;
        }
    }

    private static void launchEmailApp(Context context, Intent intent) {
        try {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.email_client_chooser)));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, context.getString(R.string.no_email_client), Toast.LENGTH_SHORT).show();
        }
    }
}
