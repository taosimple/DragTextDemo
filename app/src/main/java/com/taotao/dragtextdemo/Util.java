package com.taotao.dragtextdemo;

import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;

/**
 * Created by taotao on 16/11/7.
 */

public class Util {

    public static Path createRoundRect(RectF rect, int lt, int rt, int rb, int lb){
        Path path = new Path();
        if(lt == 0){
            path.moveTo(rect.left, rect.top);
        }else{
            path.moveTo(rect.left + lt, rect.top);
            path.addArc(new RectF(rect.left, rect.top, rect.left + lt, rect.top + lt), 270, -90);
        }

        if(lb == 0){
            path.lineTo(rect.left, rect.bottom);
        }else{
            path.lineTo(rect.left, rect.bottom - lb);
            path.arcTo(new RectF(rect.left, rect.bottom - lb, rect.left + lb, rect.bottom), 180, -90);
        }

        if(rb == 0){
            path.lineTo(rect.right, rect.bottom);
        }else{
            path.lineTo(rect.right - rb, rect.bottom);
            path.arcTo(new RectF(rect.right - rb, rect.bottom - rb, rect.right, rect.bottom), 90, -90);
        }

        if(rt == 0){
            path.lineTo(rect.right, rect.top);
        }else{
            path.lineTo(rect.right, rect.top + rt);
            path.arcTo(new RectF(rect.right - rt, rect.top, rect.right, rect.top + rt), 0, -90);
        }

        path.close();

        return path;
    }


    public static int getTextHeight(TextPaint textPaint){
        Rect rect = new Rect();
        textPaint.getTextBounds("Gj", 0, 2, rect);
        return rect.height();
    }

}
