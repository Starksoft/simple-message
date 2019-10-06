package ru.starksoft.simplemessage

private const val TEXT_COLOR_WHITE = -0x1

data class MessageData(
	var message: CharSequence = "",

	var textColor: Int = TEXT_COLOR_WHITE,

	var backgroundColor: Int = 0,

	var isProgress: Boolean = false,

	var isPersistent: Boolean = false,

	var messageType: MessageType = MessageType.STATUS_BAR
) {
	companion object {

		@JvmStatic
		fun createNormal(message: CharSequence): MessageData {
			return MessageData(message, TEXT_COLOR_WHITE, 0, false, false)
		}

		@JvmStatic
		fun createNormalWithProgress(message: CharSequence): MessageData {
			return MessageData(message, TEXT_COLOR_WHITE, 0, true, false)
		}

		@JvmStatic
		fun createSuccess(message: CharSequence): MessageData {
			return MessageData(message, TEXT_COLOR_WHITE, -0xbc5fb9, false, false)
		}

		@JvmStatic
		fun createError(message: CharSequence): MessageData {
			return MessageData(message, TEXT_COLOR_WHITE, -0x27c3f3, false, false)
		}

		@JvmStatic
		fun createErrorPersistent(message: CharSequence): MessageData {
			return MessageData(message, TEXT_COLOR_WHITE, -0x27c3f3, false, true)
		}
	}
}
