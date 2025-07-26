package com.tagmarshal.golf.dialog;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class CircularTimerView extends View {

    private Paint progressPaint;
    private Paint backgroundPaint;
    private Paint textPaint;

    private int progress = 100; // Progress percentage (0 to 100)
    private int maxProgress = 100; // Maximum progress
    private String timerText = "30"; // Center text

    public CircularTimerView(Context context) {
        super(context);
        init();
    }

    public CircularTimerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularTimerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setColor(Color.YELLOW);
        progressPaint.setStrokeWidth(15f);

        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setColor(Color.DKGRAY);
        backgroundPaint.setStrokeWidth(15f);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.YELLOW);
        textPaint.setTextSize(50f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
    }

    public void setProgress(int percentage, String seconds) {
        this.progress = percentage;
        this.timerText = seconds;
        invalidate(); // Trigger a redraw
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Circle dimensions
        float width = getWidth();
        float height = getHeight();
        float radius = Math.min(width, height) / 2.5f;
        float centerX = width / 2;
        float centerY = height / 2;

        // Draw background circle
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);

        // Draw progress arc
        float sweepAngle = (progress / (float) maxProgress) * 360;
        RectF rect = new RectF(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );
        canvas.drawArc(rect, -90f, sweepAngle, false, progressPaint);

        // Draw center text
        canvas.drawText(timerText, centerX, centerY + (textPaint.getTextSize() / 3), textPaint);
    }
}
