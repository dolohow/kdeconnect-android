package org.kde.kdeconnect.Plugins.FindMyPhonePlugin;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import org.kde.kdeconnect_tp.R;

class Flashlight implements Runnable {
    public boolean stopRunning = false;

    @Override
    public void run() {
        Camera camera;
        try {
            camera = Camera.open();
        } catch (RuntimeException re) {
            Log.d("FindMyPhone", "Device does not have a camera");
            return;
        }

        Camera.Parameters flashlightOn = camera.getParameters();
        Camera.Parameters flashlightOff = camera.getParameters();

        flashlightOn.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        flashlightOff.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);

        while (!stopRunning) {
            try {
                camera.setParameters(flashlightOn);
                Thread.sleep(40);
                camera.setParameters(flashlightOff);
                Thread.sleep(40);
            } catch (InterruptedException ignored) {

            } catch (RuntimeException re) {
                Log.d("FindMyPhone", "Device does not have a flashlight");
                camera.release();
                return;
            }

        }

        camera.release();
    }
}

public class FindMyPhoneActivity extends Activity {
    Ringtone ringtone;
    Flashlight flashlight;

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        finish(); //If this activity was already open and we received the ring packet again, just finish it
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_my_phone);

        Window window = this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        flashlight = new Flashlight();
        Thread flashlightThread = new Thread(flashlight);
        flashlightThread.start();

        Uri ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        ringtone = RingtoneManager.getRingtone(getApplicationContext(), ringtoneUri);

        if (android.os.Build.VERSION.SDK_INT >= 21) {
            AudioAttributes.Builder b = new AudioAttributes.Builder();
            b.setUsage(AudioAttributes.USAGE_ALARM);
            ringtone.setAudioAttributes(b.build());
        } else {
            ringtone.setStreamType(AudioManager.STREAM_ALARM);
        }

        ringtone.play();

        findViewById(R.id.bFindMyPhone).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public void finish() {
        ringtone.stop();
        flashlight.stopRunning = true;
        super.finish();
    }
}
