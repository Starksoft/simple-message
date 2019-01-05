package ru.starksoft.simplemessage;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.util.Log;

import static ru.starksoft.simplemessage.util.Util.checkNotNull;

@SuppressWarnings("WeakerAccess")
final class SimpleMessageManager {

	private static final String TAG = "SimpleMessageManager";

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
			int messageDelay = 0;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				//messageDelay = 750;
			}
			showInternal(message, duration, messageDelay);

		} else {
			// TODO: 03/01/2019 Решить, нужно ли разрешать дубликаты сообщений
			MessageRecord currentMessageRecord = currentMessage.getMessageRecord();
			MessageRecord newMessageRecord = message.getMessageRecord();

			if (newMessageRecord.equals(currentMessageRecord)) {
				Log.d(TAG, "show: trying to show the same message.. skipping");

			} else {
				if (!currentMessage.isDismissing()) {
					handler.removeCallbacksAndMessages(null);
					currentMessage.setOnDismissListener(() -> {
						currentMessage.setOnDismissListener(null);
						showInternal(message, duration, 0);
					});
					currentMessage.getCallback().dismiss();
				} else {
					Log.d(TAG, "show: we are in dismissing state");
				}
			}
		}
	}

	public void hide(@NonNull Message message) {
		message.getCallback().dismiss();
		message.showSystemUi();
	}

	private void showInternal(@NonNull Message message, int duration, int messageDelay) {
		currentMessage = checkNotNull(message);
		currentMessage.hideSystemUi();

		//		handler.postDelayed(() -> {
		//			Callback callback = currentMessage.getCallback();
		//			callback.show();
		//			if (duration > 0) {
		//				handler.removeCallbacksAndMessages(null);
		//				handler.postDelayed(() -> {
		//					//noinspection ConstantConditions
		//					if (callback != null) {
		//						callback.dismiss();
		//						currentMessage.showSystemUi();
		//						currentMessage = null;
		//					}
		//				}, duration);
		//			}
		//		}, messageDelay);

		Callback callback = currentMessage.getCallback();
		callback.show();
		if (duration > 0) {
			handler.removeCallbacksAndMessages(null);
			handler.postDelayed(() -> {
				//noinspection ConstantConditions
				if (callback != null) {
					callback.dismiss();
					currentMessage.showSystemUi();
					currentMessage = null;
				}
			}, duration);
		}
	}

	public void destroy() {
		handler.removeCallbacksAndMessages(null);
		currentMessage = null;
	}

	public interface Callback {

		void show();

		void dismiss();
	}
}
