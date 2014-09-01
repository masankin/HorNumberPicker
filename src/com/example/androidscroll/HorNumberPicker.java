package com.example.androidscroll;

import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.TextView;

public class HorNumberPicker extends LinearLayout {
	private Scroller mScroller;
	private VelocityTracker mVelocityTracker;
	private int mMaximumFlingVelocity, mMinimumFlingVelocity;
	private float lastX, originX, minimumSlop;
	private int touchSlop = 50;
	private int[] values;
	private TextView textA, textB, textC, textD, textE;
	
	private final int ANIMATION_DURATION = 100;
	private int childCount = 5;//default
	
	private enum Direction{LEFT, RIGHT, MIDDLE}

	/**
	 * The currentPosition while the finger performs ACTION_DOWN
	 * */
	private int originPosition;

	/**
	 * The current index of the current value
	 * */
	private int currentPosition;

	public interface OnValueChangedListener {
		public void valueChanged(int oldValue, int newValue);
	}

	private OnValueChangedListener valueChangeListener;

	public HorNumberPicker(Context context) {
		super(context);
		this.init();
	}

	public HorNumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init();
	}

	private void init() {
		this.inflate(getContext(), R.layout.numberpicker_layout, this);
		mScroller = new Scroller(this.getContext(),
				new DecelerateInterpolator());
		ViewConfiguration configuration = ViewConfiguration.get(getContext());
		mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity() * 2;
		mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity() / 8;
		minimumSlop = 5;

		textA = (TextView) findViewById(R.id.textA);
		textB = (TextView) findViewById(R.id.textB);
		textC = (TextView) findViewById(R.id.textC);
		textD = (TextView) findViewById(R.id.textD);
		textE = (TextView) findViewById(R.id.textE);
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			this.scrollValue(mScroller.getStartX() - mScroller.getCurrX());
			invalidate();
		} else {
			originPosition = currentPosition;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		obtainVelocityTracker(event);
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mScroller.forceFinished(true);
			lastX = event.getX();
			originX = event.getX();
			originPosition = currentPosition;
			break;
		case MotionEvent.ACTION_MOVE:
			if (Math.abs(lastX - event.getX()) > minimumSlop) {
				scrollValue(originX - event.getX());
				lastX = event.getX();
			}
			break;
		case MotionEvent.ACTION_UP:
			originPosition = currentPosition;
			final VelocityTracker velocityTracker = mVelocityTracker;
			velocityTracker.computeCurrentVelocity(1000, mMaximumFlingVelocity);
			if (Math.abs(velocityTracker.getXVelocity()) > mMinimumFlingVelocity) {
				mScroller.fling(0, 0, (int) velocityTracker.getXVelocity(), 0,
						Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
				invalidate();
			}
			releaseVelocityTracker();
			break;
		}
		return true;
	}

	/**
	 * Calculate the current value according to the moving distance
	 * 
	 * @param distance
	 *            the distance that move
	 * */
	private void scrollValue(float distance) {
		boolean add = distance > 0;

		distance = Math.abs(distance);
		if (distance < touchSlop / 2)
			return;

		int valueChange = 1;
		distance = distance - touchSlop / 2;
		while (distance > 0) {
			distance -= touchSlop;
			valueChange++;
		}

		int oldValue = this.getSelectedValue();

		if (add) {
			currentPosition = originPosition + valueChange;
		} else
			currentPosition = originPosition - valueChange;

		this.setCurrentPosition(currentPosition);

		if (this.getSelectedValue() != oldValue && valueChangeListener != null) {
			valueChangeListener.valueChanged(oldValue, this.getSelectedValue());
		}
	}

	public void setValueChangeListener(
			OnValueChangedListener valueChangeListener) {
		this.valueChangeListener = valueChangeListener;
	}

	private void obtainVelocityTracker(MotionEvent event) {
		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);
	}

	private void releaseVelocityTracker() {
		if (mVelocityTracker != null) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	/**
	 * Set the distance(px) that dertermines how long the finger moves will
	 * cause the value to change
	 * 
	 * @param touchSlop
	 *            the distance that causes the change
	 * */
	public void setTouchSlop(int touchSlop) {
		this.touchSlop = touchSlop;
	}

	/**
	 * Set the set of the values
	 * */
	public void setValues(int[] values) {
		this.values = values;
		this.currentPosition = 0;
		invalidateValues(Direction.MIDDLE);
	}
	
	public void setChildCount(int childCount){
		if(childCount % 2 != 1)
			throw new IllegalArgumentException("The child count must be odd");
			
		this.childCount = childCount;
	}

	/**
	 * Get the current selected value
	 * */
	public int getSelectedValue() {
		if (values == null || values.length == 0)
			throw new IllegalArgumentException("The values is empty.");
		return values[currentPosition];
	}

	/**
	 * Set the index of the current value
	 * 
	 * @param positon
	 *            the index of the value(no limit)
	 * */
	public void setCurrentPosition(int position) {
		int oldPosition = currentPosition;
		if (values == null || values.length == 0) {
			this.currentPosition = 0;
		} else {
			if (position < 0)
				position = 0;

			if (position >= values.length)
				position = values.length - 1;

			this.currentPosition = position;
		}
		
		if(oldPosition == currentPosition)
			return;
		
		if(currentPosition > oldPosition)
			invalidateValues(Direction.LEFT);
		else
			invalidateValues(Direction.RIGHT);
	}

	/**
	 * When the values is changed or the currentPosition is changed, this method
	 * should be called in order to invalidate the current value
	 * */
	private void invalidateValues(Direction direction) {
		if (values == null || values.length == 0 || textC == null) {
			return;
		}

		textC.setText(String.valueOf(values[currentPosition]));

		if (currentPosition - 1 >= 0) {
			textB.setText(String.valueOf(values[currentPosition - 1]));
			if (currentPosition - 2 >= 0) {
				textA.setText(String.valueOf(values[currentPosition - 2]));
			} else {
				textA.setText("");
			}
		} else {
			textB.setText("");
			textA.setText("");
		}

		if (currentPosition + 1 < values.length) {
			textD.setText(String.valueOf(values[currentPosition + 1]));
			if (currentPosition + 2 < values.length) {
				textE.setText(String.valueOf(values[currentPosition + 2]));
			} else {
				textE.setText("");
			}
		} else {
			textD.setText("");
			textE.setText("");
		}
	}
	
	/**
	 * Play the animation while TextView content changes.
	 * */
	private void playView(View fromView, View toView){
		AnimatorSet set = new AnimatorSet();
		ObjectAnimator animTranslate = ObjectAnimator
				.ofFloat(fromView, "x", fromView.getX(), toView.getX()).setDuration(100);
		ObjectAnimator animAlpha = ObjectAnimator
				.ofFloat(fromView, "alpha", 1f, 0.1f).setDuration(100);
		ObjectAnimator animScaleWidth = ObjectAnimator
				.ofFloat(fromView, "width", 1f, (float)toView.getMeasuredWidth() / (float)fromView.getMeasuredWidth()).setDuration(100);
		ObjectAnimator animScaleHeight = ObjectAnimator
				.ofFloat(fromView, "height", 1f, (float)toView.getMeasuredWidth() / (float)fromView.getMeasuredWidth()).setDuration(100);
		set.playTogether(animTranslate, animAlpha, animScaleWidth, animScaleHeight);
		set.start();
	}
}
