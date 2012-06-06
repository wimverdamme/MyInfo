package my.info;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

public class ArrowDirectionView extends TextView
{
	private Paint mPaint;
	private double Direction=0;
	private boolean DirectionSet=false;
	public ArrowDirectionView(Context context)
	{
		super(context);
		// TODO Auto-generated constructor stub
		initDirectionView();
	}
	public ArrowDirectionView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initDirectionView();
		// TODO Auto-generated constructor stub
	}
	private void initDirectionView()
	{
		mPaint = new Paint();
		mPaint.setColor(0xFF00FF00);
		mPaint.setStrokeWidth(3);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if (!DirectionSet)
			return;
		if (this.getVisibility()!=View.VISIBLE)
			return;
		// Do drawing
		int width=this.getWidth();
		int height=this.getHeight();
		int minimum=width<height?width:height;
		
		double Angle=(Direction)/360*2*Math.PI;
		int x0=0,x1=0,y0=-minimum/2,y1=minimum/2;
		
		int x2=(int) (x0*Math.cos(Angle)-y0*Math.sin(Angle));
		int y2=(int) (x0*Math.sin(Angle)+y0*Math.cos(Angle));
		int x3=(int) (x1*Math.cos(Angle)-y1*Math.sin(Angle));
		int y3=(int) (x1*Math.sin(Angle)+y1*Math.cos(Angle));
		
		int x4=x0-(minimum/10+1);
		int y4=y0+(minimum/10+1);
		int x5=x0+(minimum/10+1);
		int y5=y0+(minimum/10+1);
		
		int x6=(int) (x4*Math.cos(Angle)-y4*Math.sin(Angle));
		int y6=(int) (x4*Math.sin(Angle)+y4*Math.cos(Angle));
		int x7=(int) (x5*Math.cos(Angle)-y5*Math.sin(Angle));
		int y7=(int) (x5*Math.sin(Angle)+y5*Math.cos(Angle));
	     
		
		canvas.drawLine  (width/2+x2, height/2+y2, width/2+x3, height/2+y3, mPaint);
		canvas.drawLine  (width/2+x6, height/2+y6, width/2+x2, height/2+y2, mPaint);
		canvas.drawLine  (width/2+x7, height/2+y7, width/2+x2, height/2+y2, mPaint);
		
		//canvas.drawCircle(width/2+x3, height/2+y3, 3, mPaint);
		// Draw text and what you want here
		// ...
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyDown(keyCode, event);
	}
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return super.onKeyUp(keyCode, event);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
	
	public void setDirection(double dir)
	{
		Direction=dir;	
		DirectionSet=true;
		this.invalidate();
	}
}
