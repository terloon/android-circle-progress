import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
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
     * Value to use for clockwise rotation.
     */
    private static final int ROTATION_CLOCKWISE = 360;

    /**
     * Value to use for counter clockwise rotation.
     */
    private static final int ROTATION_COUNTER_CLOCKWISE = -360;

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
     * Paint for icon image.
     */
    private Paint mIconPaint;

    /**
     * Paint to use for next icon image to use.
     */
    private Paint mNextIconPaint;

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
     * The current progress rotation.
     */
    private float mRotation = ROTATION_CLOCKWISE;

    /**
     * Runnable instance to invoke when the progress meter reaches completion.
     */
    private Runnable mOnComplete;

    /**
     * Icon image to display at the center of the view.
     */
    private Bitmap mIconImage;

    /**
     * The next icon image to display at the center. Will perform a transition between current and
     * this new image.
     */
    private Bitmap mNextImage;

    private float mFadeAlpha;

    /**
     * The value animator for the progress arc.
     */
    private ValueAnimator mValueAnimator;

    public CircleProgress(Context context) {
        super(context);

        mMainPaint = new Paint();
        mMainPaint.setColor(DEFAULT_CIRCLE_COLOR);
        mMainPaint.setAntiAlias(true);

        mArcPaint = new Paint();
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(DEFAULT_BORDER_WIDTH);
        mArcPaint.setColor(DEFAULT_BORDER_COLOR);
        mArcPaint.setAntiAlias(true);

        mShadowPaint = new Paint();
        setLayerType(LAYER_TYPE_SOFTWARE, mShadowPaint);
        mShadowPaint.setShadowLayer(mShadowRadius, 0, 0, Color.argb(128, 0, 0, 0));

        mIconPaint = new Paint();
        mNextIconPaint = new Paint();
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
            mRotation * mProgress, // sweep
            false, // use center
            mArcPaint // paint
        );

        if (mIconImage != null) {
            mIconPaint.setAlpha((int)(255 * (1 - mFadeAlpha)));
            canvas.drawBitmap(mIconImage, (getWidth() - mIconImage.getWidth()) / 2, (getHeight() - mIconImage.getHeight()) / 2, mIconPaint);
        }

        if (mNextImage != null) {
            mNextIconPaint.setAlpha((int)(255 * mFadeAlpha));
            canvas.drawBitmap(mNextImage, (getWidth() - mNextImage.getWidth()) / 2, (getHeight() - mNextImage.getHeight()) / 2, mNextIconPaint);
        }
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

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
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
        };

        Context context = getContext();
        if (context instanceof Activity) {
            Activity activity = (Activity)context;
            activity.runOnUiThread(runnable);
        }
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

    /**
     * Set the resource id of the icon to use in the middle of the circle view.
     * @param resourceId The resource id of the drawable.
     */
    public void setIcon(int resourceId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), resourceId);
        mIconImage = bitmap;
    }

    /**
     * Set whether the rotation of the progress is clockwise or counter clockwise.
     * @param clockwise
     */
    public void setRotationClockwise(boolean clockwise) {
        mRotation = clockwise ? ROTATION_CLOCKWISE : ROTATION_COUNTER_CLOCKWISE;
    }

    /**
     * Animate to the next icon to use. This is centered in the middle of the circle view.
     * @param resourceId The resource id of the drawable to use.
     */
    public void animateNextImage(int resourceId) {
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), resourceId);

        mNextImage = bitmap;
        mFadeAlpha = 0;

        ValueAnimator anim = new ValueAnimator().ofFloat(0, 1);
        anim.setDuration(500);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float)animation.getAnimatedValue();
                mFadeAlpha = value;
                invalidate();
            }
        });

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mIconImage = mNextImage;
                mNextImage = null;
                mFadeAlpha = 0;
                mIconPaint.setAlpha(255);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.start();
    }
}
