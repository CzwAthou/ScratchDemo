package com.athou.scratchdemo;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

/**
 * Created by athou on 2017/3/16.
 */

public class ScratchView extends View {

    private final static float DEFAULT_ERASER_SIZE = 60F;
    public final static int DEFAULT_MASK_COLOR = Color.GRAY;

    private int mTouchSlop = 0;

    private Paint bpPaint;

    private Paint eraserPaint;

    private Paint maskPaint;
    private Canvas maskCanvas;
    private Bitmap maskBitmap;

    private float eraseSize = DEFAULT_ERASER_SIZE;
    private int maskColor = DEFAULT_MASK_COLOR;
    private BitmapDrawable waterMarkDrawable = null;

    private Path eraserPath = null;

    public ScratchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ScratchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ScratchView);
        maskColor = typedArray.getColor(R.styleable.ScratchView_mask_color, DEFAULT_MASK_COLOR);
        eraseSize = typedArray.getFloat(R.styleable.ScratchView_eraser_size, DEFAULT_ERASER_SIZE);
        int waterMarkId = typedArray.getColor(R.styleable.ScratchView_water_mark, -1);
        typedArray.recycle();

        setWaterMark(waterMarkId);

        bpPaint = new Paint();
        bpPaint.setAntiAlias(true);
        bpPaint.setFilterBitmap(true);
        bpPaint.setDither(true);

        maskPaint = new Paint();
        maskPaint.setAntiAlias(true);
        maskPaint.setDither(true);//防抖
        maskPaint.setStyle(Paint.Style.FILL);
        setMaskColor(maskColor);

        eraserPaint = new Paint();
        eraserPaint.setAntiAlias(true);
        eraserPaint.setDither(true);
        eraserPaint.setFilterBitmap(true);
        eraserPaint.setStyle(Paint.Style.STROKE);
        eraserPaint.setStrokeCap(Paint.Cap.ROUND);//设置笔尖形状，让绘制的边缘圆滑
        eraserPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        setEraserSize(eraseSize);

        eraserPath = new Path();

        ViewConfiguration viewConfiguration = ViewConfiguration.get(getContext());
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
    }

    /**
     * 设置水印图标
     *
     * @param waterMarkId 图标资源id，-1表示去除水印
     */
    public void setWaterMark(int waterMarkId) {
        if (waterMarkId <= 0) {
            waterMarkDrawable = null;
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), waterMarkId);
        waterMarkDrawable = new BitmapDrawable(getResources(), bitmap);
        waterMarkDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
    }

    /**
     * 设置蒙板颜色
     *
     * @param maskColor 十六进制颜色值，如：0xffff0000（不透明的红色）
     */
    public void setMaskColor(int maskColor) {
        maskPaint.setColor(maskColor);
    }

    /**
     * 设置橡皮檫尺寸大小（默认大小是 60）
     *
     * @param eraserSize 橡皮檫尺寸大小
     */
    public void setEraserSize(float eraserSize) {
        eraserPaint.setStrokeWidth(eraserSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(maskBitmap, 0, 0, bpPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        createMask(w, h);
    }

    private void createMask(int w, int h) {
        maskBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        maskCanvas = new Canvas(maskBitmap);

        Rect rect = new Rect(0, 0, w, h);
        maskCanvas.drawRect(rect, maskPaint);

        if (waterMarkDrawable != null) {
            waterMarkDrawable.setBounds(new Rect(rect));
            waterMarkDrawable.draw(maskCanvas);
        }
    }

    public void reset() {
        startX = 0;
        startY = 0;

        createMask(getWidth(), getHeight());
        invalidate();

        updateErasePercent();
    }

    public void clear() {
        startX = 0;
        startY = 0;

        maskBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        maskCanvas = new Canvas(maskBitmap);
        Rect rect = new Rect(0, 0, getWidth(), getHeight());
        maskCanvas.drawRect(rect, eraserPaint);
        invalidate();

        updateErasePercent();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startErase(event.getX(), event.getY());
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                doErase(event.getX(), event.getY());
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
                stopErase();
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    float startX;
    float startY;

    private void startErase(float x, float y) {
        startX = x;
        startY = y;

        eraserPath.reset();
        eraserPath.moveTo(x, y);
    }

    private void doErase(float x, float y) {
        float dx = Math.abs(startX - x);
        float dy = Math.abs(startY - y);
        if (dx >= mTouchSlop || dy >= mTouchSlop) {
            eraserPath.lineTo(x, y);
            maskCanvas.drawPath(eraserPath, eraserPaint);

            startX = x;
            startX = y;
        }
    }

    private void stopErase() {
        startX = 0;
        startY = 0;

        eraserPath.reset();
        updateErasePercent();
    }

    private void updateErasePercent() {
        int w = getWidth();
        int h = getHeight();
        new AsyncTask<Integer, Float, Boolean>() {

            @Override
            protected Boolean doInBackground(Integer... params) {
                int w = params[0];
                int h = params[1];
                int[] pixels = new int[maskBitmap.getByteCount()];
                maskBitmap.getPixels(pixels, 0, w, 0, 0, w, h);

                int eraseCount = 0;
                int totalCount = w * h;
                for (int i = 0; i < totalCount; i++) {
                    if (pixels[i] == 0) {
                        eraseCount++;
                    }
                }
                float percent = eraseCount * 1f / totalCount * 100;
                publishProgress(percent);
                return null;
            }

            @Override
            protected void onProgressUpdate(Float... values) {
                super.onProgressUpdate(values);
                if (onPercentChangeListener != null & values != null) {
                    onPercentChangeListener.onChange(values[0]);
                }
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
            }
        }.execute(w, h);
    }

    OnPercentChangeListener onPercentChangeListener;

    public void setOnPercentChangeListener(OnPercentChangeListener onPercentChangeListener) {
        this.onPercentChangeListener = onPercentChangeListener;
    }

    interface OnPercentChangeListener {
        void onChange(float percent);
    }
}
