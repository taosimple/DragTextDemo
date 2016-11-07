package com.taotao.dragtextdemo;

import android.content.Context;
import android.graphics.Rect;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by taotao on 16/11/3.
 */

public class DragTextLayout extends FrameLayout {

    final String TAG = "DragTextLayout";

    public DragTextLayout(Context context) {
        this(context, null);
    }

    public DragTextLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private BasicTextView basicTextView;
    private MaskTextView maskTextView;

    private int settleIndex = -1;
    private boolean isStartDrag;

    private ViewDragHelper dragHelper ;

    private void init(Context context){
        basicTextView = new BasicTextView(context);
        maskTextView = new MaskTextView(context);
        addView(basicTextView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(maskTextView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        dragHelper = ViewDragHelper.create(this, new ViewDragHelper.Callback() {
            @Override
            public boolean tryCaptureView(View child, int pointerId) {
//                Log.d(TAG, "tryCaptureView() called with: child = [" + child + "], pointerId = [" + pointerId + "]");
                if(child.equals(maskTextView)){
                    isStartDrag = false;
                    return (basicTextView.startDragText() != -1);
                }
                return false;
            }

            @Override
            public void onViewCaptured(View capturedChild, int activePointerId) {
//                Log.d(TAG, "onViewCaptured() called with: activePointerId = [" + activePointerId + "]");
                super.onViewCaptured(capturedChild, activePointerId);

            }

            @Override
            public int clampViewPositionHorizontal(View child, int left, int dx) {
//                Log.d(TAG, "clampViewPositionHorizontal() called with: left = [" + left + "], dx = [" + dx + "]");
                return left;
            }

            @Override
            public int clampViewPositionVertical(View child, int top, int dy) {
//                Log.d(TAG, "clampViewPositionVertical() called with: top = [" + top + "], dy = [" + dy + "]");
                return top;
            }

            @Override
            public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
//                Log.d(TAG, "onViewPositionChanged() called with: left = [" + left + "], top = [" + top + "], dx = [" + dx + "], dy = [" + dy + "]");
                super.onViewPositionChanged(changedView, left, top, dx, dy);
                if(!isStartDrag){
                    isStartDrag = true;
                    maskTextView.dragText(basicTextView.getDragBounds(), basicTextView.getDragText());
                    basicTextView.invalidate();
                }

                if(dragHelper.getViewDragState() == ViewDragHelper.STATE_DRAGGING){
                    int index = basicTextView.findInsertPosition(touchX, touchY);
                    if(index != settleIndex){
                        basicTextView.invalidate();
                    }
                    settleIndex = index;
                }
            }

            @Override
            public void onViewReleased(View releasedChild, float xvel, float yvel) {
                Log.d(TAG, "onViewReleased() called with:  XY = [" + releasedChild.getX() + ":" + releasedChild.getY() + "], TranslationXY = [" + releasedChild.getTranslationX() + ":" + releasedChild.getTranslationY() + "], posXY = [" + releasedChild.getLeft() + ":" + releasedChild.getTop() + "]" + "], scrollXY = [" + releasedChild.getScrollX() + ":" + releasedChild.getScrollY() + "]");
                super.onViewReleased(releasedChild, xvel, yvel);
                if(!isStartDrag) return;

                settleIndex = basicTextView.findInsertPosition(touchX, touchY);

                basicTextView.resetInsertIndex();

                basicTextView.changeItemTextPosition(settleIndex);
                Rect dstRect = basicTextView.getItemBoundList().get(settleIndex).get(0);
                Rect startRect = maskTextView.getDragTextBounds().get(0);

                basicTextView.reset();
                dragHelper.settleCapturedViewAt(dstRect.left - startRect.left, dstRect.top - startRect.top);
                invalidate();
            }

            @Override
            public void onViewDragStateChanged(int state) {
                Log.d(TAG, "onViewDragStateChanged() called with: state = [" + state + "]");
                super.onViewDragStateChanged(state);
            }
        });
    }


    @Override
    public void computeScroll() {
//        Log.d(TAG, "computeScroll() called state = " + dragHelper.getViewDragState());
        if(dragHelper.continueSettling(true)) {
            invalidate();
        }else{
            Log.d(TAG, "FINISH DRAG  XY = [" + maskTextView.getX() + ":" + maskTextView.getY() + "], TranslationXY = [" + maskTextView.getTranslationX() + ":" + maskTextView.getTranslationY() + "], posXY = [" + maskTextView.getLeft() + ":" + maskTextView.getTop() + "]" + "], scrollXY = [" + maskTextView.getScrollX() + ":" + maskTextView.getScrollY() + "]");
            if(settleIndex != -1){
                removeView(maskTextView);
                maskTextView.clearText();
                addView(maskTextView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
                settleIndex = -1;
            }

        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean ret = dragHelper.shouldInterceptTouchEvent(ev);
        Log.d(TAG, "onInterceptTouchEvent() called with: ev = [" + ev.getAction() + "]" + " ret = " + ret);
        return ret;
    }

    private float touchX, touchY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        touchX = event.getX();
        touchY = event.getY();
//        Log.d(TAG, "onTouchEvent() called with: event = [" + action + "-" + touchX + ":" + touchY + "]");

        dragHelper.processTouchEvent(event);

        return true;
    }


    public void setText(String text){
        basicTextView.setInputText(text);
    }

    public void setTextColor(int color) {
        basicTextView.setTextColor(color);
        maskTextView.setTextColor(color);
    }

    public void setTextSize(float size) {
        basicTextView.setTextSize(size);
        maskTextView.setTextSize(size);
    }

    public void setTextSize(int unit, float size) {
        basicTextView.setTextSize(unit, size);
        maskTextView.setTextSize(unit, size);
    }


    public BasicTextView getBasicTextView() {
        return basicTextView;
    }
}
