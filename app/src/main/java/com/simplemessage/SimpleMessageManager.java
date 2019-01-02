package com.simplemessage;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

final class SimpleMessageManager {

	private static final SimpleMessageManager INSTANCE = new SimpleMessageManager();

	private final Handler handler = new Handler(Looper.getMainLooper());

	private Message currentMessage;

	private SimpleMessageManager() {
	}

	@NonNull
	public static SimpleMessageManager getInstance() {
		return INSTANCE;
	}

	@UiThread
	public void show(@NonNull Message message, int duration) {
		// There is no message showed
		if (currentMessage == null) {
			showInternal(message, duration);

		} else {
			currentMessage.getCallback().dismiss();
			handler.postDelayed(() -> showInternal(message, duration), 300);
		}
	}

	private void showInternal(@NonNull Message message, int duration) {
		currentMessage = message;
		Callback callback = currentMessage.getCallback();
		callback.show();
		if (duration > 0) {
			handler.removeCallbacksAndMessages(null);
			handler.postDelayed(() -> {
				//noinspection ConstantConditions
				if (callback != null) {
					callback.dismiss();
				}
			}, duration);
		}
	}

	public interface Callback {
		void show();

		void dismiss();
	}
}
