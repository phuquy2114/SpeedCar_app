package com.speed.car.utils

import android.view.View

fun Any?.isNotNull(): Boolean = this != null
fun Any?.isNull(): Boolean = this == null

internal fun View.show() {
    this.visibility = View.VISIBLE
}

internal fun View.hide() {
    this.visibility = View.INVISIBLE
}

internal fun View.gone() {
    this.visibility = View.GONE
}

internal fun View.showOrGone(isGone: Boolean) {
    if (!isGone) show() else gone()
}
