package com.taotao.dragtextdemo;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;

public class MainActivity extends AppCompatActivity {

    private static final String TEST_STRING = "This |post |is going to |be |heavy |on |code |examples|. |It |will |cover |creating |a custom View |that |responds to |touch |events |and |allows |the |user |to |manipulate |an |object |drawn |within it|. |To get the most out of |the |examples |you |should be |familiar with| setting up |an |Activity |and |the |basics of |the Android UI system|. |Full project |source |will |be |linked |at the end|.";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        DragTextLayout dragTextLayout = (DragTextLayout) findViewById(R.id.drag_text_layout);
        dragTextLayout.setText(TEST_STRING);
        dragTextLayout.setTextColor(Color.BLACK);
        dragTextLayout.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);

    }





}
