package com.morristaedt.mirror;

/**
 * Created by rik on 28/09/15.
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MarqueeView extends LinearLayout {
    private TextView mTextField;
    private ScrollView mScrollView;
    private static final int TEXTVIEW_VIRTUAL_WIDTH = 2000;
    private Animation mMoveTextOut = null;
    private Animation mMoveTextIn = null;
    private Paint mPaint;
    private boolean mMarqueeNeeded = false;
    private static final String TAG = MarqueeView.class.getSimpleName();
    private float mTextDifference;
    private static final int DEFAULT_SPEED = 60;
    private static final int DEFAULT_ANIMATION_PAUSE = 2000;
    private int mSpeed = 60;
    private int mAnimationPause = 2000;
    private boolean mAutoStart = false;
    private Interpolator mInterpolator = new LinearInterpolator();
    private boolean mCancelled = false;
    private Runnable mAnimationStartRunnable;
    private boolean mStarted;

    public void setSpeed(int speed) {
        this.mSpeed = speed;
    }

    public void setPauseBetweenAnimations(int pause) {
        this.mAnimationPause = pause;
    }

    public void setInterpolator(Interpolator interpolator) {
        this.mInterpolator = interpolator;
    }

    public MarqueeView(Context context) {
        super(context);
        this.init(context);
    }

    public MarqueeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
        this.extractAttributes(attrs);
    }

    @TargetApi(11)
    public MarqueeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.init(context);
        this.extractAttributes(attrs);
    }

    private void extractAttributes(AttributeSet attrs) {
        if(this.getContext() != null) {
            TypedArray a = this.getContext().obtainStyledAttributes(attrs, asia.ivity.android.marqueeview.R.styleable.asia_ivity_android_marqueeview_MarqueeView);
            if(a != null) {
                this.mSpeed = a.getInteger(0, 60);
                this.mAnimationPause = a.getInteger(1, 2000);
                this.mAutoStart = a.getBoolean(2, false);
                a.recycle();
            }
        }
    }

    private void init(Context context) {
        this.mPaint = new Paint();
        this.mPaint.setAntiAlias(true);
        this.mPaint.setStrokeWidth(1.0F);
        this.mPaint.setStrokeCap(Paint.Cap.ROUND);
        this.mInterpolator = new LinearInterpolator();
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if(this.getChildCount() != 0 && this.getChildCount() <= 1) {
            if(changed && mScrollView == null) {
                View v = this.getChildAt(0);
                if(!(v instanceof TextView)) {
                    throw new RuntimeException("The child view of this MarqueeView must be a TextView instance.");
                }

                this.initView(this.getContext());
                this.prepareAnimation();
                if(this.mAutoStart) {
                    this.startMarquee();
                }
            }

        } else {
            throw new RuntimeException("MarqueeView must have exactly one child element.");
        }
    }

    public void startMarquee() {
        if(this.mMarqueeNeeded) {
            this.startTextFieldAnimation();
        }

        this.mCancelled = false;
        this.mStarted = true;
    }

    private void startTextFieldAnimation() {
        this.mAnimationStartRunnable = new Runnable() {
            public void run() {
                MarqueeView.this.mTextField.startAnimation(MarqueeView.this.mMoveTextOut);
            }
        };
        this.postDelayed(this.mAnimationStartRunnable, (long) this.mAnimationPause);
    }

    public void reset() {
        this.mCancelled = true;
        if(this.mAnimationStartRunnable != null) {
            this.removeCallbacks(this.mAnimationStartRunnable);
        }

        this.mTextField.clearAnimation();
        this.mStarted = false;
        this.mMoveTextOut.reset();
        this.invalidate();
    }

    private void prepareAnimation() {
        this.mPaint.setTextSize(this.mTextField.getTextSize());
        this.mPaint.setTypeface(this.mTextField.getTypeface());
        float mTextWidth = this.mPaint.measureText(this.mTextField.getText().toString());
        this.mMarqueeNeeded = mTextWidth > (float)this.getMeasuredWidth();
        this.mTextDifference = Math.abs(mTextWidth - (float)this.getMeasuredWidth()) + 5.0F;
        int duration = (int)(this.mTextDifference * (float)this.mSpeed);
        MarqueeView.this.mTextField.setX(MarqueeView.this.getMeasuredWidth());
        this.mMoveTextOut = new TranslateAnimation(0, -this.mTextDifference-2*this.getMeasuredWidth(), 0.0F, 0.0F);
        this.mMoveTextOut.setDuration((long) duration);
        this.mMoveTextOut.setInterpolator(this.mInterpolator);
        this.mMoveTextOut.setFillAfter(true);
        this.mMoveTextOut.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
                MarqueeView.this.expandTextView();
            }

            public void onAnimationEnd(Animation animation) {
                if (!MarqueeView.this.mCancelled) {
                    MarqueeView.this.mTextField.setX(MarqueeView.this.getMeasuredWidth());
                    MarqueeView.this.mTextField.startAnimation(MarqueeView.this.mMoveTextOut);
                }
            }

            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    private void initView(Context context) {
        LayoutParams sv1lp = new LayoutParams(-1, -2);
        sv1lp.gravity = 1;
        this.mScrollView = new ScrollView(context);
        this.mTextField = (TextView)this.getChildAt(0);
        this.removeView(this.mTextField);
        this.mScrollView.addView(this.mTextField, new android.widget.FrameLayout.LayoutParams(2000, -2));
        this.mTextField.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            public void afterTextChanged(Editable editable) {
                final boolean continueAnimation = MarqueeView.this.mStarted;
                MarqueeView.this.reset();
                MarqueeView.this.prepareAnimation();
                MarqueeView.this.post(new Runnable() {
                    public void run() {
                        if(continueAnimation) {
                            MarqueeView.this.startMarquee();
                        }

                    }
                });
            }
        });
        this.addView(this.mScrollView, sv1lp);
    }

    private void expandTextView() {
        android.view.ViewGroup.LayoutParams lp = this.mTextField.getLayoutParams();
        lp.width = (int)this.mPaint.measureText(this.mTextField.getText().toString());;
        this.mTextField.setLayoutParams(lp);
    }

}
