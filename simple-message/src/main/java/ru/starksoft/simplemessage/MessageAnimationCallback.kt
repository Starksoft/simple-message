package ru.starksoft.simplemessage

interface MessageAnimationCallback {

	fun onShowAnimationStart()

	fun onShowAnimationEnd()

	fun onHideAnimationStart()

	fun onHideAnimationEnd()
}