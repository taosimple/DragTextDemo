package com.taotao.dragtextdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.Layout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by taotao on 16/11/3.
 */

public class BasicTextView extends TextView{

    private final String TAG = "BasicTextView";

    private List<String> inputTextList;
    List<List<Rect>> itemBoundList;

    private Paint rectPaint;

    private float downX, downY;

    public BasicTextView(Context context) {
        this(context, null);
    }

    public BasicTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rectPaint.setColor(Color.GREEN);
        rectPaint.setStyle(Paint.Style.STROKE);
        rectPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
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
                postInvalidate();
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
        if(itemBoundList != null){
            for(List<Rect> rectList : itemBoundList){
                ListIterator<Rect> iterator = rectList.listIterator();
                while (iterator.hasNext()){
                    Rect rect = iterator.next();
                    if(iterator.hasNext()){
                        Path path = new Path();
                        path.moveTo(rect.right, rect.top);
                        path.lineTo(rect.left, rect.top);
                        path.lineTo(rect.left, rect.bottom);
                        path.lineTo(rect.right, rect.bottom);
                        canvas.drawPath(path, rectPaint);
                    }else{
                        canvas.drawRect(rect, rectPaint);
                    }
                }
            }
        }
        super.onDraw(canvas);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d(TAG, "onTouchEvent() called with: event = [" + event.getAction() + "]");
        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN){
            reset();
            downX = event.getX();
            downY = event.getY();
            Log.d(TAG, "DOWN : " + downX + ":" + downY);
        }
        return super.onTouchEvent(event);
    }

    private int dragIndex = -1;
    public int startDragText(){
        for(int i = 0; i < itemBoundList.size(); i++){
            List<Rect> rectList = itemBoundList.get(i);
            for (Rect rect : rectList){
                if(rect.contains((int) downX, (int) downY)){
                    String dragText = inputTextList.get(i);
                    dragIndex = i;
                    Log.i(TAG, "dragText = " + dragText + "  index = " + dragIndex);
                    ViewParent parent;
                    if(dragIndex != -1 && (parent = getParent()) instanceof DragTextLayout){
                        ((DragTextLayout)parent).dragText(rectList, dragText);
                    }
                    return dragIndex;
                }
            }
        }

        return dragIndex;
    }

    public void changeItemTextPosition(int index){
        Log.d(TAG, "changeItemTextPosition() called with: index = [" + index + "]" + " dragIndex = " + dragIndex);

        if(index == dragIndex){
            return;
        }

        moveListItem(inputTextList, dragIndex, index);

        StringBuffer stringBuffer = new StringBuffer(inputTextList.size() + 1);
        for(String item : inputTextList){
            stringBuffer.append(item);
        }
        setText(stringBuffer);

        calculateItemBounds(0);


    }

    public int findInsertPosition(float x, float y){
        int insertIndex = -1;
        float dis = 0;
        int nearestIndex;

        for (int i = 0; i < itemBoundList.size(); i++){
            List<Rect> rectList = itemBoundList.get(i);
            for(Rect rect : rectList){
                float disX = x - rect.centerX();
                float disY = y - rect.centerY();
//                disX*disX + disY*disY;
                if(rect.contains((int) x, (int) y)){
                    if(x > rect.centerX()){//判断插入左右位置
                        insertIndex = i + 1;
                        if(insertIndex >= itemBoundList.size()){
                            insertIndex = i;
                        }
                    }else{
                        insertIndex = i;
                    }
                    Log.i(TAG, "insert index = " + insertIndex);
                    return insertIndex;
                }
            }
        }
        Log.e(TAG, "insert index = " + insertIndex + "  bounds = " + itemBoundList.size());
        if(insertIndex == -1) insertIndex = dragIndex;
        return insertIndex;
    }



    public void reset(){
        dragIndex = -1;
    }

    public List<List<Rect>> getItemBoundList() {
        return itemBoundList;
    }

    public List<String> getInputTextList() {
        return inputTextList;
    }


    public <T> List<T> moveListItem(List<T> list, int fromIndex, int toIndex){
        if(fromIndex == toIndex) return list;
        T t = list.remove(fromIndex);
        if(fromIndex < toIndex){
            toIndex ++;
        }
        list.add(toIndex, t);
        return list;
    }
}
