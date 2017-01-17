import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

/**
 * Progress view on circle. Creates a circle with a given color and drop shadow. Setting
 * animateProgress() will animate an arc on the border to the percentage given. 1 for complete, 0
 * for no border.
 */
public class CircleProgress extends View {

    /**
     * Logging tag.
     */
    static final String TAG = "CircleProgress";

    /**
     * Progress starts at the top of the circle.
     */
    public static final float START_TOP = -90;

    /**
     * Progress starts at the right side of the circle.
     */
    public static final float START_RIGHT = 0;

    /**
     * Progress starts at the bottom of the circle.
     */
    public static final float START_BOTTOM = 90;

    /**
     * Progress starts at the left side of the circle.
     */
    public static final float START_LEFT = 180;

    /**
     * Default border width.
     */
    private static final int DEFAULT_BORDER_WIDTH = 10;

    /**
     * Default border color.
     */
    private static final int DEFAULT_BORDER_COLOR = Color.BLUE;

    /**
     * Default circle color
     */
    private static final int DEFAULT_CIRCLE_COLOR = Color.WHITE;

    /**
     * Paint to use to draw the circle.
     */
    private Paint mMainPaint;

    /**
     * Paint to use to draw the progress meter arc.
     */
    private Paint mArcPaint;

    /**
     * Paint to use to draw the drop shadow.
     */
    private Paint mShadowPaint;

    /**
     * Current border withd.
     */
    private int mBorderWidth = 10;

    /**
     * The time it takes in milliseconds to animate the progress to the next set value.
     */
    private int mIncrementDuration = 500;

    /**
     * The blur radius of the drop shadow.
     */
    private int mShadowRadius = 20;

    /**
     * The current progress percentage. Value is from 0 - 1.
     */
    private float mProgress = 0;

    /**
     * The percentage the progress should go to.
     */
    private float mToProgress = 0;

    /**
     * The start angle of the progress arc.
     */
    private float mStartAngle = START_TOP;

    /**
     * Runnable instance to invoke when the progress meter reaches completion.
     */
    private Runnable mOnComplete;

    /**
     * The value animator for the progress arc.
     */
    private ValueAnimator mValueAnimator;

    public CircleProgress(Context context) {
        super(context);

        mMainPaint = new Paint();
        mMainPaint.setColor(DEFAULT_CIRCLE_COLOR);

        mArcPaint = new Paint();
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(DEFAULT_BORDER_WIDTH);
        mArcPaint.setColor(DEFAULT_BORDER_COLOR);
        mArcPaint.setAntiAlias(true);

        mShadowPaint = new Paint();
        setLayerType(LAYER_TYPE_SOFTWARE, mShadowPaint);
        mShadowPaint.setShadowLayer(mShadowRadius, 0, 0, Color.argb(128, 0, 0, 0));
    }

    /**
     * @see View#onDraw(Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2;
        float cy = getHeight() / 2;

        float delta = mShadowRadius + (mBorderWidth / 2);

        canvas.drawCircle(cx, cy, getWidth() / 2 - mShadowRadius, mShadowPaint);
        canvas.drawCircle(cx, cy, getWidth() / 2 - mShadowRadius, mMainPaint);
        canvas.drawArc(
            0 + delta, // left
            0 + delta, // right
            getWidth() - delta, // right
            getHeight() - delta, // bottom
            mStartAngle, // start angle
            360 * mProgress, // sweep
            false, // use center
            mArcPaint // paint
        );
    }

    /**
     * Animate the progress percentage to the given value.
     * @param progress The value to animate to.
     */
    public void animateProgress(float progress) {
        // Valid values for progress are only between 0 and 1
        if (progress < 0) {
            progress = 0;
        }

        if (progress > 1) {
            progress = 1;
        }

        final float toProgress = progress;

        // No change in progress, do nothing.
        if (toProgress == mToProgress) {
            return;
        }

        if (mValueAnimator != null) {
            mValueAnimator.cancel();
            mValueAnimator = null;
        }

        mValueAnimator = new ValueAnimator().ofFloat(mToProgress, toProgress);
        mValueAnimator.setDuration(mIncrementDuration);

        mValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float)animation.getAnimatedValue();
                mProgress = value;
                invalidate();
            }
        });

        mValueAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mToProgress = toProgress;
                if (mProgress >= 1 && mOnComplete != null) {
                    mOnComplete.run();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                mToProgress = toProgress;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        mValueAnimator.start();
    }

    /**
     * Set the duration the animation to a given percentage should take. This is in milliseconds.
     * @param duration The duration of the progress animation in milliseconds.
     */
    public void setIncrementDuration(int duration) {
        mIncrementDuration = duration;
    }

    /**
     * Set the starting angle of the progress meter arc.
     * @param startAngle The start angle in degrees from 0 to 360.
     */
    public void setStartAngle(float startAngle) {
        mStartAngle = startAngle;
    }

    /**
     * Set the runnable to invoke when the progress meter is complete.
     * @param runnable The runnable.
     */
    public void setOnComplete(Runnable runnable) {
        mOnComplete = runnable;
    }

    /**
     * Set the width of the progress meter.
     * @param width The border width.
     */
    public void setBorderWidth(int width) {
        mBorderWidth = width;
        mArcPaint.setStrokeWidth(mBorderWidth);
    }

    /**
     * Set the progress meter color.
     * @param color The color.
     */
    public void setBorderColor(int color) {
        mArcPaint.setColor(color);
    }

    /**
     * Set the circle color.
     * @param color
     */
    public void setCircleColor(int color) {
        mMainPaint.setColor(color);
    }

    /**
     * Reset the progress.
     */
    public void reset() {
        mToProgress = 0;
        mProgress = 0;
        invalidate();
    }
}
