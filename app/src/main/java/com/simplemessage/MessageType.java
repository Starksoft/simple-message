package com.simplemessage;

import android.support.annotation.ColorInt;

public enum MessageType {

	NORMAL(0xFFFFFFFF, 0), // StatusBar color applied
	SUCCESS(0xFFFFFFFF, 0xFF8bc34a), ERROR(0xFFFFFFFF, 0xFFD83C0D);

	@ColorInt private final int textColor;
	@ColorInt private final int backgroundColor;

	MessageType(int textColor, int backgroundColor) {
		this.textColor = textColor;
		this.backgroundColor = backgroundColor;
	}

	public int getTextColor() {
		return textColor;
	}

	public int getBackgroundColor() {
		return backgroundColor;
	}
}