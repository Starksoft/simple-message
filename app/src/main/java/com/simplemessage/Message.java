package com.simplemessage;

import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

public interface Message {

	@NonNull
	SimpleMessageManager.Callback getCallback();

	@UiThread
	void show();

	void hide();
}
