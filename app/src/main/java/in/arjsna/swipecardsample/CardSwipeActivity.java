package in.arjsna.swipecardsample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import in.arjsna.swipecardlib.SwipeCardView;
import java.util.ArrayList;

public class CardSwipeActivity extends AppCompatActivity {

  private ArrayList<Card> al;
  private CardsAdapter arrayAdapter;
  private int i;

  @BindView(R.id.card_stack_view)
  public SwipeCardView swipeCardView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_my);
    ButterKnife.bind(this);

    al = new ArrayList<>();
    getDummyData(al);
    arrayAdapter = new CardsAdapter(this, al);

    swipeCardView.setAdapter(arrayAdapter);
    swipeCardView.setFlingListener(new SwipeCardView.OnCardFlingListener() {
      @Override public void onCardExitLeft(Object dataObject) {
        makeToast(CardSwipeActivity.this, "Left!");
      }

      @Override public void onCardExitRight(Object dataObject) {
        makeToast(CardSwipeActivity.this, "Right!");
      }

      @Override public void onAdapterAboutToEmpty(int itemsInAdapter) {

      }

      @Override public void onScroll(float scrollProgressPercent) {

      }

      @Override public void onCardExitTop(Object dataObject) {
        makeToast(CardSwipeActivity.this, "Top!");
      }

      @Override public void onCardExitBottom(Object dataObject) {
        makeToast(CardSwipeActivity.this, "Bottom!");
      }
    });

    // Optionally add an OnItemClickListener
    swipeCardView.setOnItemClickListener(new SwipeCardView.OnItemClickListener() {
      @Override public void onItemClicked(int itemPosition, Object dataObject) {
        makeToast(CardSwipeActivity.this, String.valueOf(swipeCardView.getCurrentPosition()));
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
  }

  static void makeToast(Context ctx, String s) {
    Toast.makeText(ctx, s, Toast.LENGTH_SHORT).show();
  }

  @OnClick(R.id.top) public void top() {
    /**
     * Trigger the right event manually.
     */
    swipeCardView.throwTop();
  }

  @OnClick(R.id.bottom) public void bottom() {
    swipeCardView.throwBottom();
  }

  @OnClick(R.id.left) public void left() {
    swipeCardView.throwLeft();
  }

  @OnClick(R.id.right) public void right() {
    swipeCardView.throwRight();
  }
  @OnClick(R.id.restart) public void restart() {
    swipeCardView.restart();
  }

  @OnClick(R.id.position)
  public void toastCurrentPosition(){
    makeToast(this, String.valueOf(swipeCardView.getCurrentPosition()));
  }
}
