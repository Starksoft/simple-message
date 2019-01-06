package ru.starksoft.simplemessage;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.design.animation.AnimationUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

public abstract class BaseMessageLayout extends LinearLayout implements Message {

	public static final int EVENT_SHOW = 0;
	public static final int EVENT_HIDE = 1;
	public static final long VIEW_ANIMATION_DURATION = 250L;
	public static final long CONTENT_ANIMATION_DURATION = 300L;
	private static final String TAG = "BaseMessageLayout";
	@NonNull protected final MessageData messageData;
	private final AccessibilityManager accessibilityManager;
	private final MessageAnimationCallback messageAnimationCallback = createAnimationCallback();
	private final SimpleMessageManager.Callback managerCallback = new SimpleMessageManager.Callback() {
		public void show() {
			handler.sendMessage(handler.obtainMessage(EVENT_SHOW, BaseMessageLayout.this));
		}

		public void dismiss() {
			handler.sendMessage(handler.obtainMessage(EVENT_HIDE, BaseMessageLayout.this));
		}
	};
	@Nullable private Message.OnDismissListener onDismissListener;
	@Nullable private OnLayoutChangeListener onLayoutChangeListener;
	private boolean isDismissing;
	private final Handler handler = new Handler(Looper.getMainLooper(), message -> {
		switch (message.what) {
			case 0:
				((BaseMessageLayout) message.obj).showView();
				return true;
			case 1:
				((BaseMessageLayout) message.obj).hideView();
				return true;
			default:
				return false;
		}
	});

	public BaseMessageLayout(@NonNull Context context, @NonNull MessageData messageData) {
		super(context);
		this.messageData = messageData;
		accessibilityManager = ContextCompat.getSystemService(context, AccessibilityManager.class);
		createView();
	}

	protected abstract void createView();

	@NonNull
	protected abstract MessageAnimationCallback createAnimationCallback();

	@Override
	public void setOnDismissListener(@Nullable Message.OnDismissListener onDismissListener) {
		this.onDismissListener = onDismissListener;
	}

	@UiThread
	@Override
	public void show() {
		isDismissing = false;
		// TODO: 06/01/2019 Move delay to manager
		SimpleMessageManager.getInstance().show(this, messageData.isPersistent() ? 0 : 3000);
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
	protected Window getWindow() {
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
				messageAnimationCallback.onShowAnimationEnd();
			}
		} else {
			setOnLayoutChangeListener((view, left, top, right, bottom) -> {
				setOnLayoutChangeListener(null);
				if (shouldAnimate()) {
					animateViewIn();
				} else {
					messageAnimationCallback.onShowAnimationEnd();
				}
			});
		}
	}

	private void hideView() {
		isDismissing = true;
		Log.d(TAG, hashCode() + " hideView() called");

		animateViewOut();
	}

	@Override
	@CallSuper
	public void hideSystemUi() {
		View decorView = getDecorView().getRootView();

		decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		decorView.setOnSystemUiVisibilityChangeListener((visibility) -> setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE));
	}

	@Override
	@CallSuper
	public void showSystemUi() {
		View decorView = getDecorView();
		decorView.setSystemUiVisibility(0);
		decorView.setOnSystemUiVisibilityChangeListener(null);
	}

	void setOnLayoutChangeListener(@Nullable OnLayoutChangeListener onLayoutChangeListener) {
		this.onLayoutChangeListener = onLayoutChangeListener;
	}

	@Override
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
		animator.setDuration(VIEW_ANIMATION_DURATION);
		animator.addListener(new AnimatorListenerAdapter() {
			public void onAnimationStart(Animator animator) {
				messageAnimationCallback.onShowAnimationStart();
			}

			public void onAnimationEnd(Animator animator) {
				messageAnimationCallback.onShowAnimationEnd();
			}
		});
		animator.addUpdateListener(a -> setTranslationY((float) (Integer) a.getAnimatedValue()));
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
		animator.setDuration(VIEW_ANIMATION_DURATION);
		animator.addListener(new AnimatorListenerAdapter() {
			public void onAnimationStart(Animator animator) {
				messageAnimationCallback.onHideAnimationStart();
			}

			public void onAnimationEnd(Animator animator) {
				messageAnimationCallback.onHideAnimationEnd();
				onViewHidden();
			}
		});
		animator.addUpdateListener(a -> setTranslationY((float) (Integer) a.getAnimatedValue()));
		animator.start();
	}

	private void onViewHidden() {
		if (!isDismissing) {
			throw new IllegalStateException();
		}

		ViewParent parent = getParent();
		if (parent instanceof ViewGroup) {
			((ViewGroup) parent).removeView(this);
		}

		Log.d(TAG, hashCode() + " onViewHidden() called");

		isDismissing = false;

		if (onDismissListener != null) {
			onDismissListener.onDismissed();
		}
	}

	protected void animateContentIn(@Nullable View... views) {
		if (views == null) {
			return;
		}

		for (View view : views) {
			view.setAlpha(0.0F);
			view.animate().alpha(1.0F).setDuration(CONTENT_ANIMATION_DURATION).setStartDelay((long) 100).start();
		}
	}

	protected void animateContentOut(@Nullable View... views) {
		if (views == null) {
			return;
		}

		for (View view : views) {
			view.setAlpha(1.0F);
			view.animate().alpha(0.0F).setDuration(CONTENT_ANIMATION_DURATION).start();
		}
	}

	private int getTranslationYTop() {
		int translationY = getHeight();
		//		ViewGroup.LayoutParams layoutParams = getLayoutParams();
		//		if (layoutParams instanceof MarginLayoutParams) {
		//			translationY += ((MarginLayoutParams) layoutParams).bottomMargin;
		//		}

		return translationY;
	}

	private boolean shouldAnimate() {
		return true;
		//List<AccessibilityServiceInfo> serviceList = accessibilityManager.getEnabledAccessibilityServiceList(1);
		//return serviceList != null && serviceList.isEmpty();
	}

	final int getStatusBarHeightInPixels() {
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
	public MessageData getMessageData() {
		return messageData;
	}

	interface OnLayoutChangeListener {
		void onLayoutChange(@NonNull View view, int l, int t, int r, int b);
	}
}