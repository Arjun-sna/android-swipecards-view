package in.arjsna.swipecardlib;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;


public class FlingCardListener implements View.OnTouchListener {

    private static final String TAG = FlingCardListener.class.getSimpleName();

    private static final int INVALID_POINTER_ID = -1;

    private final SwipeCardView parentView;

    private Rect RECT_TOP;

    private Rect RECT_BOTTOM;

    private Rect RECT_LEFT;

    private Rect RECT_RIGHT;

    private final float objectX;

    private final float objectY;

    private final int objectH;

    private final int objectW;

    private final int parentWidth;

    private final FlingListener mFlingListener;

    private final Object dataObject;

    private final float halfWidth;

    private final float halfHeight;

    private final int parentHeight;

    private float BASE_ROTATION_DEGREES;

    private float aPosX;

    private float aPosY;

    private float aDownTouchX;

    private float aDownTouchY;

    // The active pointer is the one currently moving our object.
    private int mActivePointerId = INVALID_POINTER_ID;

    private View frame = null;

    private final int TOUCH_ABOVE = 0;

    private final int TOUCH_BELOW = 1;

    private int touchPosition;

    private boolean isAnimationRunning = false;

    private float MAX_COS = (float) Math.cos(Math.toRadians(45));


    public FlingCardListener(SwipeCardView parent, View frame, Object itemAtPosition, FlingListener flingListener) {
        this(parent, frame, itemAtPosition, 15f, flingListener);
    }

    public FlingCardListener(SwipeCardView parent, View frame, Object itemAtPosition, float rotation_degrees, FlingListener flingListener) {
        super();
        this.parentView = parent;
        this.frame = frame;
        this.objectX = frame.getX();
        this.objectY = frame.getY();
        this.objectH = frame.getHeight();
        this.objectW = frame.getWidth();
        this.halfWidth = objectW / 2f;
        this.halfHeight = objectH / 2f;
        this.dataObject = itemAtPosition;
        this.parentWidth = ((ViewGroup) frame.getParent()).getWidth();
        this.parentHeight = ((ViewGroup) frame.getParent()).getHeight();
        this.BASE_ROTATION_DEGREES = rotation_degrees;
        this.mFlingListener = flingListener;
        this.RECT_TOP = new Rect((int) Math.max(frame.getLeft(), leftBorder()), 0, (int) Math.min(frame.getRight(), rightBorder()), (int) topBorder());
        this.RECT_BOTTOM = new Rect((int) Math.max(frame.getLeft(), leftBorder()), (int) bottomBorder(), (int) Math.min(frame.getRight(), rightBorder()), parentHeight);
        this.RECT_LEFT = new Rect(0, (int) Math.max(frame.getTop(), topBorder()), (int) leftBorder(), (int) Math.min(frame.getBottom(), bottomBorder()));
        this.RECT_RIGHT = new Rect((int) rightBorder(), (int) Math.max(frame.getTop(), topBorder()), parentWidth, (int) Math.min(frame.getBottom(), bottomBorder()));
    }


    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = event.getPointerId(0);
                float x = 0;
                float y = 0;
                boolean success = false;
                try {
                    x = event.getX(mActivePointerId);
                    y = event.getY(mActivePointerId);
                    success = true;
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Exception in onTouch(view, event) : " + mActivePointerId, e);
                }
                if (success) {
                    // Remember where we started
                    aDownTouchX = x;
                    aDownTouchY = y;
                    //to prevent an initial jump of the magnifier, aposX and aPosY must
                    //have the values from the magnifier frame
                    if (aPosX == 0) {
                        aPosX = frame.getX();
                    }
                    if (aPosY == 0) {
                        aPosY = frame.getY();
                    }

                    if (y < objectH / 2) {
                        touchPosition = TOUCH_ABOVE;
                    } else {
                        touchPosition = TOUCH_BELOW;
                    }
                }
                view.getParent().requestDisallowInterceptTouchEvent(true);
                break;

            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;
                resetCardViewOnStack();
                view.getParent().requestDisallowInterceptTouchEvent(false);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                break;

            case MotionEvent.ACTION_POINTER_UP:
                final int pointerIndex = (event.getAction() &
                        MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    aDownTouchX = event.getX(newPointerIndex);
                    aDownTouchY = event.getY(newPointerIndex);
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                final int pointerIndexMove = event.findPointerIndex(mActivePointerId);
                final float xMove = event.getX(pointerIndexMove);
                final float yMove = event.getY(pointerIndexMove);

                final float dx = xMove - aDownTouchX;
                final float dy = yMove - aDownTouchY;

                aPosX += dx;
                aPosY += dy;

                // calculate the rotation degrees
                float distobjectX = aPosX - objectX;
                float rotation = BASE_ROTATION_DEGREES * 2.f * distobjectX / parentWidth;
                if (touchPosition == TOUCH_BELOW) {
                    rotation = -rotation;
                }

                frame.setX(aPosX);
                frame.setY(aPosY);
                frame.setRotation(rotation);
                mFlingListener.onScroll(getScrollProgressPercent());
                break;

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                view.getParent().requestDisallowInterceptTouchEvent(false);
                break;
            }
        }
        return true;
    }

    private float getScrollProgressPercent() {
        if (movedBeyondLeftBorder()) {
            return -1f;
        } else if (movedBeyondRightBorder()) {
            return 1f;
        } else {
            float zeroToOneValue = (aPosX + halfWidth - leftBorder()) / (rightBorder() - leftBorder());
            return zeroToOneValue * 2f - 1f;
        }
    }

    private boolean resetCardViewOnStack() {
        if (movedBeyondLeftBorder() && parentView.DETECT_LEFT_SWIPE) {
            // Left Swipe
            onSelectedX(true, getExitPoint(-objectW), 100);
            mFlingListener.onScroll(-1.0f);
        } else if (movedBeyondRightBorder() && parentView.DETECT_RIGHT_SWIPE) {
            // Right Swipe
            onSelectedX(false, getExitPoint(parentWidth), 100);
            mFlingListener.onScroll(1.0f);
        } else if(movedBeyondTopBorder() && parentView.DETECT_TOP_SWIPE){
            onSelectedY(true, getExitPointX(-objectH), 100);
            mFlingListener.onScroll(-1.0f);
        } else if(movedBeyondBottomBorder() && parentView.DETECT_BOTTOM_SWIPE){
            onSelectedY(false, getExitPointX(parentHeight), 100);
            mFlingListener.onScroll(1.0f);
        }
        else {
            float abslMoveDistance = Math.abs(aPosX - objectX);
            aPosX = 0;
            aPosY = 0;
            aDownTouchX = 0;
            aDownTouchY = 0;
            frame.animate()
                    .setDuration(200)
                    .setInterpolator(new OvershootInterpolator(1.5f))
                    .x(objectX)
                    .y(objectY)
                    .rotation(0);
            mFlingListener.onScroll(0.0f);
            if (abslMoveDistance < 4.0) {
                mFlingListener.onClick(dataObject);
            }
        }
        return false;
    }


    private boolean movedBeyondLeftBorder() {
        int centerX = (int) (frame.getX() + halfWidth);
        int centerY = (int) (frame.getY() + halfHeight);
        return (RECT_LEFT.contains(centerX, centerY) || (centerX < RECT_LEFT.left && RECT_LEFT.contains(0, centerY)));
    }

    private boolean movedBeyondRightBorder() {
        int centerX = (int) (frame.getX() + halfWidth);
        int centerY = (int) (frame.getY() + halfHeight);
        return (RECT_RIGHT.contains(centerX, centerY) || (centerX > RECT_RIGHT.right && RECT_RIGHT.contains(RECT_RIGHT.left, centerY)));
    }

    private boolean movedBeyondBottomBorder() {
        int centerX = (int) (frame.getX() + halfWidth);
        int centerY = (int) (frame.getY() + halfHeight);
        return (RECT_BOTTOM.contains(centerX, centerY) || (centerY > RECT_BOTTOM.bottom && RECT_BOTTOM.contains(centerX, RECT_BOTTOM.top)));
    }

    private boolean movedBeyondTopBorder() {
        int centerX = (int) (frame.getX() + halfWidth);
        int centerY = (int) (frame.getY() + halfHeight);
        return (RECT_TOP.contains(centerX, centerY) || (centerY < RECT_TOP.top && RECT_TOP.contains(centerX, 0)));
    }

    private float leftBorder() {
        return parentWidth / 4.f;
    }

    private float rightBorder() {
        return 3 * parentWidth / 4.f;
    }

    private float bottomBorder() {
        return 3 * parentHeight / 4.f;
    }

    private float topBorder() {
        return parentHeight  / 4.f;
    }

    private void onSelectedY(final boolean isTop, float exitX, int duration) {
        isAnimationRunning = true;
        float exitY;
        if (isTop) {
            exitY = -objectH - getRotationWidthOffset();
        } else {
            exitY = parentHeight + getRotationWidthOffset();
        }

        this.frame.animate()
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
                .x(exitX)
                .y(exitY)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (isTop) {
                            mFlingListener.onCardExited();
                            mFlingListener.topExit(dataObject);
                        } else {
                            mFlingListener.onCardExited();
                            mFlingListener.bottomExit(dataObject);
                        }
                        isAnimationRunning = false;
                    }
                })
                .rotation(getVerticalExitRotation(isTop));
    }

    private void onSelectedX(final boolean isLeft,
                             float exitY, long duration) {
        isAnimationRunning = true;
        float exitX;
        if (isLeft) {
            exitX = -objectW - getRotationWidthOffset();
        } else {
            exitX = parentWidth + getRotationWidthOffset();
        }

        this.frame.animate()
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
                .x(exitX)
                .y(exitY)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (isLeft) {
                            mFlingListener.onCardExited();
                            mFlingListener.leftExit(dataObject);
                        } else {
                            mFlingListener.onCardExited();
                            mFlingListener.rightExit(dataObject);
                        }
                        isAnimationRunning = false;
                    }
                })
                .rotation(getHorizontalExitRotation(isLeft));
    }

    void selectLeft() {
        if (!isAnimationRunning)
            onSelectedX(true, objectY, 200);
    }

    void selectRight() {
        if (!isAnimationRunning)
            onSelectedX(false, objectY, 200);
    }

    void selectTop() {
        if (!isAnimationRunning)
            onSelectedY(true, objectX, 200);
    }

    void selectBottom() {
        if (!isAnimationRunning)
            onSelectedY(false, objectX, 200);
    }


    private float getExitPoint(int exitXPoint) {
        float[] x = new float[2];
        x[0] = objectX;
        x[1] = aPosX;

        float[] y = new float[2];
        y[0] = objectY;
        y[1] = aPosY;

        LinearRegression regression = new LinearRegression(x, y);

        //Your typical y = ax+b linear regression
        return (float) regression.slope() * exitXPoint + (float) regression.intercept();
    }

    private float getExitPointX(int exitYPoint) {
        float[] x = new float[2];
        x[0] = objectX;
        x[1] = aPosX;

        float[] y = new float[2];
        y[0] = objectY;
        y[1] = aPosY;

        LinearRegression regression = new LinearRegression(x, y);

        //Your typical x = (y - b) / a linear regression
        return (float) ((exitYPoint - (float) regression.intercept()) / regression.slope());
    }

    private float getHorizontalExitRotation(boolean isLeft) {
        float rotation = BASE_ROTATION_DEGREES * 2.f * (parentWidth - objectX) / parentWidth;
        if (touchPosition == TOUCH_BELOW) {
            rotation = -rotation;
        }
        if (isLeft) {
            rotation = -rotation;
        }
        return rotation;
    }

    private float getVerticalExitRotation(boolean isTop) {
        float rotation = BASE_ROTATION_DEGREES * 2.f * (parentHeight - objectY) / parentHeight;
        if (touchPosition == TOUCH_BELOW) {
            rotation = -rotation;
        }
        if (isTop) {
            rotation = -rotation;
        }
        return rotation;
    }

    /**
     * When the object rotates it's width becomes bigger.
     * The maximum width is at 45 degrees.
     * <p/>
     * The below method calculates the width offset of the rotation.
     */
    private float getRotationWidthOffset() {
        return objectW / MAX_COS - objectW;
    }


    public void setRotationDegrees(float degrees) {
        this.BASE_ROTATION_DEGREES = degrees;
    }

    boolean isTouching() {
        return this.mActivePointerId != INVALID_POINTER_ID;
    }

    PointF getLastPoint() {
        return new PointF(this.aPosX, this.aPosY);
    }

    interface FlingListener {
        void onCardExited();

        void leftExit(Object dataObject);

        void rightExit(Object dataObject);

        void onClick(Object dataObject);

        void onScroll(float scrollProgressPercent);

        void topExit(Object dataObject);

        void bottomExit(Object dataObject);
    }

}





