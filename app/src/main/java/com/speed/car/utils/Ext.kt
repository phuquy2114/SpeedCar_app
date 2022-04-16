package com.speed.car.utils

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.fragment.app.Fragment

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

fun Boolean?.orFalse() = this ?: false

@BindingAdapter("visible_or_gone")
fun View.setVisibleOrGone(isVisible: Boolean?) {
    visibility = if (isVisible.orFalse()) View.VISIBLE else View.GONE
}