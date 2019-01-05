package ru.starksoft.simplemessage;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;

public interface Message {

	@NonNull
	SimpleMessageManager.Callback getCallback();

	@NonNull
	MessageRecord getMessageRecord();

	void setOnDismissListener(@Nullable OnDismissListener onDismissListener);

	@UiThread
	void show();

	@UiThread
	void hide();

	void hideSystemUi();

	void showSystemUi();

	boolean isDismissing();

	interface OnDismissListener {

		void onDismissed();
	}
}