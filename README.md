# SwipeCardsView [![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-SwipeCardsView-green.svg?style=true)](https://android-arsenal.com/details/1/4141) [ ![Download](https://api.bintray.com/packages/arjun-sna/maven/swipecardview/images/download.svg) ](https://bintray.com/arjun-sna/maven/swipecardview/_latestVersion)

##Demo
<img src="https://rawgit.com/Arjun-sna/Arjun-sna.github.io/master/raw/swipecarddemo.gif" width="250" />

##Installation
```
repositories {
    jcenter()
}

dependencies {
    compile 'in.arjsna:swipecardlib:1.0.2'
}
```

##Usage
Add `SwipeCardView` to the layout xml file where it is needed
```xml
<in.arjsna.swipecardlib.SwipeCardView
    android:id="@+id/frame"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_weight="1"
    app:rotation_degrees="15.5"
    app:bottom_swipe_detect="false"
    tools:context=".CardSwipeActivity" />
```
The various customisation attribures available are

`max_visible` - maximum card to be show in stack as visible

`min_adapter_stack` - minimum card count left at which callback about adapter to empties will be called

`left_swipe_detect` - whether swipe to lelf should be enabled or not. `true` by default

`right_swipe_detect` - whether swipe to right should be enabled or not. `true` by default

`top_swipe_detect` - whether swipe to top should be enabled or not. `true` by default

`bottom_swipe_detect` - whether swipe to bottom should be enabled or not. `true` by default

Create an `ArrayAdapter` with card list
```java
CardsAdapter arrayAdapter = new CardsAdapter(this, al );
```

Set the adapter and fling listener to SwipeCardView
```java
SwipeCardView swipeCardView = (SwipeCardView) findViewById(R.id.swipe_card_view);
swipeCardView.setAdapter(arrayAdapter);
swipeCardView.setFlingListener(new SwipeCardView.OnCardFlingListener() {
            @Override
            public void onCardExitLeft(Object dataObject) {
              Log.i(TAG, "Left Exit");                
            }

            @Override
            public void onCardExitRight(Object dataObject) {
              Log.i(TAG, "Right Exit");
            }

            @Override
            public void onAdapterAboutToEmpty(int itemsInAdapter) {
              Log.i(TAG, "Adater to be empty");
              //add more items to adapter and call notifydatasetchanged
            }

            @Override
            public void onScroll(float scrollProgressPercent) {
              Log.i(TAG, "Scroll");
            }

            @Override
            public void onCardExitTop(Object dataObject) {
              Log.i(TAG, "Top Exit");
            }

            @Override
            public void onCardExitBottom(Object dataObject) {
              Log.i(TAG, "Bottom Exit");
            }
        });
```

The cards can be removed by code with fling animation

```java
swipeCardView.throwRight(); //throw card to right
swipeCardView.throwLeft(); //throw card to left
swipeCardView.throwTop(); //throw card to top
swipeCardView.throwBottom(); //throw card to bottom
```

License
=======

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
