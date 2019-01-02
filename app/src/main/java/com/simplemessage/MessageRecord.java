package com.simplemessage;

import android.support.annotation.NonNull;

public final class MessageRecord {

	private final CharSequence message;
	private final int textColor;
	private final int backgroundColor;

	public MessageRecord(@NonNull CharSequence message, int textColor, int backgroundColor) {
		this.message = message;
		this.textColor = textColor;
		this.backgroundColor = backgroundColor;
	}

	public CharSequence getMessage() {
		return message;
	}

	public int getTextColor() {
		return textColor;
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		MessageRecord that = (MessageRecord) o;

		if (textColor != that.textColor) {
			return false;
		}
		if (backgroundColor != that.backgroundColor) {
			return false;
		}
		return message != null ? message.equals(that.message) : that.message == null;
	}

	@Override
	public int hashCode() {
		int result = message != null ? message.hashCode() : 0;
		result = 31 * result + textColor;
		result = 31 * result + backgroundColor;
		return result;
	}
}
