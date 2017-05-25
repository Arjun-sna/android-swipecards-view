package in.arjsna.swipecardlib;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by dvelasquez on 5/2/17.
 */

public class SwipeFrameView extends FrameLayout implements SwipeableView {

    private SwipeCardView.OnCardFlingListener mFlingListener;
    private FlingCardListener flingCardListener;
    private Object object;

    @Override
    public boolean detectBottomSwipe() {
        return true;
    }

    @Override
    public boolean detectLeftSwipe() {
        return true;
    }

    @Override
    public boolean detectRightSwipe() {
        return true;
    }

    @Override
    public boolean detectTopSwipe() {
        return true;
    }

    public void setFlingListener(Object object, SwipeCardView.OnCardFlingListener mFlingListener) {
        this.mFlingListener = mFlingListener;
        this.object = object;
    }

    public SwipeFrameView(Context context) {
        this(context, null);
    }

    public SwipeFrameView(Context context,AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setupView();
    }


    private void setupView() {
        flingCardListener = new FlingCardListener(this, this, object,
                15.0f, new FlingCardListener.FlingListener() {
            @Override
            public void onCardExited() {
                requestLayout();
            }

            @Override
            public void leftExit(Object dataObject) {
                if(mFlingListener != null)
                    mFlingListener.onCardExitLeft(dataObject);
            }

            @Override
            public void rightExit(Object dataObject) {
                if(mFlingListener != null)
                    mFlingListener.onCardExitRight(dataObject);
            }

            @Override
            public void onClick(Object dataObject) {

            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }

            @Override
            public void topExit(Object dataObject) {
                if(mFlingListener != null)
                    mFlingListener.onCardExitTop(dataObject);
            }

            @Override
            public void bottomExit(Object dataObject) {
                if(mFlingListener != null)
                    mFlingListener.onCardExitBottom(dataObject);
            }
        });

        this.setOnTouchListener(flingCardListener);
    }
}
