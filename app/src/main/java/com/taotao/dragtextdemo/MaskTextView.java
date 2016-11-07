package com.taotao.dragtextdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.List;

/**
 * Created by taotao on 16/11/3.
 */

public class MaskTextView extends TextView {

    private final String TAG = "MaskTextView";

    private Paint rectPaint;

    public MaskTextView(Context context) {
        this(context, null);
    }

    public MaskTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init(){
//        setBackgroundColor(0x50FFFF00);
        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setColor(0x50888888);
        rectPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d(TAG, "onTouchEvent() called with: event = [" + event.getAction() + "]");
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw() called with: showDragText = [" + showDragText + "]");
        if(showDragText){
            showDragText(canvas);
            showDragText = false;
        }
    }

    private void showDragText(Canvas canvas){
        TextPaint textPaint = getPaint();
        int offsetY = getLineHeight() - Util.getTextHeight(textPaint);
        int spaceWith = (int) textPaint.measureText(" ", 0, 1);

        if(dragTextBounds.size() > 1){
            for(Rect rect : dragTextBounds){
                int count = textPaint.breakText(dragText, true, rect.width(), null);
                String text = dragText.substring(0, count);
                canvas.drawText(text, rect.left, rect.bottom, textPaint);
                dragText = dragText.substring(count, dragText.length());
            }

        }else{
            Rect origin = dragTextBounds.get(0);
            canvas.drawText(dragText, origin.left, origin.bottom, textPaint);

            RectF rectF = new RectF(origin.left, origin.top + offsetY, origin.right - spaceWith, origin.bottom);
            int r = (int) (rectF.height()*0.4f);
            canvas.drawRoundRect(rectF, r, r, rectPaint);
        }

    }

    public void clearText(){
        showDragText = false;
        invalidate();
    }

    private List<Rect> dragTextBounds;
    private String dragText;
    private boolean showDragText;

    public void dragText(List<Rect> rectList, String dragText){
        Log.d(TAG, "dragText");
        this.dragText = dragText;
        this.dragTextBounds = rectList;
        showDragText = true;
        invalidate();
    }

    public List<Rect> getDragTextBounds() {
        return dragTextBounds;
    }
}
