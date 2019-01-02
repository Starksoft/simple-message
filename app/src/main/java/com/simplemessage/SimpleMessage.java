package com.simplemessage;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public final class SimpleMessage {

	private static final String TAG = "SimpleMessage";

	@NonNull private final Activity activity;

	private final StatusBarMessageLayout view;

	private SimpleMessage(@NonNull Activity activity, @NonNull MessageRecord messageRecord) {
		this.activity = checkNotNull(activity);

		view = new StatusBarMessageLayout(activity, messageRecord);

		observeLifecycle();
	}

	@NonNull
	private static <T> T checkNotNull(T reference) {
		if (reference == null) {
			throw new NullPointerException();
		} else {
			return reference;
		}
	}

	@NonNull
	public static SimpleMessage create(@NonNull Activity activity, @NonNull String message) {
		return new SimpleMessage(activity, new MessageRecord(message, 0xFFFFFFFF, 0xFF009000));
	}

	private void observeLifecycle() {
		if (activity instanceof AppCompatActivity) {
			((AppCompatActivity) activity).getLifecycle().addObserver(new LifecycleObserver() {

				@OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
				void destroy() {
					Log.d(TAG, "destroy() called");
				}
			});
		}
	}

	public void show() {
		view.show();
	}

}
