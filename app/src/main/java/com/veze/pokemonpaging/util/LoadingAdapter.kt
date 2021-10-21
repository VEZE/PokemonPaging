package com.veze.pokemonpaging.util

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.veze.pokemonpaging.R

class LoadingAdapter : RecyclerView.Adapter<LoadingAdapter.LoadingViewHolder>() {

    //TODO notifyItemChanged is too complex mb should use notifyItem Moved/Removed?
    var loadState: LoadState = LoadState.Done
        set(value) {
            when (field) {
                LoadState.Done -> notifyItemChanged(0)
                LoadState.Loading -> notifyItemChanged(0)
            }
            field = value
        }

    class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(loadState: LoadState) = with(itemView) {
            val progress = findViewById<TextView>(R.id.loading)
            progress.isVisible = LoadState.Loading == loadState
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadingViewHolder =
        LoadingViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.loading, parent, false)
        )

    override fun onBindViewHolder(holder: LoadingViewHolder, position: Int) {
        holder.bind(loadState)
    }

    override fun getItemCount(): Int = 1
}

sealed class LoadState {
    object Loading : LoadState()
    object Done : LoadState()
}