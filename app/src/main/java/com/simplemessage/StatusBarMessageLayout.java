package com.simplemessage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.animation.AnimationUtils;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public final class StatusBarMessageLayout extends LinearLayout implements Message {

	public static final int EVENT_SHOW = 0;
	public static final int EVENT_HIDE = 1;

	private static final String TAG = "BaseMessage";

	private static final Handler handler = new Handler(Looper.getMainLooper(), message -> {
		switch (message.what) {
			case 0:
				((StatusBarMessageLayout) message.obj).showView();
				return true;
			case 1:
				((StatusBarMessageLayout) message.obj).hideView(message.arg1);
				return true;
			default:
				return false;
		}
	});
	@NonNull final SimpleMessageManager.Callback managerCallback = new SimpleMessageManager.Callback() {
		public void show() {
			handler.sendMessage(handler.obtainMessage(EVENT_SHOW, StatusBarMessageLayout.this));
		}

		public void dismiss() {
			handler.sendMessage(handler.obtainMessage(EVENT_HIDE, StatusBarMessageLayout.this));
		}
	};
	@Nullable private final MessageRecord messageRecord;
	@Nullable SimpleMessageManager.Callback callback;
	private OnLayoutChangeListener onLayoutChangeListener;

	private TextView messageTextView;
	private ProgressBar progressBar;
	private boolean systemUiVisibilitySaved = false;
	private int systemUiVisibility;
	private boolean statusBarColorOriginalSaved = false;
	private int statusBarColorOriginal;

	public StatusBarMessageLayout(@NonNull Context context) {
		super(context);
		throw new UnsupportedOperationException();
	}

	public StatusBarMessageLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		throw new UnsupportedOperationException();
	}

	public StatusBarMessageLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		throw new UnsupportedOperationException();
	}

	public StatusBarMessageLayout(@NonNull Context context, @NonNull MessageRecord messageRecord) {
		super(context);
		this.messageRecord = messageRecord;
		init();
	}

	@UiThread
	@Override
	public void show() {
		SimpleMessageManager.getInstance().show(this, 3000);
	}

	private void hideView(int arg1) {
		Log.d(TAG, "hideView() called with: arg1 = [" + arg1 + "]");

		animateViewOut();
	}

	@NonNull
	private View getDecorView() {
		return getWindow().getDecorView();
	}

	@NonNull
	private Window getWindow() {
		return ((Activity) getContext()).getWindow();
	}

	private void showView() {
		Log.d(TAG, "showView() called");

		ViewGroup target = ((ViewGroup) getDecorView());

		ViewParent parent = getParent();
		if (parent == null) {
			FrameLayout.LayoutParams layoutParams =
					new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, getStatusBarHeightInPixels());

			target.addView(this, layoutParams);
		}

		if (ViewCompat.isLaidOut(this)) {
			if (shouldAnimate()) {
				animateViewIn();
			} else {
				onViewShown();
			}
		} else {
			setOnLayoutChangeListener((view, left, top, right, bottom) -> {
				setOnLayoutChangeListener(null);
				if (shouldAnimate()) {
					animateViewIn();
				} else {
					onViewShown();
				}
			});
		}

		View decorView = getDecorView().getRootView();

		if (!systemUiVisibilitySaved) {
			systemUiVisibility = decorView.getSystemUiVisibility();
			systemUiVisibilitySaved = true;
		}

		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		decorView.setOnSystemUiVisibilityChangeListener((visibility) -> {
			this.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		});

		//		hasOriginalStatusBarTranslucent = isTranslucentStatusBar()


	}

	@Override
	public void onDetachedFromWindow() {
		getDecorView().setOnSystemUiVisibilityChangeListener(null);
		super.onDetachedFromWindow();
	}

	public void setOnLayoutChangeListener(OnLayoutChangeListener onLayoutChangeListener) {
		this.onLayoutChangeListener = onLayoutChangeListener;
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (this.onLayoutChangeListener != null) {
			this.onLayoutChangeListener.onLayoutChange(this, l, t, r, b);
		}
	}

	private void animateViewIn() {
		final int translationYBottom = -getTranslationYBottom();

		setTranslationY((float) translationYBottom);

		ValueAnimator animator = new ValueAnimator();
		animator.setIntValues(translationYBottom, 0);
		animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
		animator.setDuration(250L);
		animator.addListener(new AnimatorListenerAdapter() {
			public void onAnimationStart(Animator animator) {
				animateContentIn(messageTextView, progressBar);
			}

			public void onAnimationEnd(Animator animator) {
				StatusBarMessageLayout.this.onViewShown();
			}
		});
		animator.addUpdateListener(a -> {
			int currentAnimatedIntValue = (Integer) a.getAnimatedValue();
			setTranslationY((float) currentAnimatedIntValue);
		});
		animator.start();
	}

	private void animateViewOut() {
		final int translationYBottom = -getTranslationYBottom();

		ValueAnimator animator = new ValueAnimator();
		animator.setIntValues(0, translationYBottom);
		animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
		animator.setDuration(250L);
		animator.addListener(new AnimatorListenerAdapter() {
			public void onAnimationStart(Animator animator) {
				animateContentOut(messageTextView, progressBar);
			}

			public void onAnimationEnd(Animator animator) {
				onViewHidden();
			}
		});
		animator.addUpdateListener(a -> {
			int currentAnimatedIntValue = (Integer) a.getAnimatedValue();
			setTranslationY((float) currentAnimatedIntValue);
		});
		animator.start();
	}

	private void onViewShown() {
		//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		//			Window window = getWindow();
		//
		//			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		//			if (!statusBarColorOriginalSaved) {
		//				statusBarColorOriginal = window.getStatusBarColor();
		//				statusBarColorOriginalSaved = true;
		//			}
		//			window.setStatusBarColor(statusBarColorOriginal);
		//		}


	}

	private void onViewHidden() {
		//SimpleMessageManager.getInstance().onDismissed(this.managerCallback);

		ViewParent parent = this.getParent();
		if (parent instanceof ViewGroup) {
			((ViewGroup) parent).removeView(this);
		}

		//		if (statusBarColorOriginalSaved) {
		//			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		//				Window window = getWindow();
		//				window.setStatusBarColor(statusBarColorOriginal);
		//			}
		//			statusBarColorOriginalSaved = false;
		//		}

		if (systemUiVisibilitySaved) {
			View decorView = getDecorView();
			decorView.setSystemUiVisibility(systemUiVisibility);
			systemUiVisibilitySaved = false;
		}
	}

	private void animateContentIn(@Nullable View... views) {
		if (views == null) {
			return;
		}

		for (View view : views) {
			view.setAlpha(0.0F);
			view.animate().alpha(1.0F).setDuration(300L).setStartDelay((long) 100).start();
		}
	}

	private void animateContentOut(@Nullable View... views) {
		if (views == null) {
			return;
		}

		for (View view : views) {
			view.setAlpha(1.0F);
			view.animate().alpha(0.0F).setDuration(300L).start();
		}
	}

	private int getTranslationYBottom() {
		int translationY = this.getHeight();
		ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
		if (layoutParams instanceof MarginLayoutParams) {
			translationY += ((MarginLayoutParams) layoutParams).bottomMargin;
		}

		return translationY;
	}

	private boolean shouldAnimate() {
		return true;
	}

	private void init() {
		//		ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
		//			Log.d(TAG, "setOnApplyWindowInsetsListener: insets=" + insets);
		//
		//			v.setPadding(v.getPaddingLeft(), insets.getSystemWindowInsetTop(), v.getPaddingRight(), v.getPaddingBottom());
		//			return insets;
		//		});

		//ViewCompat.setAccessibilityLiveRegion(this, 1);
		//ViewCompat.setImportantForAccessibility(this, 1);
		//		ViewCompat.setFitsSystemWindows(this, true);

		LayoutInflater.from(getContext()).inflate(R.layout.message_statusbar, this);

		setGravity(Gravity.CENTER);
		setPadding(Util.dpToPx(8), 0, Util.dpToPx(8), 0);

		messageTextView = findViewById(R.id.text);
		progressBar = findViewById(R.id.progress);
		
		if (messageRecord != null) {
			int backgroundColor = messageRecord.getBackgroundColor();
			if (backgroundColor != 0) {
				setBackgroundColor(backgroundColor);
			} else {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					setBackgroundColor(getWindow().getStatusBarColor());
				}
			}
			messageTextView.setText(messageRecord.getMessage());

			int textColor = messageRecord.getTextColor();
			messageTextView.setTextColor(textColor);
			progressBar.getIndeterminateDrawable().setColorFilter(textColor, PorterDuff.Mode.SRC_IN);
		}
	}

	public int getStatusBarHeightInPixels() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	@NonNull
	@Override
	public SimpleMessageManager.Callback getCallback() {
		return managerCallback;
	}

	interface OnLayoutChangeListener {
		void onLayoutChange(@NonNull View view, int l, int t, int r, int b);
	}
}
