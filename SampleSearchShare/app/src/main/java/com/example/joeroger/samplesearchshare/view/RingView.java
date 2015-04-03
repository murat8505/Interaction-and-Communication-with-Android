package com.example.joeroger.samplesearchshare.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;

import com.example.joeroger.samplesearchshare.R;


public class RingView extends View {

    private Paint leftPaint = new Paint();
    private Paint rightPaint = new Paint();
    private int leftColor = Color.GREEN;
    private int rightColor = Color.BLUE;
    private int strokeSize = 8;
    private int progress = 50;
    private int height = 0;
    private int width = 0;
    private int centerHeight = 0;
    private int centerWidth = 0;
    private int rightSweepAngle = 180;
    private int leftSweepAngle = 180;

    private RectF rectF = new RectF();


    public RingView(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public RingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public RingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RingView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        leftPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        leftPaint.setStyle(Paint.Style.STROKE);

        rightPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        rightPaint.setStyle(Paint.Style.STROKE);

        // If no attribute set, no ability to pull resources so go with defaults
        if (attrs == null) {
            invalidatePaintAndMeasurements(false);
            return;
        }

        // Load attributes
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.RingView, defStyleAttr, defStyleRes);

        leftColor = a.getColor(R.styleable.RingView_leftColor, leftColor);
        rightColor = a.getColor(R.styleable.RingView_rightColor, rightColor);
        strokeSize = a.getDimensionPixelSize(R.styleable.RingView_paintSize, strokeSize);
        progress = a.getInteger(R.styleable.RingView_progress, progress);

        a.recycle();

        if (progress < 0 || progress > 100) {
            throw new IllegalStateException("Progress must be between 0 and 100");
        }

        invalidatePaintAndMeasurements(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int paddingStart = ViewCompat.getPaddingStart(this);
        int paddingEnd = ViewCompat.getPaddingEnd(this);

        if (height != h || width != w) {
            height = h;
            width = w;
            invalidatePaintAndMeasurements(false);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.rotate(-90, centerWidth, centerHeight);
        canvas.drawArc(rectF, 0, rightSweepAngle, false, rightPaint);
        canvas.drawArc(rectF, rightSweepAngle, leftSweepAngle, false, leftPaint);
        canvas.restore();
    }

    private void invalidatePaintAndMeasurements(boolean invalidateView) {
        leftPaint.setColor(leftColor);
        leftPaint.setStrokeWidth(strokeSize);

        rightPaint.setColor(rightColor);
        rightPaint.setStrokeWidth(strokeSize);

        rightSweepAngle = 360 * progress / 100;
        leftSweepAngle = 360 - rightSweepAngle;

        int paddingStart = ViewCompat.getPaddingStart(this);
        int paddingEnd = ViewCompat.getPaddingEnd(this);
        int adjustedHeight = height - getPaddingTop() - getPaddingBottom();
        int adjustedWidth = width - paddingStart - paddingEnd;

        // Find center point with respect to padding
        centerHeight = adjustedHeight / 2 + getPaddingTop();
        centerWidth = adjustedWidth / 2 + paddingStart;

        int halfSmallestSide = (adjustedWidth < adjustedHeight ? adjustedWidth : adjustedHeight) / 2;
        int strokeAdjustment = strokeSize / 2;

        rectF.set(centerWidth - halfSmallestSide + strokeAdjustment,
                centerHeight - halfSmallestSide + strokeAdjustment,
                centerWidth + halfSmallestSide - strokeAdjustment,
                centerHeight + halfSmallestSide - strokeAdjustment);

        if (invalidateView) {
            invalidate();
        }
    }

    public int getLeftColor() {
        return leftColor;
    }

    public void setLeftColor(int leftColor) {
        this.leftColor = leftColor;
        invalidatePaintAndMeasurements(true);
    }

    public int getRightColor() {
        return rightColor;
    }

    public void setRightColor(int rightColor) {
        this.rightColor = rightColor;
        invalidatePaintAndMeasurements(true);
    }

    public int getStrokeSize() {
        return strokeSize;
    }

    public void setStrokeSize(int strokeSize) {
        this.strokeSize = strokeSize;
        invalidatePaintAndMeasurements(true);
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
        invalidatePaintAndMeasurements(true);
    }
}
