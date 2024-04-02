package zzh;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import zzh.source.hl2.R;
public class ImprovemView extends View {
// 
    //我的注释已经给的够详细了 如果报错那就重开
    private Paint paint;
    private float mouseX, mouseY;
    private Drawable mouseDrawable;
    public ImprovemView(Context context) {
        super(context);
        init();
    }

    public ImprovemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    public ImprovemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
 private void init() {
    paint = new Paint();
    paint.setAntiAlias(true);
    //加载鼠标图
    mouseDrawable = getResources().getDrawable(R.drawable.ic_kaq); //这个是鼠标 换成你的实际吧
        //注意导入的时候R资源随时替换
    //获取鼠标图片的原始宽度和高度
    int originalWidth = mouseDrawable.getIntrinsicWidth();
    int originalHeight = mouseDrawable.getIntrinsicHeight();
    //获取屏幕宽度
    int screenWidth = getResources().getDisplayMetrics().widthPixels;
    //计算鼠标图片的目标宽度，假设为屏幕宽度的百分之一
    int targetWidth = screenWidth / 40;
    //根据原始宽高比计算目标高度
    int targetHeight = (int) (1.0 * targetWidth * originalHeight / originalWidth);
    //设置鼠标图片的大小
    mouseDrawable.setBounds(0, 0, targetWidth, targetHeight);
}
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //绘制鼠标图片 这个是方便注意到的
        canvas.save();
        canvas.translate(mouseX, mouseY);
        mouseDrawable.draw(canvas);
        canvas.restore();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mouseX = event.getX() - mouseDrawable.getBounds().width() / 2;
        mouseY = event.getY() - mouseDrawable.getBounds().height() / 2;
        invalidate();
        return false;
    }
    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }
}