package com.morristaedt.mirror;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.morristaedt.mirror.configuration.ConfigurationSettings;
import com.morristaedt.mirror.modules.BirthdayModule;
import com.morristaedt.mirror.modules.CalendarModule;
import com.morristaedt.mirror.modules.DayModule;
import com.morristaedt.mirror.modules.DimModule;
import com.morristaedt.mirror.modules.ForecastModule;
import com.morristaedt.mirror.modules.MoodModule;
import com.morristaedt.mirror.modules.NewsModule;
import com.morristaedt.mirror.modules.SpeechRecognitionModule;
import com.morristaedt.mirror.modules.XKCDModule;
import com.morristaedt.mirror.modules.YahooFinanceModule;
import com.morristaedt.mirror.receiver.AlarmReceiver;
import com.morristaedt.mirror.requests.YahooStockResponse;
import com.morristaedt.mirror.utils.WeekUtil;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.util.List;

public class MirrorActivity extends ActionBarActivity {

    private static final boolean DEMO_MODE = false;

    @NonNull
    private ConfigurationSettings mConfigSettings;

    private TextView mBirthdayText;
    private TextView mDayText;
    private TextView mWeatherSummary;
    private TextView mHelloText;
    private TextView mStockText;
    private TextView mMoodText;
    private View mWaterPlants;
    private View mGroceryList;
    private ImageView mXKCDImage;
    private MoodModule mMoodModule;
    private DimModule mDimModule;
    private SpeechRecognitionModule mSpeechRecognitionModule = new SpeechRecognitionModule(this);


    private TextView mNewsHeadline;
    private TextView mCalendarTitleText;
    private TextView mCalendarDetailsText;

    private XKCDModule.XKCDListener mXKCDListener = new XKCDModule.XKCDListener() {
        @Override
        public void onNewXKCDToday(String url) {
            if (TextUtils.isEmpty(url)) {
                mXKCDImage.setVisibility(View.GONE);
            } else {
                Picasso.with(MirrorActivity.this).load(url).into(mXKCDImage);
                mXKCDImage.setVisibility(View.VISIBLE);
            }
        }
    };

    private YahooFinanceModule.StockListener mStockListener = new YahooFinanceModule.StockListener() {
        @Override
        public void onNewStockPrice(YahooStockResponse.YahooQuoteResponse quoteResponse) {
            if (quoteResponse == null) {
                mStockText.setVisibility(View.GONE);
            } else {
                mStockText.setVisibility(View.VISIBLE);
                mStockText.setText("$" + quoteResponse.symbol + " $" + quoteResponse.LastTradePriceOnly);
            }
        }
    };

    private ForecastModule.ForecastListener mForecastListener = new ForecastModule.ForecastListener() {
        @Override
        public void onWeatherToday(String weatherToday) {
            if (!TextUtils.isEmpty(weatherToday)) {
                mWeatherSummary.setVisibility(View.VISIBLE);
                mWeatherSummary.setText(weatherToday);
            }
        }

    };

    private NewsModule.NewsListener mNewsListener = new NewsModule.NewsListener() {
        @Override
        public void onNewNews(String headline) {
            if (TextUtils.isEmpty(headline)) {
                mNewsHeadline.setVisibility(View.GONE);
            } else {
                mNewsHeadline.setVisibility(View.VISIBLE);
                mNewsHeadline.setText(headline);
            }
        }
    };

    private MoodModule.MoodListener mMoodListener = new MoodModule.MoodListener() {
        @Override
        public void onShouldGivePositiveAffirmation(final String affirmation) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMoodText.setVisibility(affirmation == null ? View.GONE : View.VISIBLE);
                    mMoodText.setText(affirmation);
                }
            });
        }
    };

    private DimModule.DimListener mDimListener = new DimModule.DimListener() {
        @Override
        public void onDim(int brightnessValue) {
            int curBrightness = 0;
            try {
                curBrightness = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            if(curBrightness != brightnessValue) {
                android.provider.Settings.System.putInt(getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS,
                        brightnessValue);

                // Apply brightness by creating a dummy activity
                Intent intent = new Intent(getBaseContext(), DimModule.DummyBrightnessActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("brightness value", brightnessValue / 255.0f);
                getApplication().startActivity(intent);
            }

        }
    };

    private CalendarModule.CalendarListener mCalendarListener = new CalendarModule.CalendarListener() {
        @Override
        public void onCalendarUpdate(List<CalendarModule.CalendarItem> items) {
            CalendarModule.CalendarItem item = items.get(0);
            String title = item.title;
            String details = item.details;
            mCalendarTitleText.setVisibility(title != null ? View.VISIBLE : View.GONE);
            mCalendarTitleText.setText(title);
            mCalendarDetailsText.setVisibility(details != null ? View.VISIBLE : View.GONE);
            mCalendarDetailsText.setText(details);

            //Make marquee effect work for long text
            mCalendarTitleText.setSelected(true);
            mCalendarDetailsText.setSelected(true);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("memory bitch "+Runtime.getRuntime().maxMemory());
        setContentView(R.layout.activity_main);
        mConfigSettings = new ConfigurationSettings(this);
        AlarmReceiver.startMirrorUpdates(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mBirthdayText = (TextView) findViewById(R.id.birthday_text);
        mDayText = (TextView) findViewById(R.id.day_text);
        mWeatherSummary = (TextView) findViewById(R.id.weather_summary);
        mStockText = (TextView) findViewById(R.id.stock_text);
        mMoodText = (TextView) findViewById(R.id.mood_text);
        mXKCDImage = (ImageView) findViewById(R.id.xkcd_image);
        mNewsHeadline = (TextView) findViewById(R.id.news_headline);
        mCalendarTitleText = (TextView) findViewById(R.id.calendar_title);
        mCalendarDetailsText = (TextView) findViewById(R.id.calendar_details);

        if (mConfigSettings.invertXKCD()) {
            //Negative of XKCD image
            float[] colorMatrixNegative = {
                    -1.0f, 0, 0, 0, 255, //red
                    0, -1.0f, 0, 0, 255, //green
                    0, 0, -1.0f, 0, 255, //blue
                    0, 0, 0, 1.0f, 0 //alpha
            };
            ColorFilter colorFilterNegative = new ColorMatrixColorFilter(colorMatrixNegative);
            mXKCDImage.setColorFilter(colorFilterNegative); // not inverting for now
        }

        mSpeechRecognitionModule.addListener("calendar", new SpeechRecognitionModule.SpeechRecognitionListener() {
            @Override
            public void onRecognizedSpeech() {
                System.out.println("showing calendar!!!");
            }
        });

    }

    @Override
    protected void onResume()   {
        super.onResume();
        setViewState();
        mSpeechRecognitionModule.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mMoodModule != null) {
            mMoodModule.release();
        }
        mSpeechRecognitionModule.stop();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setViewState();
    }

    private void setViewState() {
        List<String> birthdays = BirthdayModule.getBirthday(this);
        if (birthdays.isEmpty()) {
            mBirthdayText.setVisibility(View.GONE);
        } else {
            mBirthdayText.setVisibility(View.VISIBLE);
            mBirthdayText.setSelected(true);

            String bdayString = birthdays.get(0);
            for(int i = 1; i<birthdays.size(); i++) {
                bdayString += ","+birthdays.get(i);
            }
            mBirthdayText.setText(getString(R.string.happy_birthday, bdayString));
        }
        mDayText.setText(DayModule.getDay());

        ForecastModule.getHourlyForecast(getResources(), mConfigSettings.getLatitude(), mConfigSettings.getLongitude(), mForecastListener);

        if (mConfigSettings.showNewsHeadline()) {
            NewsModule.getNewsHeadline(mNewsListener);
        } else {
            mNewsHeadline.setVisibility(View.GONE);
        }

        if (mConfigSettings.showXKCD()) {
            XKCDModule.getXKCDForToday(mXKCDListener);
        } else {
            mXKCDImage.setVisibility(View.GONE);
        }

        if (mConfigSettings.showNextCalendarEvent()) {
            CalendarModule.getCalendarEvents(this, mCalendarListener, 1);
        } else {
            mCalendarTitleText.setVisibility(View.GONE);
            mCalendarDetailsText.setVisibility(View.GONE);
        }

        if (mConfigSettings.showStock() && WeekUtil.isWeekday() && WeekUtil.afterFive()) {
            YahooFinanceModule.getStockForToday(mConfigSettings.getStockTickerSymbol(), mStockListener);
        } else {
            mStockText.setVisibility(View.GONE);
        }

        if (mConfigSettings.showMoodDetection()) {
            mMoodModule = new MoodModule(new WeakReference<Context>(this));
            mMoodModule.getCurrentMood(mMoodListener);
        } else {
            mMoodText.setVisibility(View.GONE);
        }

        mDimModule = new DimModule();
        mDimModule.getScreenBrightness(mDimListener);

    }

    private void showDemoMode() {
        if (DEMO_MODE) {
            mStockText.setVisibility(View.VISIBLE);
            mWaterPlants.setVisibility(View.VISIBLE);
            mGroceryList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, SetUpActivity.class);
        startActivity(intent);
    }
}
