package ru.starksoft.simplemessage

private const val TEXT_COLOR_WHITE = -0x1

data class MessageRecord(
	var message: CharSequence = "",

	var textColor: Int = TEXT_COLOR_WHITE,

	var backgroundColor: Int = 0,

	var progress: Boolean = false,

	var isPersistent: Boolean = false
) {
	companion object {

		@JvmStatic
		fun createNormal(message: CharSequence): MessageRecord {
			return MessageRecord(message, TEXT_COLOR_WHITE, 0, false, false)
		}

		@JvmStatic
		fun createNormalWithProgress(message: CharSequence): MessageRecord {
			return MessageRecord(message, TEXT_COLOR_WHITE, 0, true, false)
		}

		@JvmStatic
		fun createSuccess(message: CharSequence): MessageRecord {
			return MessageRecord(message, TEXT_COLOR_WHITE, -0xbc5fb9, false, false)
		}

		@JvmStatic
		fun createError(message: CharSequence): MessageRecord {
			return MessageRecord(message, TEXT_COLOR_WHITE, -0x27c3f3, false, false)
		}

		@JvmStatic
		fun createErrorPersistent(message: CharSequence): MessageRecord {
			return MessageRecord(message, TEXT_COLOR_WHITE, -0x27c3f3, false, true)
		}
	}
}