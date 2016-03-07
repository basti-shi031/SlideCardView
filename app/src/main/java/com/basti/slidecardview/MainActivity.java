package com.basti.slidecardview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.basti.slidecardviewlib.SlideCardView;
import com.basti.slidecardviewlib.SlideCardViewListener;

public class MainActivity extends AppCompatActivity {

    private SlideCardView slideCardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();

        /*TextView textView1 = new TextView(this);
        textView1.setText("123");
        textView1.setTextColor(Color.parseColor("#303030"));

        TextView textView2 = new TextView(this);
        textView2.setText("321");
        textView2.setTextColor(Color.parseColor("#303030"));

        slideCardView.addView(textView1);
        slideCardView.addView(textView2);*/
        for (int i = 0;i<3;i++){

            /*Button button = new Button(this);
            button.setText("第"+ i +"个item");
            slideCardView.addView(button);*/

            View view = getLayoutInflater().inflate(R.layout.item,null,false);
            slideCardView.addView(view);

        }

        slideCardView.setSlideCardViewListener(new SlideCardViewListener() {
            @Override
            public void deleteFinished(int index, View childview) {
                Log.i("index of delete View",index+"");
            }
        });
    }

    private void initView() {
        slideCardView = (SlideCardView) findViewById(R.id.slidecardview);
    }
}
