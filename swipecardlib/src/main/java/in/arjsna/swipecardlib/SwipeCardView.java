package in.arjsna.swipecardlib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.Adapter;
import android.widget.FrameLayout;

public class SwipeCardView extends BaseFlingAdapterView implements SwipeableView {

  private static final double SCALE_OFFSET = 0.04;

  private static final float TRANS_OFFSET = 45;

  protected boolean mDetectBottomSwipe;

  protected boolean mDetectTopSwipe;

  protected boolean mDetectRightSwipe;

  protected boolean mDetectLeftSwipe;

  private int mInitialMaxVisible = 3;

  private int mMaxVisible = 3;

  private int mMinAdapterStack = 6;

  private float mRotationDegrees = 15.f;

  private int mCurrentAdapterCount = 0;

  private Adapter mAdapter;

  private int mLastObjectInStack = 0;

  private OnCardFlingListener mFlingListener;

  private AdapterDataSetObserver mDataSetObserver;

  private boolean mInLayout = false;

  private View mActiveCard = null;

  private OnItemClickListener mOnItemClickListener;

  private FlingCardListener flingCardListener;

  private int mTopOfStack = 0;

  private int adapterCount = 0;

  public SwipeCardView(Context context) {
    this(context, null);
  }

  public SwipeCardView(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.SwipeFlingStyle);
  }

  public SwipeCardView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeCardView, defStyle, 0);
    mMaxVisible = a.getInt(R.styleable.SwipeCardView_max_visible, mMaxVisible);
    mMinAdapterStack = a.getInt(R.styleable.SwipeCardView_min_adapter_stack, mMinAdapterStack);
    mRotationDegrees = a.getFloat(R.styleable.SwipeCardView_rotation_degrees, mRotationDegrees);
    mDetectLeftSwipe = a.getBoolean(R.styleable.SwipeCardView_left_swipe_detect, true);
    mDetectRightSwipe = a.getBoolean(R.styleable.SwipeCardView_right_swipe_detect, true);
    mDetectBottomSwipe = a.getBoolean(R.styleable.SwipeCardView_bottom_swipe_detect, true);
    mDetectTopSwipe = a.getBoolean(R.styleable.SwipeCardView_top_swipe_detect, true);
    mInitialMaxVisible = mMaxVisible;
    a.recycle();
  }

  /**
   * A shortcut method to set both the listeners and the adapter.
   *
   * @param context The activity context which extends OnCardFlingListener, OnItemClickListener or
   * both
   * @param mAdapter The adapter you have to set.
   */
  public void init(final Context context, Adapter mAdapter) {
    if (context instanceof OnCardFlingListener) {
      mFlingListener = (OnCardFlingListener) context;
    } else {
      throw new RuntimeException(
          "Activity does not implement SwipeFlingAdapterView.OnCardFlingListener");
    }
    if (context instanceof OnItemClickListener) {
      mOnItemClickListener = (OnItemClickListener) context;
    }
    setAdapter(mAdapter);
  }

  @Override public View getSelectedView() {
    return mActiveCard;
  }

  public int getCurrentPosition() {
    return mTopOfStack;
  }

  public Object getCurrentItem() {
    return mAdapter.getItem(mTopOfStack);
  }

  @Override public void requestLayout() {
    if (!mInLayout) {
      super.requestLayout();
    }
  }

  @Override protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    // if we don't have an adapter, we don't need to do anything
    if (mAdapter == null) {
      return;
    }

    mInLayout = true;

    if (adapterCount == 0) {
      removeAllViewsInLayout();
    } else {
      // Reset the UI and set top view listener
      removeAllViewsInLayout();
      layoutChildren(0, adapterCount);
      setTopView();
    }
    mInLayout = false;

    checkForAdapterCount();
  }

  private void checkForAdapterCount() {
    if (mCurrentAdapterCount <= mMinAdapterStack) {
      mFlingListener.onAdapterAboutToEmpty(mCurrentAdapterCount);
    }
  }

  private void layoutChildren(int startingIndex, int adapterCount) {
    int maxVisible = mMaxVisible;
    if (adapterCount < maxVisible) {
      maxVisible = adapterCount;
    }
    int viewStack = startingIndex;
    while (startingIndex < maxVisible && startingIndex < adapterCount) {
      View newUnderChild = mAdapter.getView(startingIndex, null, this);
      if (newUnderChild.getVisibility() != GONE) {
        makeAndAddView(newUnderChild, false);
      }
      startingIndex++;
      viewStack++;
    }

    /**
     * This is to add a base view at end. To make an illusion that views come out from
     * a base card. The scale and translation of this view is same as the one previous to
     * this.
     */
    if (startingIndex >= adapterCount) {
      mLastObjectInStack = --viewStack;
      return;
    }
    View newUnderChild = mAdapter.getView(startingIndex, null, this);
    if (newUnderChild != null && newUnderChild.getVisibility() != GONE) {
      makeAndAddView(newUnderChild, true);
      mLastObjectInStack = viewStack;
    }
  }

  public void layoutMissingChildren() {
    while(getChildCount() < mMaxVisible && (mLastObjectInStack + 1) < adapterCount) {
      View newUnderChild = mAdapter.getView(++mLastObjectInStack, null, this);
      if (newUnderChild.getVisibility() != GONE) {
        makeAndAddView(newUnderChild, false);
      }
    }
    if (mLastObjectInStack + 1 < adapterCount) {
      View newUnderChild = mAdapter.getView(++mLastObjectInStack, null, this);
      if (newUnderChild.getVisibility() != GONE) {
        makeAndAddView(newUnderChild, true);
      }
    }
  }

  private float getScaleValue() {
    int currentChildCount = getChildCount();
    return (float) (SCALE_OFFSET * currentChildCount);
  }

  private float getTransValue() {
    int currentChildCount = getChildCount();
    return (float) (TRANS_OFFSET * currentChildCount);
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
  private void makeAndAddView(View child, boolean isBase) {
    FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) child.getLayoutParams();
    if (isBase) {
      child.setScaleX((float) (child.getScaleX() - (getScaleValue() - SCALE_OFFSET)));
      child.setScaleY((float) (child.getScaleY() - (getScaleValue() - SCALE_OFFSET)));
      child.setY(child.getTranslationY() + getTransValue() - TRANS_OFFSET);
    } else {
      child.setScaleX(child.getScaleX() - getScaleValue());
      child.setScaleY(child.getScaleY() - getScaleValue());
      child.setY(child.getTranslationY() + getTransValue());
    }

    addViewInLayout(child, 0, lp, true);

    final boolean needToMeasure = child.isLayoutRequested();
    if (needToMeasure) {
      int childWidthSpec = getChildMeasureSpec(getWidthMeasureSpec(),
          getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
      int childHeightSpec = getChildMeasureSpec(getHeightMeasureSpec(),
          getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin, lp.height);
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
        childLeft = (getWidth() + getPaddingLeft() - getPaddingRight() - w) / 2 + lp.leftMargin
            - lp.rightMargin;
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
        childTop = (getHeight() + getPaddingTop() - getPaddingBottom() - h) / 2 + lp.topMargin
            - lp.bottomMargin;
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

  public void relayoutChild(View child, float scrollDis, int childcount) {
    float absScrollDis = scrollDis > 1 ? 1 : scrollDis;
    float newScale =
        (float) (1 - SCALE_OFFSET * (mMaxVisible - childcount) + absScrollDis * SCALE_OFFSET);
    child.setScaleX(newScale);
    child.setScaleY(newScale);
    child.setTranslationY(TRANS_OFFSET * (mMaxVisible - childcount) - absScrollDis * TRANS_OFFSET);
  }

  /**
   * Set the top view and add the fling listener
   */
  private void setTopView() {
    if (getChildCount() > 0) {
      mActiveCard = getChildAt(getChildCount() - 1);
      if (mActiveCard != null) {
        flingCardListener = new FlingCardListener(this, mActiveCard, mAdapter.getItem(mTopOfStack),
            mRotationDegrees, new FlingCardListener.FlingListener() {
          @Override public void onCardExited() {

            //                        requestLayout();
            removeTopCard();
          }

          @Override public void leftExit(Object dataObject) {
            mFlingListener.onCardExitLeft(dataObject);
          }

          @Override public void rightExit(Object dataObject) {
            mFlingListener.onCardExitRight(dataObject);
          }

          @Override public void onClick(Object dataObject) {
            if (mOnItemClickListener != null) mOnItemClickListener.onItemClicked(0, dataObject);
          }

          @Override public void onScroll(float scrollProgressPercent) {
            mFlingListener.onScroll(scrollProgressPercent);
            int childCount = getChildCount() - 1;
            if (childCount < mMaxVisible) {
              while (childCount > 0) {
                relayoutChild(getChildAt(childCount - 1), Math.abs(scrollProgressPercent),
                    childCount);
                childCount--;
              }
            } else {
              while (childCount > 1) {
                relayoutChild(getChildAt(childCount - 1), Math.abs(scrollProgressPercent),
                    childCount - 1);
                childCount--;
              }
            }
          }

          @Override public void topExit(Object dataObject) {
            mFlingListener.onCardExitTop(dataObject);
          }

          @Override public void bottomExit(Object dataObject) {
            mFlingListener.onCardExitBottom(dataObject);
          }
        });

        mActiveCard.setOnTouchListener(flingCardListener);
      }
    }
  }

  private void removeTopCard() {
    removeViewInLayout(mActiveCard);
    mActiveCard = null;
    mTopOfStack++;
    mCurrentAdapterCount--;
    checkForAdapterCount();
    resetCards();
  }

  private void resetCards() {
    layoutMissingChildren();
    setTopView();
  }



  public void restart() {
    mCurrentAdapterCount = mAdapter.getCount();
    adapterCount = mCurrentAdapterCount;
    mTopOfStack = 0;
    mLastObjectInStack = 0;
    mMaxVisible = mInitialMaxVisible;
    layoutChildren(0, mCurrentAdapterCount);
    requestLayout();
  }

  public FlingCardListener getTopCardListener() throws NullPointerException {
    if (flingCardListener == null) {
      throw new NullPointerException();
    }
    return flingCardListener;
  }

  public void setMaxVisible(int MAX_VISIBLE) {
    this.mMaxVisible = MAX_VISIBLE;
  }

  public void setMinStackInAdapter(int MIN_ADAPTER_STACK) {
    this.mMinAdapterStack = MIN_ADAPTER_STACK;
  }

  @Override public Adapter getAdapter() {
    return mAdapter;
  }

  @Override public void setAdapter(Adapter adapter) {
    if (mAdapter != null && mDataSetObserver != null) {
      mAdapter.unregisterDataSetObserver(mDataSetObserver);
      mDataSetObserver = null;
    }

    mAdapter = adapter;
    mCurrentAdapterCount = adapter.getCount();
    adapterCount = mAdapter.getCount();

    if (mAdapter != null && mDataSetObserver == null) {
      mDataSetObserver = new AdapterDataSetObserver();
      mAdapter.registerDataSetObserver(mDataSetObserver);
    }
  }

  public void setFlingListener(OnCardFlingListener OnCardFlingListener) {
    this.mFlingListener = OnCardFlingListener;
  }

  public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
    this.mOnItemClickListener = onItemClickListener;
  }

  @Override public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new FrameLayout.LayoutParams(getContext(), attrs);
  }

  @Override
  public boolean detectBottomSwipe() {
    return mDetectBottomSwipe;
  }

  @Override
  public boolean detectTopSwipe() {
    return mDetectTopSwipe;
  }

  @Override
  public boolean detectLeftSwipe() {
    return mDetectLeftSwipe;
  }

  @Override
  public boolean detectRightSwipe() {
    return mDetectRightSwipe;
  }

  private class AdapterDataSetObserver extends DataSetObserver {
    @Override public void onChanged() {
      int newAdapterCount = mAdapter.getCount();
      mCurrentAdapterCount += newAdapterCount - adapterCount;
      adapterCount = newAdapterCount;
      resetCards();
    }

    @Override public void onInvalidated() {
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
