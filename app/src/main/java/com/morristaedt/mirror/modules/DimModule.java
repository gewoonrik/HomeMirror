package com.morristaedt.mirror.modules;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import java.util.Calendar;

/**
 * Created by rik on 28/09/15.
 */
public class DimModule  {

    public interface DimListener {
        void onDim(int brightnessValue);
    }
    public static class DummyBrightnessActivity extends Activity {

        private static final int DELAYED_MESSAGE = 1;

        private Handler handler;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    if(msg.what == DELAYED_MESSAGE) {
                        DummyBrightnessActivity.this.finish();
                    }
                    super.handleMessage(msg);
                }
            };
            Intent brightnessIntent = this.getIntent();
            float brightness = brightnessIntent.getFloatExtra("brightness value", 0);
            WindowManager.LayoutParams lp = getWindow().getAttributes();
            lp.screenBrightness = brightness;
            getWindow().setAttributes(lp);

            Message message = handler.obtainMessage(DELAYED_MESSAGE);
            //this next line is very important, you need to finish your activity with slight delay
            handler.sendMessageDelayed(message,1000);
        }

    }


    public DimModule()
    {

    }


    public void getScreenBrightness(DimListener listener) {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        if(hour > 21 || hour < 7) {
            //completely dim between 22 and 7
            listener.onDim(0);
        }
        else
        {
            listener.onDim(255);
        }
    }
}
