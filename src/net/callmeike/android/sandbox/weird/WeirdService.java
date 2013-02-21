/* $Id: $
   Copyright 2013, G. Blake Meike

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.callmeike.android.sandbox.weird;


import java.util.Random;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;


public class WeirdService extends IntentService {
    private static final String TAG = "SVC";

    private static final String EXTRA_OP = "WeirdService.OP";
    private static final String EXTRA_MESSENGER = "WeirdService.MESSENGER";
    private static final int OP_START = -9001;
    private static final int OP_STOP = -9002;

    public static void startRndGenerator(Context ctxt, Messenger messenger) {
        Intent i = new Intent(ctxt, WeirdService.class);
        i.putExtra(EXTRA_OP, OP_START);
        i.putExtra(EXTRA_MESSENGER, messenger);
        ctxt.startService(i);
    }

    public static void stopRndGenerator(Context ctxt) {
        Intent i = new Intent(ctxt, WeirdService.class);
        i.putExtra(EXTRA_OP, OP_STOP);
        ctxt.startService(i);
    }

    private NotificationManager mNotificationManager;
    private Notification notification;
    private Random r;
    private volatile boolean stop;

    public WeirdService() { super(TAG); }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        r = new Random();

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Notification.Builder mBuilder = new Notification.Builder(
            this)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentTitle("My notification")
            .setContentText("Hello World!")
            .setProgress(100, 0, false)
            .setContentIntent(
                PendingIntent.getActivity(getApplicationContext(), 0,
                    new Intent(getApplicationContext(), WeirdActivity.class),
                    PendingIntent.FLAG_ONE_SHOT));

        notification = mBuilder.build();

        startForeground(42, notification);
    }

    // This is a pretty serious hack.  It is necessary because
    // rnd generation completely hangs the daemon thread.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extra = intent.getExtras();
        if (null != extra) {
            switch (extra.getInt(EXTRA_OP)) {
                case OP_START:
                    stop = false;
                    break;

                case OP_STOP:
                    stop = true;
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    // This will completely stall the daemon Looper thread!!
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: " + stop);
        Messenger messenger = (Messenger) intent.getExtras().get(EXTRA_MESSENGER);
        for (int i = 1; i < 10; i++ ) {
            if (stop) { break; }

            notification.contentView.setProgressBar(android.R.id.progress, 100, i, false);
            mNotificationManager.notify(42, notification);
            try {
                Message backMsg = Message.obtain();
                Bundle bundle = new Bundle();
                bundle.putFloat("randomFloat", r.nextFloat());
                backMsg.setData(bundle);

                Log.d(TAG, "sending: " + messenger);
                messenger.send(backMsg);
                Thread.sleep(1000);
            }
            catch (Exception e) {
                Log.e(TAG, "Failed sending rnd", e);
            }
        }
    }
}
