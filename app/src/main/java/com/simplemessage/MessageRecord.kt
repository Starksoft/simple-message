package com.simplemessage

data class MessageRecord(
		val message: CharSequence,

		val textColor: Int,

		val backgroundColor: Int,

		val progress: Boolean
) {
	companion object {

//		@JvmStatic
//		fun createNormal():MessageRecord {
//			return MessageRecord();
//		}
	}
}