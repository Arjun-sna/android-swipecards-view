
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

/**
 * Created by arjun on 4/25/16.
 */
public class FlingPageListener implements View.OnTouchListener{
    private static final String TAG = "FlingPageListener";
    private static final int INVALID_POINTER_ID = -1;
    private final Rect RECT_BOTTOM;
    private final Rect RECT_TOP;
    private final Rect RECT_RIGHT;
    private final Rect RECT_LEFT;
    private final View frame;
    private final float objectX;
    private final float objectY;
    private final int objectH;
    private final int objectW;
    private final float halfWidth;
    private final float halfHeight;
    private final Object dataObject;
    private final int parentWidth;
    private final int parentHeight;
    private final FlingListener mFlingListener;
    private int mActivePointerId = INVALID_POINTER_ID;
    private float aDownTouchX;
    private float aDownTouchY;
    private float aPosX;
    private float aPosY;
    private boolean isAnimationRunning = false;

//    public FlingPageListener(View frame, Object itemAtPosition, FlingListener flingListener) {
//        this(frame, itemAtPosition, 15f, flingListener);
//    }

    public FlingPageListener(View frame, Object itemAtPosition, FlingListener flingListener) {
        super();
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
        this.mFlingListener = flingListener;
        this.RECT_TOP = new Rect((int) Math.max(frame.getLeft(), leftBorder()), 0, (int) Math.min(frame.getRight(), rightBorder()), (int) topBorder());
        this.RECT_BOTTOM = new Rect((int) Math.max(frame.getLeft(), leftBorder()), (int) bottomBorder(), (int) Math.min(frame.getRight(), rightBorder()), parentHeight);
        this.RECT_LEFT = new Rect(0, (int) Math.max(frame.getTop(), topBorder()), (int) leftBorder(), (int) Math.min(frame.getBottom(), bottomBorder()));
        this.RECT_RIGHT = new Rect((int) rightBorder(), (int) Math.max(frame.getTop(), topBorder()), parentWidth, (int) Math.min(frame.getBottom(), bottomBorder()));
    }

    public float leftBorder() {
        return parentWidth / 4.f;
    }

    public float rightBorder() {
        return 3 * parentWidth / 4.f;
    }

    public float bottomBorder() {
        return 3 * parentHeight / 4.f;
    }

    public float topBorder() {
        return parentHeight  / 4.f;
    }

    public boolean onTouch(View view, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                // from http://android-developers.blogspot.com/2010/06/making-sense-of-multitouch.html
                // Save the ID of this pointer

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

//                    if (y < objectH / 2) {
//                        touchPosition = TOUCH_ABOVE;
//                    } else {
//                        touchPosition = TOUCH_BELOW;
//                    }
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
                // Extract the index of the pointer that left the touch sensor
                final int pointerIndex = (event.getAction() &
                        MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            case MotionEvent.ACTION_MOVE:

                // Find the index of the active pointer and fetch its position
                final int pointerIndexMove = event.findPointerIndex(mActivePointerId);
                final float xMove = event.getX(pointerIndexMove);
                final float yMove = event.getY(pointerIndexMove);

                //from http://android-developers.blogspot.com/2010/06/making-sense-of-multitouch.html
                // Calculate the distance moved
                final float dx = xMove - aDownTouchX;
                final float dy = yMove - aDownTouchY;


                // Move the frame
                aPosX += dx;
                aPosY += dy;

                //in this area would be code for doing something with the view as the frame moves.
//                frame.setX(aPosX);
                frame.setY(aPosY);
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
        if (movedBeyondTopBorder()) {
            return -1f;
        } else if (movedBeyondBottomBorder()) {
            return 1f;
        } else {
            float zeroToOneValue = (aPosY + halfHeight - topBorder()) / (bottomBorder() - topBorder());
            return zeroToOneValue * 2f - 1f;
        }
    }

    private boolean resetCardViewOnStack() {
        if(movedBeyondTopBorder()){
            Log.i("Swipe ", "top");
            onSelectedY(true, 100);
            mFlingListener.onScroll(-1.0f);
        } else if(movedBeyondBottomBorder()){
            Log.i("Swipe ", "bottom");
            onSelectedY(false, 100);
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

    private boolean movedBeyondBottomBorder() {
        int centerX = (int) (frame.getX() + halfWidth);
        int centerY = (int) (frame.getY() + halfHeight);
        return (RECT_BOTTOM.contains(centerX, centerY) || (centerY > RECT_BOTTOM.bottom && RECT_BOTTOM.contains(centerX, RECT_BOTTOM.top)));
//        return aPosY + halfHeight > bottomBorder();
    }

    private boolean movedBeyondTopBorder() {
        int centerX = (int) (frame.getX() + halfWidth);
        int centerY = (int) (frame.getY() + halfHeight);
        return (RECT_TOP.contains(centerX, centerY) || (centerY < RECT_TOP.top && RECT_TOP.contains(centerX, 0)));
//        return aPosY + halfHeight < topBorder();
    }

    private void onSelectedY(final boolean isTop, int duration) {
        isAnimationRunning = true;
        float exitY;
        if (isTop) {
            exitY = -objectH;
        } else {
            exitY = parentHeight;
        }

        this.frame.animate()
                .setDuration(duration)
                .setInterpolator(new AccelerateInterpolator())
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
                });
    }

    private float getExitPointX(int exitYPoint) {
        float[] x = new float[2];
        x[0] = objectX;
        x[1] = aPosX;

        float[] y = new float[2];
        y[0] = objectY;
        y[1] = aPosY;

        LinearRegression regression = new LinearRegression(y, x);

        //Your typical y = ax+b linear regression
        return (float) regression.slope() * exitYPoint + (float) regression.intercept();
    }

    public boolean isTouching() {
        return this.mActivePointerId != INVALID_POINTER_ID;
    }

    public PointF getLastPoint() {
        return new PointF(this.aPosX, this.aPosY);
    }


//    private float getRotationWidthOffset() {
//        return objectW / MAX_COS - objectW;
//    }

    protected interface FlingListener {
        void onCardExited();

        void leftExit(Object dataObject);

        void rightExit(Object dataObject);

        void onClick(Object dataObject);

        void onScroll(float scrollProgressPercent);

        void topExit(Object dataObject);

        void bottomExit(Object dataObject);
    }
}
