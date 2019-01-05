package ru.starksoft.simplemessage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
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
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.simplemessage.R;

import ru.starksoft.simplemessage.util.Util;

@SuppressLint("ViewConstructor")
final class StatusBarMessageLayout extends LinearLayout implements Message {

	public static final int EVENT_SHOW = 0;
	public static final int EVENT_HIDE = 1;
	public static final int PADDING_LEFT_RIGHT = Util.dpToPx(8);
	private static final String TAG = "StatusBarMessageLayout";
	private final MessageRecord messageRecord;
	private final AccessibilityManager accessibilityManager;
	@Nullable private OnDismissListener onDismissListener;
	@Nullable private OnLayoutChangeListener onLayoutChangeListener;
	private TextView messageTextView;
	private ProgressBar progressBar;
	private boolean isDismissing;
	private final Handler handler = new Handler(Looper.getMainLooper(), message -> {
		switch (message.what) {
			case 0:
				((StatusBarMessageLayout) message.obj).showView();
				return true;
			case 1:
				((StatusBarMessageLayout) message.obj).hideView();
				return true;
			default:
				return false;
		}
	});
	private final SimpleMessageManager.Callback managerCallback = new SimpleMessageManager.Callback() {
		public void show() {
			handler.sendMessage(handler.obtainMessage(EVENT_SHOW, StatusBarMessageLayout.this));
		}

		public void dismiss() {
			handler.sendMessage(handler.obtainMessage(EVENT_HIDE, StatusBarMessageLayout.this));
		}
	};

	public StatusBarMessageLayout(@NonNull Context context, @NonNull MessageRecord messageRecord) {
		super(context);
		this.messageRecord = messageRecord;
		accessibilityManager = ContextCompat.getSystemService(context, AccessibilityManager.class);
		init();
	}

	@Override
	public void setOnDismissListener(@Nullable OnDismissListener onDismissListener) {
		this.onDismissListener = onDismissListener;
	}

	@UiThread
	@Override
	public void show() {
		isDismissing = false;
		SimpleMessageManager.getInstance().show(this, messageRecord.isPersistent() ? 0 : 3000);
	}

	@Override
	public void hide() {
		isDismissing = true;
		SimpleMessageManager.getInstance().hide(this);
	}

	@Override
	public boolean isDismissing() {
		return isDismissing;
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
		Log.d(TAG, hashCode() + " showView() called");

		if (isDismissing) {
			throw new IllegalStateException();
		}

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

//		hideSystemUi();
	}

	private void hideView() {
		isDismissing = true;
		Log.d(TAG, hashCode() + " hideView() called");

		animateViewOut();
	}

	@Override
	public void hideSystemUi() {
		View decorView = getDecorView().getRootView();

		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		decorView.setOnSystemUiVisibilityChangeListener((visibility) -> setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE));
	}

	@Override
	public void showSystemUi() {
		View decorView = getDecorView();
		getDecorView().setSystemUiVisibility(0);
		decorView.setOnSystemUiVisibilityChangeListener(null);
	}

	public void setOnLayoutChangeListener(@Nullable OnLayoutChangeListener onLayoutChangeListener) {
		this.onLayoutChangeListener = onLayoutChangeListener;
	}

	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (onLayoutChangeListener != null) {
			onLayoutChangeListener.onLayoutChange(this, l, t, r, b);
		}
	}

	private void animateViewIn() {
		if (isDismissing) {
			throw new IllegalStateException();
		}

		final int translationYBottom = -getTranslationYTop();

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
				onViewShown();
			}
		});
		animator.addUpdateListener(a -> {
			int currentAnimatedIntValue = (Integer) a.getAnimatedValue();
			setTranslationY((float) currentAnimatedIntValue);
		});
		animator.start();
	}

	private void animateViewOut() {
		if (!isDismissing) {
			throw new IllegalStateException();
		}
		final int translationYBottom = -getTranslationYTop();

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
		Log.d(TAG, hashCode() + " onViewShown() called");
	}

	private void onViewHidden() {
		if (!isDismissing) {
			throw new IllegalStateException();
		}

		ViewParent parent = this.getParent();
		if (parent instanceof ViewGroup) {
			((ViewGroup) parent).removeView(this);
		}

//		showSystemUi();

		Log.d(TAG, hashCode() + " onViewHidden() called");

		isDismissing = false;

		if (onDismissListener != null) {
			onDismissListener.onDismissed();
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

	private int getTranslationYTop() {
		int translationY = this.getHeight();
		ViewGroup.LayoutParams layoutParams = this.getLayoutParams();
		if (layoutParams instanceof MarginLayoutParams) {
			translationY += ((MarginLayoutParams) layoutParams).bottomMargin;
		}

		return translationY;
	}

	private boolean shouldAnimate() {
		return true;
		//List<AccessibilityServiceInfo> serviceList = accessibilityManager.getEnabledAccessibilityServiceList(1);
		//return serviceList != null && serviceList.isEmpty();
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
		//ViewCompat.setFitsSystemWindows(this, true);

		LayoutInflater.from(getContext()).inflate(R.layout.message_statusbar, this);

		setGravity(Gravity.CENTER);
		setPadding(PADDING_LEFT_RIGHT, 0, PADDING_LEFT_RIGHT, 0);

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

			boolean progress = messageRecord.getProgress();
			Util.setVisibility(progressBar, progress);
			if (progress) {
				progressBar.getIndeterminateDrawable().setColorFilter(textColor, PorterDuff.Mode.SRC_IN);
			}
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

	@NonNull
	@Override
	public MessageRecord getMessageRecord() {
		return messageRecord;
	}

	interface OnLayoutChangeListener {
		void onLayoutChange(@NonNull View view, int l, int t, int r, int b);
	}
}
