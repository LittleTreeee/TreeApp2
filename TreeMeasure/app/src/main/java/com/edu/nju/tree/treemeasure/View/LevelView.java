package com.edu.nju.tree.treemeasure.View;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import com.edu.nju.tree.treemeasure.R;

public class LevelView extends View {
    // 定义水平仪中的气泡图标
    public Bitmap bubble;
    // 定义水平仪中气泡的X、Y坐标
    public int bubbleX, bubbleY;

    public LevelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 加载水平仪图片和气泡图片
        bubble = BitmapFactory.decodeResource(getResources(),
                R.drawable.bubble);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 根据气泡坐标绘制气泡
        canvas.drawBitmap(bubble, bubbleX, bubbleY, null);
    }
}
