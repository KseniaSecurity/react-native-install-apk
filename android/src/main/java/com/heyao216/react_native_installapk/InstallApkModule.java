package com.heyao216.react_native_installapk;

import android.app.PendingIntent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.os.Build;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by heyao on 2016/11/4.
 */
public class InstallApkModule extends ReactContextBaseJavaModule {
    private ReactApplicationContext _context = null;

    public InstallApkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        _context = reactContext;
    }

    @Override
    public String getName() {
        return "InstallApk";
    }

    @ReactMethod
    public void install(String path, String provider) {
        String cmd = "chmod 777 " + path;
        try {
            Runtime.getRuntime().exec(cmd);
        } catch (Exception e) {
            e.printStackTrace();
        }
        File apkFile = new File(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri;
        if (Build.VERSION.SDK_INT >= 24) {
            uri = FileProvider.getUriForFile(_context, provider, apkFile);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            uri = Uri.fromFile(apkFile);
        }
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        _context.startActivity(intent);
    }

    @ReactMethod
    public void silentInstall(String path, String packageName, String activityName, final Promise promise) {
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            try {
                File file = new File(path);
                FileInputStream inputStream = new FileInputStream(file);
                PackageInstaller packageInstaller = _context.getPackageManager().getPackageInstaller();
                packageInstaller.registerSessionCallback(new PackageInstaller.SessionCallback() {
                    @Override
                    public void onCreated(int sessionId) {

                    }

                    @Override
                    public void onBadgingChanged(int sessionId) {

                    }

                    @Override
                    public void onActiveChanged(int sessionId, boolean active) {

                    }

                    @Override
                    public void onProgressChanged(int sessionId, float progress) {

                    }

                    @Override
                    public void onFinished(int sessionId, boolean success) {
                        if (success)
                            promise.resolve(true);
                        else
                            promise.reject("error", "install error");
                    }
                });
                int sessionId = 0;
                sessionId = packageInstaller.createSession(new PackageInstaller
                        .SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL));
                PackageInstaller.Session session = packageInstaller.openSession(sessionId);
                long sizeBytes = 0;

                OutputStream out = null;
                out = session.openWrite("my_app_session", 0, sizeBytes);

                int total = 0;
                byte[] buffer = new byte[65536];
                int c;
                while ((c = inputStream.read(buffer)) != -1) {
                    total += c;
                    out.write(buffer, 0, c);
                }
                session.fsync(out);
                inputStream.close();
                out.close();

                session.commit(createIntentSender(sessionId, packageName, activityName));
            } catch (IOException e) {
                promise.reject("error", e);
            }
        }
    }

    private IntentSender createIntentSender(int sessionId, String packageName, String activityName) {
        Intent intent = new Intent(packageName);
        intent.putExtra("ACTIVITY_NAME", activityName);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                _context,
                sessionId,
                intent,
                0);
        return pendingIntent.getIntentSender();
    }
}
