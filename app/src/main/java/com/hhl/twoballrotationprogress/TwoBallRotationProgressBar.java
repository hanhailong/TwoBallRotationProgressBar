package com.hhl.twoballrotationprogress;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.annotation.Keep;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * 两个颜色的小球循环旋转，
 * 目前只支持代码设置颜色、最大半径、最小半径等属性
 * version 1.0
 * //TODO 2.0 添加xml自定义属性支持
 * Created by HanHailong on 15/8/07.
 */
public class TwoBallRotationProgressBar extends View {

    //默认小球最大半径
    private final static int DEFAULT_MAX_RADIUS = 15;
    //默认小球最小半径
    private final static int DEFAULT_MIN_RADIUS = 5;
    //默认两个小球运行轨迹直径距离
    private final static int DEFAULT_DISTANCE = 20;

    //默认第一个小球颜色
    private final static int DEFAULT_ONE_BALL_COLOR = Color.parseColor("#40df73");
    //默认第二个小球颜色
    private final static int DEFAULT_TWO_BALL_COLOR = Color.parseColor("#ffdf3e");

    //默认动画执行时间
    private final static int DEFAULT_ANIMATOR_DURATION = 1000;


    //画笔
    private Paint mPaint;

    //球的最大半径
    private float maxRadius = DEFAULT_MAX_RADIUS;
    //球的最小半径
    private float minRadius = DEFAULT_MIN_RADIUS;

    //两球旋转的范围距离
    private int distance = DEFAULT_DISTANCE;

    //动画的时间
    private long duration = DEFAULT_ANIMATOR_DURATION;

    private Ball mOneBall;
    private Ball mTwoBall;

    private float mCenterX;
    private float mCenterY;

    private AnimatorSet animatorSet;

    public TwoBallRotationProgressBar(Context context) {
        this(context, null);
    }

    public TwoBallRotationProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwoBallRotationProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        mOneBall = new Ball();
        mTwoBall = new Ball();

        mOneBall.setColor(DEFAULT_ONE_BALL_COLOR);
        mTwoBall.setColor(DEFAULT_TWO_BALL_COLOR);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        configAnimator();

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mCenterX = w / 2;
        mCenterY = h / 2;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mCenterX = getWidth() / 2;
        mCenterY = getHeight() / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //画两个小球，半径小的先画，半径大的后画
        if (mOneBall.getRadius() > mTwoBall.getRadius()) {
            mPaint.setColor(mTwoBall.getColor());
            canvas.drawCircle(mTwoBall.getCenterX(), mCenterY, mTwoBall.getRadius(), mPaint);

            mPaint.setColor(mOneBall.getColor());
            canvas.drawCircle(mOneBall.getCenterX(), mCenterY, mOneBall.getRadius(), mPaint);
        } else {
            mPaint.setColor(mOneBall.getColor());
            canvas.drawCircle(mOneBall.getCenterX(), mCenterY, mOneBall.getRadius(), mPaint);

            mPaint.setColor(mTwoBall.getColor());
            canvas.drawCircle(mTwoBall.getCenterX(), mCenterY, mTwoBall.getRadius(), mPaint);
        }
    }

    /**
     * 配置属性动画
     */
    private void configAnimator() {

        //中间半径大小
        float centerRadius = (maxRadius + minRadius) * 0.5f;

        //第一个小球缩放动画，通过改变小球的半径
        //半径变化规律：中间大小->最大->中间大小->最小->中间大小
        ObjectAnimator oneScaleAnimator = ObjectAnimator.ofFloat(mOneBall, "radius",
                centerRadius, maxRadius, centerRadius, minRadius, centerRadius);
        //无限循环
        oneScaleAnimator.setRepeatCount(ValueAnimator.INFINITE);


        //第一个小球位移动画，通过改变小球的圆心
        ValueAnimator oneCenterAnimator = ValueAnimator.ofFloat(-1, 0, 1, 0, -1);
        oneCenterAnimator.setRepeatCount(ValueAnimator.INFINITE);
        oneCenterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                float x = mCenterX + (distance) * value;
                mOneBall.setCenterX(x);
                //不停的刷新view，让view不停的重绘
                invalidate();
            }
        });

        //第二个小球缩放动画
        //变化规律：中间大小->最小->中间大小->最大->中间大小
        ObjectAnimator twoScaleAnimator = ObjectAnimator.ofFloat(mTwoBall, "radius", centerRadius, minRadius,
                centerRadius, maxRadius, centerRadius);
        twoScaleAnimator.setRepeatCount(ValueAnimator.INFINITE);

        //第二个小球位移动画
        ValueAnimator twoCenterAnimator = ValueAnimator.ofFloat(1, 0, -1, 0, 1);
        twoCenterAnimator.setRepeatCount(ValueAnimator.INFINITE);
        twoCenterAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (Float) animation.getAnimatedValue();
                float x = mCenterX + (distance) * value;
                mTwoBall.setCenterX(x);
            }
        });

        //属性动画集合
        animatorSet = new AnimatorSet();
        //四个属性动画一块执行
        animatorSet.playTogether(oneScaleAnimator, oneCenterAnimator, twoScaleAnimator, twoCenterAnimator);
        //动画一次运行时间
        animatorSet.setDuration(DEFAULT_ANIMATOR_DURATION);
        //时间插值器，这里表示动画开始最快，结尾最慢
        animatorSet.setInterpolator(new DecelerateInterpolator());
    }

    /**
     * 小球
     */
    public class Ball {
        @Keep
        private float radius;//半径
        private float centerX;//圆心
        private int color;//颜色

        @Keep
        public float getRadius() {
            return radius;
        }

        @Keep
        public void setRadius(float radius) {
            this.radius = radius;
        }

        public float getCenterX() {
            return centerX;
        }

        public void setCenterX(float centerX) {
            this.centerX = centerX;
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }
    }

    @Override
    public void setVisibility(int v) {
        if (getVisibility() != v) {
            super.setVisibility(v);
            if (v == GONE || v == INVISIBLE) {
                stopAnimator();
            } else {
                startAnimator();
            }
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int v) {
        super.onVisibilityChanged(changedView, v);
        if (v == GONE || v == INVISIBLE) {
            stopAnimator();
        } else {
            startAnimator();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAnimator();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimator();
    }

    /**
     * 设置第一个球的颜色
     *
     * @param color
     */
    public void setOneBallColor(@ColorInt int color) {
        mOneBall.setColor(color);
    }

    /**
     * 设置第二个球的颜色
     *
     * @param color
     */
    public void setmTwoBallColor(@ColorInt int color) {
        mTwoBall.setColor(color);
    }

    /**
     * 设置球的最大半径
     *
     * @param maxRadius
     */
    public void setMaxRadius(float maxRadius) {
        this.maxRadius = maxRadius;
        configAnimator();
    }

    /**
     * 设置球的最小半径
     *
     * @param minRadius
     */
    public void setMinRadius(float minRadius) {
        this.minRadius = minRadius;
        configAnimator();
    }

    /**
     * 设置两个球旋转的最大范围距离
     *
     * @param distance
     */
    public void setDistance(int distance) {
        this.distance = distance;
    }

    public void setDuration(long duration) {
        this.duration = duration;
        if (animatorSet != null) {
            animatorSet.setDuration(duration);
        }
    }

    /**
     * 开始动画
     */
    public void startAnimator() {
        if (getVisibility() != VISIBLE) return;

        if (animatorSet.isRunning()) return;

        if (animatorSet != null) {
            animatorSet.start();
        }
    }

    /**
     * 结束停止动画
     */
    public void stopAnimator() {
        if (animatorSet != null) {
            animatorSet.end();
        }
    }
}
