package ru.starksoft.simplemessage;

import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import static ru.starksoft.simplemessage.util.Util.checkNotNull;

@SuppressWarnings("WeakerAccess")
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
	public static Builder create(@NonNull Activity activity, @NonNull CharSequence message) {
		return new Builder(activity, message, MessageTemplate.NONE);
	}

	@NonNull
	public static Builder create(@NonNull Activity activity, @NonNull CharSequence message, @MessageTemplate int template) {
		return new Builder(activity, message, template);
	}

	private void observeLifecycle() {
		if (activity instanceof AppCompatActivity) {
			((AppCompatActivity) activity).getLifecycle().addObserver(new LifecycleObserver() {

				@OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
				void destroy() {
					SimpleMessageManager.getInstance().destroy();
					Log.d(TAG, "destroy() called");
				}
			});
		}
	}

	public void show() {
		view.show();
	}

	public void hide() {
		view.hide();
	}

	public static final class Builder {

		private final MessageRecord messageRecord;
		@NonNull private final Activity activity;

		public Builder(@NonNull Activity activity, @NonNull CharSequence message, @MessageTemplate int template) {
			this.activity = activity;
			messageRecord = getMessageRecordByTemplate(message, template);
			messageRecord.setMessage(message);
		}

		@NonNull
		private MessageRecord getMessageRecordByTemplate(@NonNull CharSequence message, @MessageTemplate int template) {
			switch (template) {
				case MessageTemplate.ERROR_PERSISTENT:
					return MessageRecord.createErrorPersistent(message);

				case MessageTemplate.ERROR:
					return MessageRecord.createError(message);

				case MessageTemplate.NORMAL_PROGRESS:
					return MessageRecord.createNormalWithProgress(message);

				case MessageTemplate.NORMAL:
					return MessageRecord.createNormal(message);

				case MessageTemplate.SUCCESS:
					return MessageRecord.createSuccess(message);

				case MessageTemplate.NONE:
				default:
					return new MessageRecord();
			}
		}

		@NotNull
		public Builder textColor(@ColorInt int color) {
			messageRecord.setTextColor(color);
			return this;
		}

		@NotNull
		public Builder backgroundColor(@ColorInt int color) {
			messageRecord.setBackgroundColor(color);
			return this;
		}

		@NotNull
		public Builder progress(boolean progress) {
			messageRecord.setProgress(progress);
			return this;
		}

		@NotNull
		public Builder persistent(boolean persistent) {
			messageRecord.setPersistent(persistent);
			return this;
		}

		@NonNull
		public SimpleMessage build() {
			return new SimpleMessage(activity, messageRecord);
		}

		public void show() {
			build().show();
		}
	}
}