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

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class WeirdActivity extends Activity {
    private static final String TAG = "ACT";

    static volatile WeirdActivity activity;

    static final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message message) {
            Log.d(TAG, "handler: " + this);
            Bundle data = message.getData();
            float random = data.getFloat("randomFloat");
            if (null != activity) { activity.setText(random); }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        ((Button) findViewById(R.id.buttonStart)).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WeirdService.startRndGenerator(
                        WeirdActivity.this,
                        new Messenger(handler));
                }
            });
        ((Button) findViewById(R.id.buttonStop)).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    WeirdService.stopRndGenerator(WeirdActivity.this);
                }
            });
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: " + this);
        activity = null;
        // alternatively:
        // WeirdService.stopRndGenerator(WeirdActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        activity = this;
    }

    void setText(float random) {
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setText("New random number: " + random);
    }
}
