package com.simplemessage;

import android.support.annotation.ColorInt;

public enum MessageType {

	NORMAL(0xFFFFFFFF, 0, true), // StatusBar color applied
	SUCCESS(0xFFFFFFFF, 0xFF8bc34a, false), ERROR(0xFFFFFFFF, 0xFFD83C0D, false);

	@ColorInt private final int textColor;
	@ColorInt private final int backgroundColor;
	private final boolean progress;

	MessageType(int textColor, int backgroundColor, boolean progress) {
		this.textColor = textColor;
		this.backgroundColor = backgroundColor;
		this.progress = progress;
	}

	public boolean isProgress() {
		return progress;
	}

	public int getTextColor() {
		return textColor;
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}
}