package in.arjsna.swipecardsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class HomeActivity extends Activity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_home);
    findViewById(R.id.card_swipe_demo).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Intent intent = new Intent(HomeActivity.this, CardSwipeActivity.class);
        startActivity(intent);
      }
    });
    findViewById(R.id.page_swipe_demo).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Intent intent = new Intent(HomeActivity.this, PageSwipeActivity.class);
        startActivity(intent);
      }
    });
    findViewById(R.id.card_swipe_fragment_demo).setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View v) {
        Intent intent = new Intent(HomeActivity.this, CardSwipeWithFragment.class);
        startActivity(intent);
      }
    });
  }
}
