package in.arjsna.swipecardlib;

/**
 * Created by dvelasquez on 5/2/17.
 */

public interface SwipeableView {
    boolean detectBottomSwipe();
    boolean detectTopSwipe();
    boolean detectLeftSwipe();
    boolean detectRightSwipe();
}
