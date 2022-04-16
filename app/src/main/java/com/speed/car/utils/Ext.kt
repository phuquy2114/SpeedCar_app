package com.speed.car.utils

import android.view.View
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.*

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

fun Date?.toTime(dateFormat: String): String {
    return try {
        SimpleDateFormat(dateFormat, Locale.getDefault()).format(this)
    } catch (ex: Exception) {
        println(ex.message)
        ""
    }
}
fun <T> QuerySnapshot?.toListOrEmpty(clazz: Class<T>, invoke: T.(DocumentSnapshot) -> Unit): List<T> {
    return this?.documents?.mapNotNull { doc ->
        doc.toObject(clazz)?.apply { invoke(doc) }
    } ?: listOf()
}