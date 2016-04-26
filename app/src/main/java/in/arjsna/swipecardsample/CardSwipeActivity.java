package in.arjsna.swipecardsample;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import in.arjsna.swipecardlib.SwipeFlingCardView;


public class CardSwipeActivity extends Activity {

    private ArrayList<Card> al;
    private CardsAdapter arrayAdapter;
    private int i;

    @InjectView(R.id.frame)
    SwipeFlingCardView flingContainer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        ButterKnife.inject(this);


        al = new ArrayList<>();
        getDummyData(al);
        arrayAdapter = new CardsAdapter(this, al );


        flingContainer.setAdapter(arrayAdapter);
        flingContainer.setFlingListener(new SwipeFlingCardView.OnCardFlingListener() {
            @Override
            public void onLeftCardExit(Object dataObject) {
                //Do something on the left!
                //You also have access to the original object.
                //If you want to use it just cast it (String) dataObject
//                makeToast(CardSwipeActivity.this, "Left!");
            }

            @Override
            public void onRightCardExit(Object dataObject) {
//                makeToast(CardSwipeActivity.this, "Right!");
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                // Ask for more data here
//                al.add("XML ".concat(String.valueOf(i)));
//                arrayAdapter.notifyDataSetChanged();
//                Log.d("LIST", "notified");
//                i++;
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
                View view = flingContainer.getSelectedView();
//                view.findViewById(R.id.item_swipe_right_indicator).setAlpha(scrollProgressPercent < 0 ? -scrollProgressPercent : 0);
//                view.findViewById(R.id.item_swipe_left_indicator).setAlpha(scrollProgressPercent > 0 ? scrollProgressPercent : 0);
            }

            @Override
            public void onTopCardExit(Object dataObject) {
//                makeToast(CardSwipeActivity.this, "Top!");
            }

            @Override
            public void onBottomCardExit(Object dataObject) {

            }
        });


        // Optionally add an OnItemClickListener
        flingContainer.setOnItemClickListener(
                new SwipeFlingCardView.OnItemClickListener() {
            @Override
            public void onItemClicked(int itemPosition, Object dataObject) {
                Card card = (Card) dataObject;
                makeToast(CardSwipeActivity.this, card.name);
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

    static void makeToast(Context ctx, String s){
        Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
    }


    @OnClick(R.id.top)
    public void top() {
        /**
         * Trigger the right event manually.
         */
        flingContainer.getTopCardListener().selectTop();
    }

    @OnClick(R.id.bottom)
    public void bottom() {
        flingContainer.getTopCardListener().selectBottom();
    }
//

    @OnClick(R.id.left)
    public void left() {
        flingContainer.getTopCardListener().selectLeft();
    }


    @OnClick(R.id.right)
    public void right() {
        flingContainer.getTopCardListener().selectRight();
    }



}
