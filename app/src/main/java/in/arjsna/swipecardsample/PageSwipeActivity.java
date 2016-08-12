package in.arjsna.swipecardsample;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import in.arjsna.swipecardlib.SwipePageView;

public class PageSwipeActivity extends Activity {

    private ArrayList<Card> al;
    private PageAdapter arrayAdapter;

    @InjectView(R.id.page_swipe_view)
    SwipePageView flingContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_swipe);
        ButterKnife.inject(this);


        al = new ArrayList<>();
        getDummyData(al);
        arrayAdapter = new PageAdapter(this, al );


        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipePageView.OnPageFlingListener() {
            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
            }

            @Override
            public void onTopCardExit(Object dataObject) {
            }

            @Override
            public void onBottomCardExit(Object dataObject) {
            }
        });
        flingContainer.setOnItemClickListener(new SwipePageView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
            }
        });


    }
    private void getDummyData(ArrayList<Card> al) {
        Card card = new Card();
        card.name = "John";
        card.imageId = R.drawable.faces1;
        al.add(card);

        Card card2 = new Card();
        card2.name = "Mike";
        card2.imageId = R.drawable.faces2;
        al.add(card2);
        Card card3 = new Card();
        card3.name = "Ronoldo";
        card3.imageId = R.drawable.faces3;
        al.add(card3);
        Card card4 = new Card();
        card4.name = "Messi";
        card4.imageId = R.drawable.faces4;
        al.add(card4);
        Card card5 = new Card();
        card5.name = "Sachin";
        card5.imageId = R.drawable.faces5;
        al.add(card5);
        Card card56 = new Card();
        card56.name = "Dhoni";
        card56.imageId = R.drawable.faces6;
        al.add(card56);
        Card card7 = new Card();
        card7.name = "Kohli";
        card7.imageId = R.drawable.faces7;
        al.add(card7);
        Card card8 = new Card();
        card8.name = "Pandya";
        card8.imageId = R.drawable.faces8;
        al.add(card8);
        Card card9 = new Card();
        card9.name = "Nehra";
        card9.imageId = R.drawable.faces9;
        al.add(card9);
        Card card10 = new Card();
        card10.name = "Bumra";
        card10.imageId = R.drawable.faces10;
        al.add(card10);
        Card card11 = new Card();
        card11.name = "Rohit";
        card11.imageId = R.drawable.faces11;
        al.add(card11);
    }
}
