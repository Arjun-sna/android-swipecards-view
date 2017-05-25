package in.arjsna.swipecardsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import in.arjsna.swipecardlib.SwipeCardView;
import in.arjsna.swipecardlib.SwipeFrameView;

public class SingleCardSwipeCustomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_card_swipe);
        //Set a transparent style for this activity in the Manifest file
        //Set this to make SwipeFrameView fullscreen
        getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        SwipeFrameView frameView = (SwipeFrameView) findViewById(R.id.frame_view);

        frameView.setFlingListener("My Custom object", new SwipeCardView.OnCardFlingListener() {
            @Override
            public void onCardExitLeft(Object dataObject) {
                    finish();
            }

            @Override
            public void onCardExitRight(Object dataObject) {
                finish();
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {

            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }

            @Override
            public void onCardExitTop(Object dataObject) {
                finish();
            }

            @Override
            public void onCardExitBottom(Object dataObject) {
                finish();
            }
        });

    }

}
