package com.example.hello;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class DoubleSeekBar extends View {
    private static final String TAG = "DoubleSeekBar";
    private static final int CLICK_TYPE_NULL = 0;
    private static final int CLICK_TYPE_LEFT = 1;
    private static final int CLICK_TYPE_RIGHT = 2;

    private Bitmap leftIcon;
    private Bitmap rightIcon;
    private Bitmap clickIcon;

    private int maxValue;//最大值
    private int minValue;//最小值
    private int leftValue, rightValue;
    private int defaultWidth = 0;
    private int defaultHeight = 0;

    private float bgHeight;
    private float paddingLeft, paddingRight;
    private float indexLeftX, indexLeftY;//左按钮的坐标值
    private float indexRightX, indexRightY;//右按钮的坐标值

    private int viewWidth, viewHeight;
    private float startLeftX, startRightX;//左右按钮的开始x轴坐标值

    private int clickedType = CLICK_TYPE_NULL;//0 没有点击，1点击中左按钮，2点击中右按钮

    private Paint paintBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint paintBg2 = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private OnChanged mOnChanged;


    public DoubleSeekBar(Context context) {
        this(context, null);
    }

    public DoubleSeekBar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DoubleSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        bgHeight = dp2px(2);//设置背景线条的高度

        leftIcon = getBitmap(R.drawable.icon_seek_bar);
        rightIcon = getBitmap(R.drawable.icon_seek_bar);
        clickIcon = getBitmap(R.drawable.icon_seek_bar);
        paddingLeft = dp2px( 16);
        paddingRight = dp2px( 16);

        paintBg.setColor(getResources().getColor(R.color.md_red_A400));
        paintBg2.setColor(getResources().getColor(R.color.md_grey_400));
        maxValue = 100;//default maxvalue
        minValue = 0;//default minvalue
        leftValue = minValue;
        rightValue = maxValue;

        defaultWidth = dp2px( 100);
        defaultHeight = dp2px( 50);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    private int dp2px(float dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
                getResources().getDisplayMetrics());
    }

    private int sp2px(float sp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp,
                getResources().getDisplayMetrics());
    }
    private Bitmap getBitmap(int drawableRes) {
        Drawable drawable = getResources().getDrawable(drawableRes);
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(dp2px(35), dp2px(35), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, dp2px(35), dp2px(35));
        drawable.draw(canvas);

        return bitmap;
    }

    public void setMaxValue(int maxValue) {
        if (maxValue < 0) {
            maxValue = 0;
        }
        this.maxValue = maxValue;
    }

    public void setMinValue(int minValue) {
        if (minValue < 0) {
            minValue = 0;
        }
        this.minValue = minValue;
    }

    public void setOnChanged(OnChanged mOnChanged) {
        this.mOnChanged = mOnChanged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isClickedIcon(event)) {
                    postInvalidate();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                clickedType = CLICK_TYPE_NULL;
                postInvalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                if (handleMoveEvent(event)) {
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 判断是否点击到按钮了
     *
     * @param event
     * @return
     */
    private boolean isClickedIcon(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (x < indexLeftX + leftIcon.getWidth() && x > indexLeftX && y > indexLeftY && y < indexLeftY + leftIcon.getHeight()) {
            clickedType = CLICK_TYPE_LEFT;
            return true;
        }

        if (x < indexRightX + rightIcon.getWidth() && x > indexRightX && y > indexRightY && y < indexRightY + rightIcon.getHeight()) {
            clickedType = CLICK_TYPE_RIGHT;
            return true;
        }
        clickedType = CLICK_TYPE_NULL;

        return false;
    }

    /**
     * 滑动事件处理
     *
     * @param motionEvent
     */
    private boolean handleMoveEvent(MotionEvent motionEvent) {
        Log.d(TAG, "handleMoveEvent: start");
        float x = motionEvent.getX();
        if (clickedType == CLICK_TYPE_LEFT) {
            if (x < indexRightX - leftIcon.getWidth() && x > startLeftX) {//左按钮的范围小于右按钮的位置，大于初始值位置
                indexLeftX = x;
            }
            Log.d(TAG, "handleMoveEvent: start indexLeftX = " + indexLeftX);
        } else if (clickedType == CLICK_TYPE_RIGHT) {
            if (x > indexLeftX + rightIcon.getWidth() && x < startRightX) {//右按钮的范围大于左按钮的位置，小于初始值位置
                indexRightX = x;
            }
            Log.d(TAG, "handleMoveEvent: start indexRightX = " + indexRightX);
        }

        leftValue = (int) ((maxValue - minValue) * (indexLeftX - startLeftX) / (startRightX - startLeftX));

        rightValue = (int) ((maxValue - minValue) * (indexRightX - startLeftX) / (startRightX - startLeftX));

        if (mOnChanged != null) {
            mOnChanged.onChange(leftValue, rightValue);
        }
        Log.d(TAG, "handleMoveEvent: start leftValue = " + leftValue + " rightValue = " + rightValue);
        postInvalidate();
        return true;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

    }


    /**
     * 注意，在LinearLayout布局中，设置weight = 1时，onlayout 会多次回调，导致indexL(/R)X值一直为默认值，导致无法滑动
     * 可以将布局改成releativeLayout。
     * @param changed
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d(TAG, "onLayout: ");
        viewWidth = getWidth();
        viewHeight = getHeight();

        //左右按钮的初始值位置计算
        startLeftX = 0 + paddingLeft - leftIcon.getWidth() / 2;
        indexLeftX = startLeftX;
        indexLeftY = viewHeight / 2 - bgHeight / 2 - leftIcon.getHeight() / 2;



        startRightX = viewWidth - paddingRight - rightIcon.getWidth() / 2;
        indexRightX = startRightX;
        indexRightY = viewHeight / 2 - bgHeight / 2 - rightIcon.getHeight() / 2;


        leftValue = (int) ((maxValue - minValue) * (indexLeftX - startLeftX) / (startRightX - startLeftX));

        rightValue = (int) ((maxValue - minValue) * (indexRightX - startLeftX) / (startRightX - startLeftX));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw: getWidth = " + viewWidth);
        drawBg(canvas);
        drawLeftIcon(canvas);
        drawRightIcon(canvas);
    }

    /**
     * 绘制背景
     *
     * @param canvas
     */
    private void drawBg(Canvas canvas) {

        paintBg.setAntiAlias(true);


        //画两端的半圆
        float circleLeftCenterX = 0 + paddingLeft;
        float circleRightCenterX = viewWidth - paddingRight;
        canvas.drawCircle(circleLeftCenterX, viewHeight / 2, bgHeight / 2, paintBg);
        canvas.drawCircle(circleRightCenterX, viewHeight / 2, bgHeight / 2, paintBg);

        canvas.drawRect(
                new RectF(circleLeftCenterX, viewHeight / 2 - bgHeight / 2, circleRightCenterX, viewHeight / 2 + bgHeight / 2)
                , paintBg);

        //左右按钮滑动区域之外绘制其他颜色
        int centerLeftIconX = (int) (indexLeftX + leftIcon.getWidth() / 2);//左按钮图标的中心点
        int centerRightIconX = (int) (indexRightX + rightIcon.getWidth() / 2);//右按钮图标的中心点

        if (centerLeftIconX > circleLeftCenterX) {
            canvas.drawCircle(circleLeftCenterX, viewHeight / 2, bgHeight / 2, paintBg2);

            canvas.drawRect(
                    new RectF(circleLeftCenterX, viewHeight / 2 - bgHeight / 2, centerLeftIconX, viewHeight / 2 + bgHeight / 2)
                    , paintBg2);
        }

        if (centerRightIconX < circleRightCenterX) {
            canvas.drawCircle(circleRightCenterX, viewHeight / 2, bgHeight / 2, paintBg2);
            canvas.drawRect(
                    new RectF(centerRightIconX, viewHeight / 2 - bgHeight / 2, circleRightCenterX, viewHeight / 2 + bgHeight / 2)
                    , paintBg2);
        }
    }

    /**
     * 绘制左按钮
     * @param canvas
     */
    private void drawLeftIcon(Canvas canvas) {
        if (clickedType == CLICK_TYPE_LEFT) {
            iconPaint.setMaskFilter(new BlurMaskFilter(50, BlurMaskFilter.Blur.INNER));

            canvas.drawBitmap(clickIcon, indexLeftX, indexLeftY, iconPaint);
        } else {
            iconPaint.setMaskFilter(null);
            canvas.drawBitmap(leftIcon, indexLeftX, indexLeftY, iconPaint);
        }
    }

    /**
     * 绘制右按钮
     * @param canvas
     */
    private void drawRightIcon(Canvas canvas) {
        Log.d(TAG, "drawLeftIcon: getWidht = " + viewWidth + " indexRightX= " + indexRightX + " indexRightY = " + indexRightY);
        if (clickedType == CLICK_TYPE_RIGHT) {
            iconPaint.setMaskFilter(new BlurMaskFilter(50, BlurMaskFilter.Blur.INNER));
            canvas.drawBitmap(clickIcon, indexRightX, indexRightY, iconPaint);
        } else {
            iconPaint.setMaskFilter(null);
            canvas.drawBitmap(rightIcon, indexRightX, indexRightY, iconPaint);

        }
    }


    /**
     * 回调接口，回调左右值
     */
    public interface OnChanged {
        void onChange(int leftValue, int rightValue);
    }


    /**
     * 重写onMeasure方法，设置wrap_content 时需要默认大小
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defaultWidth, defaultHeight);
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(defaultWidth, heightSpecSize);
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, defaultHeight);
        }
    }
}