package in.arjsna.swipecardlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Adapter;
import android.widget.FrameLayout;

/**
 * Created by arjun on 4/26/16.
 */
public class SwipeFlingPageView extends BaseFlingAdapterView {
    private int MIN_ADAPTER_STACK = 6;
    private int MAX_VISIBLE = 2;
    private Adapter mAdapter;
    private boolean mInLayout = false;
    private int LAST_OBJECT_IN_STACK = 0;
    private View mActiveCard;
    private FlingPageListener flingPageListener;
    private PointF mLastTouchPoint;
    private float CURRENT_TRANSY_VAL;
    private float CURRENT_SCALE_VAL;
    private double SCALE_OFFSET = 0.2;
    private int TRANS_OFFSET = 45;
    private OnPageFlingListener mFlingListener;
    private OnItemClickListener mOnItemClickListener;
    private AdapterDataSetObserver mDataSetObserver;
    private int START_STACK_FROM = 0;

    public SwipeFlingPageView(Context context) {
        this(context, null);
    }

    public SwipeFlingPageView(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public SwipeFlingPageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeFlingPageView, defStyle, 0);
//        MAX_VISIBLE = a.getInt(R.styleable.SwipeFlingCardView_max_visible, MAX_VISIBLE);
        MIN_ADAPTER_STACK = a.getInt(R.styleable.SwipeFlingPageView_min_adapter_stack_page, MIN_ADAPTER_STACK);
        a.recycle();
    }

    @Override
    public void requestLayout() {
        if (!mInLayout) {
            super.requestLayout();
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        // if we don't have an adapter, we don't need to do anything
        if (mAdapter == null) {
            return;
        }

        mInLayout = true;
        final int adapterCount = mAdapter.getCount();

        if(adapterCount == 0) {
            removeAllViewsInLayout();
        }else {
            View topCard = getChildAt(LAST_OBJECT_IN_STACK);
            if(mActiveCard!=null && topCard!=null && topCard==mActiveCard) {
                if (this.flingPageListener.isTouching()) {
                    PointF lastPoint = this.flingPageListener.getLastPoint();
                    if (this.mLastTouchPoint == null || !this.mLastTouchPoint.equals(lastPoint)) {
                        this.mLastTouchPoint = lastPoint;
                        removeViewsInLayout(0, LAST_OBJECT_IN_STACK);
                        layoutChildren(1, adapterCount);
                    }
                }
            }else{
                // Reset the UI and set top view listener
                removeAllViewsInLayout();
                layoutChildren(START_STACK_FROM, adapterCount);
                setTopView();
            }
        }

        mInLayout = false;

        if(adapterCount <= MIN_ADAPTER_STACK) mFlingListener.onAdapterAboutToEmpty(adapterCount);
    }

    private void layoutChildren(int startingIndex, int adapterCount){
        resetOffsets();
        if(adapterCount < MAX_VISIBLE){
            MAX_VISIBLE = adapterCount;
        }
        int viewStack = 0;
        while (startingIndex < START_STACK_FROM + MAX_VISIBLE && startingIndex < adapterCount) {
            View newUnderChild = mAdapter.getView(startingIndex, null, this);
            if (newUnderChild.getVisibility() != GONE) {
                makeAndAddView(newUnderChild);
                LAST_OBJECT_IN_STACK = viewStack;
            }
            startingIndex++;viewStack++;
        }
    }

    private void resetOffsets() {
        CURRENT_TRANSY_VAL = 0;
        CURRENT_SCALE_VAL = 0;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void makeAndAddView(View child) {

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
        child.setScaleX(child.getScaleX() - CURRENT_SCALE_VAL);
        child.setScaleY(child.getScaleY() - CURRENT_SCALE_VAL);
//        child.setY(child.getTranslationY() + CURRENT_TRANSY_VAL);
        CURRENT_SCALE_VAL += SCALE_OFFSET;
//        CURRENT_TRANSY_VAL += TRANS_OFFSET;
        addViewInLayout(child, 0, lp, true);

        final boolean needToMeasure = child.isLayoutRequested();
        if (needToMeasure) {
            int childWidthSpec = getChildMeasureSpec(getWidthMeasureSpec(),
                    getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin,
                    lp.width);
            int childHeightSpec = getChildMeasureSpec(getHeightMeasureSpec(),
                    getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin,
                    lp.height);
            child.measure(childWidthSpec, childHeightSpec);
        } else {
            cleanupLayoutState(child);
        }


        int w = child.getMeasuredWidth();
        int h = child.getMeasuredHeight();

        int gravity = lp.gravity;
        if (gravity == -1) {
            gravity = Gravity.TOP | Gravity.START;
        }


        int layoutDirection = getLayoutDirection();
        final int absoluteGravity = Gravity.getAbsoluteGravity(gravity, layoutDirection);
        final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

        int childLeft;
        int childTop;
        switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
            case Gravity.CENTER_HORIZONTAL:
                childLeft = (getWidth() + getPaddingLeft() - getPaddingRight()  - w) / 2 +
                        lp.leftMargin - lp.rightMargin;
                break;
            case Gravity.END:
                childLeft = getWidth() + getPaddingRight() - w - lp.rightMargin;
                break;
            case Gravity.START:
            default:
                childLeft = getPaddingLeft() + lp.leftMargin;
                break;
        }
        switch (verticalGravity) {
            case Gravity.CENTER_VERTICAL:
                childTop = (getHeight() + getPaddingTop() - getPaddingBottom()  - h) / 2 +
                        lp.topMargin - lp.bottomMargin;
                break;
            case Gravity.BOTTOM:
                childTop = getHeight() - getPaddingBottom() - h - lp.bottomMargin;
                break;
            case Gravity.TOP:
            default:
                childTop = getPaddingTop() + lp.topMargin;
                break;
        }

        child.layout(childLeft, childTop, childLeft + w, childTop + h);
    }

    private void setTopView() {
        if(getChildCount()>0){

            mActiveCard = getChildAt(LAST_OBJECT_IN_STACK);
            if(mActiveCard!=null) {

                flingPageListener = new FlingPageListener(mActiveCard, mAdapter.getItem(0),
                        new FlingPageListener.FlingListener() {

                            @Override
                            public void onCardExited() {
                                mActiveCard = null;
                                START_STACK_FROM++;
                                requestLayout();
//                                mFlingListener.removeFirstObjectInAdapter();
                            }

                            @Override
                            public void leftExit(Object dataObject) {
                                mFlingListener.onLeftCardExit(dataObject);
                            }

                            @Override
                            public void rightExit(Object dataObject) {
                                mFlingListener.onRightCardExit(dataObject);
                            }

                            @Override
                            public void onClick(Object dataObject) {
                                if(mOnItemClickListener!=null)
                                    mOnItemClickListener.onItemClicked(0, dataObject);

                            }

                            @Override
                            public void onScroll(float scrollProgressPercent) {
                                Log.i("Scroll Percentage ", scrollProgressPercent + "");
                                mFlingListener.onScroll(scrollProgressPercent);
                                int childCount = getChildCount() - 1;
                                if(childCount < MAX_VISIBLE){
                                    while (childCount > 0) {
                                        relayoutChild(getChildAt(childCount - 1), Math.abs(scrollProgressPercent), childCount);
                                        childCount--;
                                    }
                                } else {
                                    while (childCount > 1) {
                                        relayoutChild(getChildAt(childCount - 1), Math.abs(scrollProgressPercent), childCount - 1);
                                        childCount--;
                                    }
                                }
                            }

                            @Override
                            public void topExit(Object dataObject) {
                                mFlingListener.onTopCardExit(dataObject);
                            }

                            @Override
                            public void bottomExit(Object dataObject) {
                                mFlingListener.onBottomCardExit(dataObject);
                            }
                        });

                mActiveCard.setOnTouchListener(flingPageListener);
            }
        }
    }

    public void relayoutChild(View child, float scrollDis, int childcount){
        float absScrollDis = scrollDis > 1 ? 1 : scrollDis;
        float newScale = (float) (1 - SCALE_OFFSET * (MAX_VISIBLE - childcount) + absScrollDis * SCALE_OFFSET);
        child.setScaleX(newScale);
        child.setScaleY(newScale);
//        child.setTranslationY(TRANS_OFFSET * (MAX_VISIBLE - childcount) - absScrollDis * TRANS_OFFSET);
    }

    public interface OnItemClickListener {
        void onItemClicked(int itemPosition, Object dataObject);
    }

    @Override
    public Adapter getAdapter() {
        return null;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
            mDataSetObserver = null;
        }

        mAdapter = adapter;

        if (mAdapter != null  && mDataSetObserver == null) {
            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
    }

    @Override
    public View getSelectedView() {
        return mActiveCard;
    }

    public void setFlingListener(OnPageFlingListener OnCardFlingListener) {
        this.mFlingListener = OnCardFlingListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnPageFlingListener {
//        void removeFirstObjectInAdapter();
        void onLeftCardExit(Object dataObject);
        void onRightCardExit(Object dataObject);
        void onAdapterAboutToEmpty(int itemsInAdapter);
        void onScroll(float scrollProgressPercent);

        void onTopCardExit(Object dataObject);
        void onBottomCardExit(Object dataObject);
    }

    private class AdapterDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            requestLayout();
        }

    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new FrameLayout.LayoutParams(getContext(), attrs);
    }
}
