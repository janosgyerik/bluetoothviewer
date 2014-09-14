package net.bluetoothviewer.util;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import net.bluetoothviewer.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


public abstract class EmailTools {

    private static final String TAG = EmailTools.class.getSimpleName();

    private static final String MESSAGE_TYPE = "message/rfc822";

    public static void send(Context context, String subject, String message, String attachmentContent) {
        String packageName = context.getPackageName();
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info;
            if (manager != null) {
                info = manager.getPackageInfo(packageName, 0);
                if (info != null) {
                    message += String.format("\n\n--\n[App: %s Version: %d/%s]",
                            packageName, info.versionCode, info.versionName);
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Could not get package info", e);
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(MESSAGE_TYPE);
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, message);

        if (attachmentContent != null) {
            try {
                String filename = "btviewer.dat";
                FileOutputStream ostream = context.openFileOutput(filename, Context.MODE_WORLD_READABLE);
                ostream.write(attachmentContent.getBytes());
                ostream.close();
                File attachment = context.getFileStreamPath(filename);
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(attachment));
            } catch (IOException e) {
                Log.e(TAG, "could not create temp file", e);
            }
        }

        try {
            context.startActivity(Intent.createChooser(intent, context.getString(R.string.email_client_chooser)));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(context, context.getString(R.string.no_email_client), Toast.LENGTH_SHORT).show();
        }
    }

    public static void send(Context context, int subjectID, String message, String attachmentContent) {
        send(context, context.getString(subjectID), message, attachmentContent);
    }
}
