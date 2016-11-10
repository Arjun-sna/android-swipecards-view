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
import in.arjsna.swipecardlib.SwipeCardView;


public class CardSwipeActivity extends Activity {

    private ArrayList<Card> al;
    private CardsAdapter arrayAdapter;
    private int i;

    @InjectView(R.id.card_stack_view)
    SwipeCardView swipeCardView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        ButterKnife.inject(this);


        al = new ArrayList<>();
        getDummyData(al);
        arrayAdapter = new CardsAdapter(this, al );


        swipeCardView.setAdapter(arrayAdapter);
        swipeCardView.setFlingListener(new SwipeCardView.OnCardFlingListener() {
            @Override
            public void onCardExitLeft(Object dataObject) {
                makeToast(CardSwipeActivity.this, "Left!");
            }

            @Override
            public void onCardExitRight(Object dataObject) {
                makeToast(CardSwipeActivity.this, "Right!");
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
                getDummyData(al);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onScroll(float scrollProgressPercent) {

            }

            @Override
            public void onCardExitTop(Object dataObject) {
                makeToast(CardSwipeActivity.this, "Top!");
            }

            @Override
            public void onCardExitBottom(Object dataObject) {
                makeToast(CardSwipeActivity.this, "Bottom!");
            }
        });


        // Optionally add an OnItemClickListener
        swipeCardView.setOnItemClickListener(
                new SwipeCardView.OnItemClickListener() {
                    @Override
                    public void onItemClicked(int itemPosition, Object dataObject) {
                        Card card = (Card) dataObject;
                        makeToast(CardSwipeActivity.this, card.name);
                    }
                });

    }

    private void getDummyData(ArrayList<Card> al) {
        Card card = new Card();
        card.name = "Card1";
        card.imageId = R.drawable.faces1;
        al.add(card);

        Card card2 = new Card();
        card2.name = "Card2";
        card2.imageId = R.drawable.faces2;
        al.add(card2);
        Card card3 = new Card();
        card3.name = "Card3";
        card3.imageId = R.drawable.faces3;
        al.add(card3);
        Card card4 = new Card();
        card4.name = "Card4";
        card4.imageId = R.drawable.faces4;
        al.add(card4);
        Card card5 = new Card();
        card5.name = "Card5";
        card5.imageId = R.drawable.faces5;
        al.add(card5);
        Card card56 = new Card();
        card56.name = "Card6";
        card56.imageId = R.drawable.faces6;
        al.add(card56);
        Card card7 = new Card();
        card7.name = "Card7";
        card7.imageId = R.drawable.faces7;
        al.add(card7);
        Card card8 = new Card();
        card8.name = "Card8";
        card8.imageId = R.drawable.faces8;
        al.add(card8);
        Card card9 = new Card();
        card9.name = "Card9";
        card9.imageId = R.drawable.faces9;
        al.add(card9);
        Card card10 = new Card();
        card10.name = "Card10";
        card10.imageId = R.drawable.faces10;
        al.add(card10);
        Card card11 = new Card();
        card11.name = "Card11";
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
        swipeCardView.throwTop();
    }

    @OnClick(R.id.bottom)
    public void bottom() {
        swipeCardView.throwBottom();
    }

    @OnClick(R.id.left)
    public void left() {
        swipeCardView.throwLeft();
    }


    @OnClick(R.id.right)
    public void right() {
        swipeCardView.throwRight();
    }

}
