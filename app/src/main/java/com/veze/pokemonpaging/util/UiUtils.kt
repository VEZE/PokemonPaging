package com.veze.pokemonpaging.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast

fun Context.showToast(message: String) {
    Toast.makeText(
        this,
        message,
        Toast.LENGTH_LONG
    ).show()
}


fun ViewGroup.inflateView(layoutId: Int): View {
    return LayoutInflater.from(this.context).inflate(layoutId, this, false)
}

