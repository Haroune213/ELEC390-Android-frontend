package com.example.myapplication.UI;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class GaugeView extends View {

    private float value = 0f;
    private float maxValue = 100f;
    private float minValue = 0f;
    private String title = "Gauge";

    private float lowDanger = 20f;
    private float lowWarning = 30f;
    private float highWarning = 70f;
    private float highDanger = 80f;

    private int safeColor = Color.parseColor("#2ECCA3");
    private int warningColor = Color.YELLOW;
    private int dangerColor = Color.RED;

    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint valuePaint;
    private Paint labelPaint;
    private Paint titlePaint;

    public GaugeView(Context context) {
        super(context);
        init();
    }

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GaugeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(Color.LTGRAY);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(30f);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setColor(safeColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeWidth(30f);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        valuePaint.setColor(Color.DKGRAY);
        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setTextSize(64f);
        valuePaint.setTypeface(Typeface.DEFAULT_BOLD);

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.GRAY);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTextSize(32f);

        titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(Color.GRAY);
        titlePaint.setTextAlign(Paint.Align.CENTER);
        titlePaint.setTextSize(36f);
    }

    public void setTitle(String newTitle) {
        title = newTitle;
        invalidate();
    }

    public void setMinValue(float minValue) {
        this.minValue = minValue;
        invalidate();
    }

    public void setMaxValue(float maxValue) {
        if (maxValue <= minValue) {
            this.maxValue = minValue + 1f;
        } else {
            this.maxValue = maxValue;
        }

        if (value > this.maxValue) {
            value = this.maxValue;
        }
        if (value < minValue) {
            value = minValue;
        }

        updateGaugeColor(value);
        invalidate();
    }

    public void setRanges(float lowDanger, float lowWarning, float highWarning, float highDanger) {
        this.lowDanger = lowDanger;
        this.lowWarning = lowWarning;
        this.highWarning = highWarning;
        this.highDanger = highDanger;
        updateGaugeColor(value);
        invalidate();
    }

    public void setGaugeColors(int safeColor, int warningColor, int dangerColor) {
        this.safeColor = safeColor;
        this.warningColor = warningColor;
        this.dangerColor = dangerColor;
        updateGaugeColor(value);
        invalidate();
    }

    public void setValue(float newValue) {
        if (newValue < minValue) {
            value = minValue;
        } else if (newValue > maxValue) {
            value = maxValue;
        } else {
            value = newValue;
        }

        updateGaugeColor(value);
        invalidate();
    }

    public void setValueAnimated(float newValue) {
        float targetValue;

        if (newValue < minValue) {
            targetValue = minValue;
        } else if (newValue > maxValue) {
            targetValue = maxValue;
        } else {
            targetValue = newValue;
        }

        ValueAnimator animator = ValueAnimator.ofFloat(value, targetValue);
        animator.setDuration(1000);
        animator.addUpdateListener(animation -> {
            value = (float) animation.getAnimatedValue();
            updateGaugeColor(value);
            invalidate();
        });
        animator.start();
    }

    private void updateGaugeColor(float currentValue) {
        if (currentValue <= lowDanger || currentValue >= highDanger) {
            progressPaint.setColor(dangerColor);
        } else if ((currentValue > lowDanger && currentValue <= lowWarning)
                || (currentValue >= highWarning && currentValue < highDanger)) {
            progressPaint.setColor(warningColor);
        } else {
            progressPaint.setColor(safeColor);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float w = getWidth();
        float h = getHeight();

        float size = Math.min(w, h * 2);
        float padding = 50f;

        float left = (w - size) / 2f + padding;
        float top = padding;
        float right = (w + size) / 2f - padding;
        float bottom = size - padding;

        RectF rect = new RectF(left, top, right, bottom);

        canvas.drawArc(rect, 180f, 180f, false, backgroundPaint);

        float range = maxValue - minValue;
        float sweepAngle = ((value - minValue) / range) * 180f;
        canvas.drawArc(rect, 180f, sweepAngle, false, progressPaint);

        canvas.drawText(String.valueOf((int) value), w / 2f, h * 0.68f, valuePaint);
        canvas.drawText(title, w / 2f, h * 0.82f, titlePaint);

        canvas.drawText(String.valueOf((int) minValue), left, h * 0.95f, labelPaint);
        canvas.drawText(String.valueOf((int) maxValue), right, h * 0.95f, labelPaint);
    }
}