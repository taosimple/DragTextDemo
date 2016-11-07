package com.taotao.dragtextdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by taotao on 16/11/3.
 */

public class BasicTextView extends TextView{

    private final String TAG = "BasicTextView";

    private List<String> inputTextList;
    List<List<Rect>> itemBoundList;

    private Paint rectPaint, blankPaint, cursorPaint;

    private float downX, downY;

    private int insertIndex = -1;
    private int dragIndex = -1;
    private String dragText;
    private List<Rect> dragBounds;

    public BasicTextView(Context context) {
        this(context, null);
    }

    public BasicTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setColor(0x50888888);
        rectPaint.setStyle(Paint.Style.FILL);
//        rectPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));

        blankPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blankPaint.setColor(Color.WHITE);
        blankPaint.setStyle(Paint.Style.FILL);

        cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cursorPaint.setColor(Color.BLUE);
        cursorPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));

    }
    
    public void setInputText(String inputText){
//        inputText = inputText.replace(" ", "_");
        inputTextList = new LinkedList<>(Arrays.asList(inputText.split("\\|")));
        StringBuffer stringBuffer = new StringBuffer(inputTextList.size() + 1);
        for(String item : inputTextList){
            stringBuffer.append(item);
        }
        
        setText(stringBuffer);


        postDelayed(new Runnable() {
            @Override
            public void run() {
                calculateItemBounds(0);
                invalidate();
            }
        }, 100);
    }


    private void calculateItemBounds(int startIndex){
        TextPaint textPaint = getPaint();

        List<String> inputList = new LinkedList<>(inputTextList);

        for (String item : inputList){
            Rect bound = new Rect();
            textPaint.getTextBounds(item, 0, item.length(), bound);
        }


        Rect bound = new Rect();
        textPaint.getTextBounds(getText().toString(), 0, getText().length(), bound);

        Rect lineBount = new Rect();
        getLineBounds(0, lineBount);

        String content = getText().toString();
        Layout layout = getLayout();

        itemBoundList = new ArrayList<>(inputList.size());

        int lineHeight = getLineHeight();

        boolean isChain = false;//跨行


        for(int i = 0; i < layout.getLineCount(); i++){
            int lineStart = layout.getLineStart(i);
            int lineEnd = layout.getLineEnd(i);
            String line = content.substring(lineStart, lineEnd);
            Log.i(TAG, "line = " + line);
//            if(true) continue;
            String item = inputList.remove(0);
            int itemLeft = 0;
            int top = lineHeight*i;

            //空行
            while (line.contains(item)){
                Log.v(TAG, "item = " + item + " line = " + line);
                Rect rect = getItemBound(item, itemLeft, top, textPaint);
                itemLeft = rect.right ;

                List<Rect> itemRects ;
                if(isChain){
                    itemRects = itemBoundList.get(itemBoundList.size() - 1);
                    itemRects.add(rect);
                    Log.i(TAG, "CHAIN : " + itemRects);
                }else{
                    itemRects = new ArrayList<>(1);
                    itemRects.add(rect);
                    itemBoundList.add(itemRects);
                }


                line = line.substring(item.length(), line.length());
                if(!line.isEmpty()){
                    item = inputList.remove(0);
                }

                isChain = false;
            }

            if(!line.isEmpty()){
                Rect rect = getItemBound(line, itemLeft, top, textPaint);

                List<Rect> itemRects;
                if(isChain){//跨多行的情况
                    itemRects = itemBoundList.get(itemBoundList.size() - 1);
                    itemRects.add(rect);
                }else{
                    itemRects = new ArrayList<>(2);
                    itemRects.add(rect);
                    itemBoundList.add(itemRects);
                }

//                item = inputTextList.remove(0);

                Log.d(TAG, "rest item = " + item + " line = " + line + "  " + itemRects);

                String itemRest = item.substring(line.length(), item.length());

                inputList.add(0, itemRest);

                isChain = true;
            }

        }

    }


    private Rect getItemBound(String item, int offsetX, int offsetY, TextPaint textPaint){
        Rect rect = new Rect();
        float width = textPaint.measureText(item);
//        textPaint.getTextBounds(item, 0, item.length(), rect);
        rect.set(offsetX, offsetY, (int) (width + offsetX), offsetY + getLineHeight());
//        rect.offset(0, getLayout().getline);
        return rect;
    }




    @Override
    protected void onDraw(Canvas canvas) {

        //draw background frame
        if(itemBoundList != null){
            TextPaint textPaint = getPaint();
            int offsetY = getLineHeight() - Util.getTextHeight(textPaint);
            int spaceWith = (int) textPaint.measureText(" ", 0, 1);
            for(List<Rect> rectList : itemBoundList){

                if(rectList.size() == 1){
                    Rect rect = rectList.get(0);
                    RectF rectF = new RectF(rect.left, rect.top + offsetY, rect.right - spaceWith, rect.bottom);
                    int r = (int) (rectF.height()*0.4f);
                    canvas.drawRoundRect(rectF, r, r, rectPaint);

                }else{
                    for(int i = 0; i < rectList.size(); i++){
                        Rect rect = rectList.get(i);
                        RectF rectF = new RectF(rect.left, rect.top + offsetY, rect.right - spaceWith, rect.bottom);
                        int r = (int) (rectF.height()*0.4f);
                        if(i == 0){
                            canvas.drawPath(Util.createRoundRect(rectF, r, 0, 0, r), rectPaint);
                        }else if(i == rectList.size() - 1){
                            canvas.drawPath(Util.createRoundRect(rectF, 0, r, r, 0), rectPaint);
                        }else{
                            canvas.drawRect(rectF, rectPaint);
                        }
                    }

                }

            }
        }

        //draw insert cursor
        if(insertIndex != -1 && dragIndex != -1){
            TextPaint textPaint = getPaint();
            int offset = (int) textPaint.measureText(" ", 0, 1)/2;
            Rect rect = itemBoundList.get(insertIndex).get(0);
            if(insertIndex > dragIndex){
                canvas.drawLine(rect.right + offset, rect.top, rect.right + offset, rect.bottom , cursorPaint);
            }else{
                canvas.drawLine(rect.left - offset, rect.top, rect.left - offset, rect.bottom , cursorPaint);
            }

        }

        //draw text
        super.onDraw(canvas);

        //draw blank
        if(dragBounds != null){
            for(Rect rect : dragBounds){
                canvas.drawRect(rect, blankPaint);
            }
        }

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d(TAG, "onTouchEvent() called with: event = [" + event.getAction() + "]");
        int action = event.getAction();

        switch (action){
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                Log.d(TAG, "DOWN : " + downX + ":" + downY);

                break;

        }
        return super.onTouchEvent(event);
    }



    public int startDragText(){
        for(int i = 0; i < itemBoundList.size(); i++){
            List<Rect> rectList = itemBoundList.get(i);
            for (Rect rect : rectList){
                if(rect.contains((int) downX, (int) downY)){
                    dragText = inputTextList.get(i);
                    dragIndex = i;
                    dragBounds = rectList;
                    Log.i(TAG, "dragText = " + dragText + "  index = " + dragIndex);
                    return dragIndex;
                }
            }
        }

        return dragIndex;
    }

    public void changeItemTextPosition(int index){
        Log.d(TAG, "changeItemTextPosition() called with: index = [" + index + "]" + " dragIndex = " + dragIndex);

        if(index == dragIndex){
            invalidate();
            return;
        }

        //index 已作补偿
        String t = inputTextList.remove(dragIndex);
        inputTextList.add(index, t);


        StringBuffer stringBuffer = new StringBuffer(inputTextList.size() + 1);
        for(String item : inputTextList){
            stringBuffer.append(item);
        }
        setText(stringBuffer);

        calculateItemBounds(0);


    }

    /**
     *
     * @param x
     * @param y
     * @return 返回的index已经是补偿过了,所以后面插入位置计算直接用就可以了
     */
    public int findInsertPosition(float x, float y){
        for (int i = 0; i < itemBoundList.size(); i++){
            List<Rect> rectList = itemBoundList.get(i);
            for(Rect rect : rectList){
                if(rect.contains((int) x, (int) y)){
                    if(i < dragIndex){
                        if(x > rect.centerX()){//判断插入左右位置
                            insertIndex = i + 1;
                        }else{
                            insertIndex = i;
                        }
                    }else if(i > dragIndex){
                        if(x > rect.centerX()){//判断插入左右位置
                            insertIndex = i;
                        }else{
                            insertIndex = i - 1;
                        }
                    }else{
                        insertIndex = i;
                    }
                    Log.i(TAG, "insert index = " + insertIndex + "  drag index = " + dragIndex);
                    return insertIndex;
                }
            }
        }
        Log.e(TAG, "insert index = " + insertIndex + "  bounds = " + itemBoundList.size());
        if(insertIndex == -1) insertIndex = dragIndex;
        return insertIndex;
    }


    public void reset(){
        insertIndex = -1;
        dragIndex = -1;
        dragBounds = null;
        dragText = null;
    }

    public void resetInsertIndex() {
        this.insertIndex = -1;
    }

    public List<List<Rect>> getItemBoundList() {
        return itemBoundList;
    }

    public List<String> getInputTextList() {
        return inputTextList;
    }

    public int getDragIndex() {
        return dragIndex;
    }

    public String getDragText() {
        return dragText;
    }

    public List<Rect> getDragBounds() {
        return dragBounds;
    }
}
