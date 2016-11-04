package com.taotao.dragtextdemo;

import android.graphics.Rect;

import java.util.List;

/**
 * Created by taotao on 16/11/4.
 */

public interface DragAble {

    void dragText(List<Rect> rectList, String dragText);

}
