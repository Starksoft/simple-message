package com.simplemessage;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
@IntDef({
		MessageTemplate.NONE,
		MessageTemplate.NORMAL,
		MessageTemplate.NORMAL_PROGRESS,
		MessageTemplate.SUCCESS,
		MessageTemplate.ERROR,
		MessageTemplate.ERROR_PERSISTENT})
public @interface MessageTemplate {
	int NONE = 0;
	int NORMAL = 1;
	int NORMAL_PROGRESS = 2;
	int SUCCESS = 3;
	int ERROR = 4;
	int ERROR_PERSISTENT = 5;
}