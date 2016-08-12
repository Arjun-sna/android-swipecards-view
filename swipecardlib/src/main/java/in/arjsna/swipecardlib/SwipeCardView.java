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

public class SwipeCardView extends BaseFlingAdapterView {


    private static final double SCALE_OFFSET = 0.04;
    private static final float TRANS_OFFSET = 45;
    protected boolean DETECT_BOTTOM_SWIPE;
    protected boolean DETECT_TOP_SWIPE;
    protected boolean DETECT_RIGHT_SWIPE;
    protected boolean DETECT_LEFT_SWIPE;
    private float CURRENT_TRANSY_VAL = 0;
    private float CURRENT_SCALE_VAL = 0;
    private int MAX_VISIBLE = 3;
    private int MIN_ADAPTER_STACK = 6;
    private float ROTATION_DEGREES = 15.f;

    private Adapter mAdapter;
    private int LAST_OBJECT_IN_STACK = 0;
    private OnCardFlingListener mFlingListener;
    private AdapterDataSetObserver mDataSetObserver;
    private boolean mInLayout = false;
    private View mActiveCard = null;
    private OnItemClickListener mOnItemClickListener;
    private FlingCardListener flingCardListener;
    private PointF mLastTouchPoint;
    private int START_STACK_FROM = 0;


    public SwipeCardView(Context context) {
        this(context, null);
    }

    public SwipeCardView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.SwipeFlingStyle);
    }

    public SwipeCardView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeCardView, defStyle, 0);
        MAX_VISIBLE = a.getInt(R.styleable.SwipeCardView_max_visible, MAX_VISIBLE);
        MIN_ADAPTER_STACK = a.getInt(R.styleable.SwipeCardView_min_adapter_stack, MIN_ADAPTER_STACK);
        ROTATION_DEGREES = a.getFloat(R.styleable.SwipeCardView_rotation_degrees, ROTATION_DEGREES);
        DETECT_LEFT_SWIPE = a.getBoolean(R.styleable.SwipeCardView_left_swipe_detect, true);
        DETECT_RIGHT_SWIPE = a.getBoolean(R.styleable.SwipeCardView_right_swipe_detect, true);
        DETECT_BOTTOM_SWIPE = a.getBoolean(R.styleable.SwipeCardView_bottom_swipe_detect, true);
        DETECT_TOP_SWIPE = a.getBoolean(R.styleable.SwipeCardView_top_swipe_detect, true);
        a.recycle();
    }


    /**
     * A shortcut method to set both the listeners and the adapter.
     *
     * @param context The activity context which extends OnCardFlingListener, OnItemClickListener or both
     * @param mAdapter The adapter you have to set.
     */
    public void init(final Context context, Adapter mAdapter) {
        if(context instanceof OnCardFlingListener) {
            mFlingListener = (OnCardFlingListener) context;
        }else{
            throw new RuntimeException("Activity does not implement SwipeFlingAdapterView.OnCardFlingListener");
        }
        if(context instanceof OnItemClickListener){
            mOnItemClickListener = (OnItemClickListener) context;
        }
        setAdapter(mAdapter);
    }

 	@Override
    public View getSelectedView() {
        return mActiveCard;
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//        canvas.drawRect((int) Math.max(mActiveCard.getLeft(), leftBorder()), 0, (int) Math.min(mActiveCard.getRight(), rightBorder()), (int) topBorder(), new Paint());
//        canvas.drawRect((int) Math.max(mActiveCard.getLeft(), leftBorder()), (int) bottomBorder(), (int) Math.min(mActiveCard.getRight(), rightBorder()), getHeight(), new Paint());
//        canvas.drawRect(0, (int) Math.max(mActiveCard.getTop(), topBorder()), (int) leftBorder(), (int) Math.min(mActiveCard.getBottom(), bottomBorder()), new Paint());
//        canvas.drawRect((int) rightBorder(), (int) Math.max(mActiveCard.getTop(), topBorder()), getWidth(), (int) Math.min(mActiveCard.getBottom(), bottomBorder()), new Paint());
//    }


    public float leftBorder() {
        return getWidth() / 4.f;
    }

    public float rightBorder() {
        return 3 * getWidth() / 4.f;
    }

    public float bottomBorder() {
        return 3 * getHeight() / 4.f;
    }

    public float topBorder() {
        return getHeight() / 4.f;
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
                if (this.flingCardListener.isTouching()) {
                    PointF lastPoint = this.flingCardListener.getLastPoint();
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
        if(adapterCount - startingIndex < MAX_VISIBLE){
            MAX_VISIBLE = adapterCount - startingIndex;
        }
        int viewStack = 0;
        while (startingIndex < START_STACK_FROM + MAX_VISIBLE && startingIndex < adapterCount) {
            View newUnderChild = mAdapter.getView(startingIndex, null, this);
            if (newUnderChild.getVisibility() != GONE) {
                makeAndAddView(newUnderChild, false);
            }
            startingIndex++;viewStack++;
        }

        /**
         * This is to add a base view at end. To make an illusion that views come out from
         * a base card. The scale and translation of this view is same as the one previous to
         * this.
         */
        if(startingIndex >= adapterCount){
            LAST_OBJECT_IN_STACK = --viewStack;
            return;
        }
        View newUnderChild = mAdapter.getView(startingIndex, null, this);
        if (newUnderChild != null && newUnderChild.getVisibility() != GONE) {
            makeAndAddView(newUnderChild, true);
            LAST_OBJECT_IN_STACK = viewStack;
        }
    }

    private void resetOffsets() {
        CURRENT_TRANSY_VAL = 0;
        CURRENT_SCALE_VAL = 0;
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void makeAndAddView(View child, boolean isBase) {

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
        if(isBase){
            child.setScaleX((float) (child.getScaleX() - (CURRENT_SCALE_VAL - SCALE_OFFSET)));
            child.setScaleY((float) (child.getScaleY() - (CURRENT_SCALE_VAL - SCALE_OFFSET)));
            child.setY(child.getTranslationY() + CURRENT_TRANSY_VAL - TRANS_OFFSET);
        } else {
            child.setScaleX(child.getScaleX() - CURRENT_SCALE_VAL);
            child.setScaleY(child.getScaleY() - CURRENT_SCALE_VAL);
            child.setY(child.getTranslationY() + CURRENT_TRANSY_VAL);
        }
        CURRENT_SCALE_VAL += SCALE_OFFSET;
        CURRENT_TRANSY_VAL += TRANS_OFFSET;
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

    public void relayoutChild(View child, float scrollDis, int childcount){
        float absScrollDis = scrollDis > 1 ? 1 : scrollDis;
        float newScale = (float) (1 - SCALE_OFFSET * (MAX_VISIBLE - childcount) + absScrollDis * SCALE_OFFSET);
        child.setScaleX(newScale);
        child.setScaleY(newScale);
        child.setTranslationY(TRANS_OFFSET * (MAX_VISIBLE - childcount) - absScrollDis * TRANS_OFFSET);
    }



    /**
    *  Set the top view and add the fling listener
    */
    private void setTopView() {
        if(getChildCount()>0){

            mActiveCard = getChildAt(LAST_OBJECT_IN_STACK);
            if(mActiveCard!=null) {

                flingCardListener = new FlingCardListener(this, mActiveCard, mAdapter.getItem(START_STACK_FROM),
                        ROTATION_DEGREES, new FlingCardListener.FlingListener() {

                            @Override
                            public void onCardExited() {
                                mActiveCard = null;
                                START_STACK_FROM++;
                                requestLayout();
//                                mFlingListener.removeFirstObjectInAdapter();
                            }

                            @Override
                            public void leftExit(Object dataObject) {
                                mFlingListener.onCardExitLeft(dataObject);
                            }

                            @Override
                            public void rightExit(Object dataObject) {
                                mFlingListener.onCardExitRight(dataObject);
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
                        mFlingListener.onCardExitTop(dataObject);
                    }

                    @Override
                    public void bottomExit(Object dataObject) {
                        mFlingListener.onCardExitBottom(dataObject);
                    }
                });

                mActiveCard.setOnTouchListener(flingCardListener);
            }
        }
    }

    public FlingCardListener getTopCardListener() throws NullPointerException{
        if(flingCardListener==null){
            throw new NullPointerException();
        }
        return flingCardListener;
    }

    public void setMaxVisible(int MAX_VISIBLE){
        this.MAX_VISIBLE = MAX_VISIBLE;
    }

    public void setMinStackInAdapter(int MIN_ADAPTER_STACK){
        this.MIN_ADAPTER_STACK = MIN_ADAPTER_STACK;
    }

    @Override
    public Adapter getAdapter() {
        return mAdapter;
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

    public void setFlingListener(OnCardFlingListener OnCardFlingListener) {
        this.mFlingListener = OnCardFlingListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }




    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new FrameLayout.LayoutParams(getContext(), attrs);
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


    public interface OnItemClickListener {
        void onItemClicked(int itemPosition, Object dataObject);
    }

    public interface OnCardFlingListener {
        void onCardExitLeft(Object dataObject);
        void onCardExitRight(Object dataObject);
        void onAdapterAboutToEmpty(int itemsInAdapter);
        void onScroll(float scrollProgressPercent);

        void onCardExitTop(Object dataObject);
        void onCardExitBottom(Object dataObject);
    }

    public void throwLeft() {
        flingCardListener.selectLeft();
    }

    public void throwRight() {
        flingCardListener.selectRight();
    }

    public void throwTop() {
        flingCardListener.selectTop();
    }

    public void throwBottom() {
        flingCardListener.selectBottom();
    }

}
