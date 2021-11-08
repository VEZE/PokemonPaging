package com.veze.pokemonpaging.ui.main

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.veze.pokemonpaging.R
import com.veze.pokemonpaging.databinding.LoadingBinding

class LoadingAdapter : RecyclerView.Adapter<FooterStateViewHolder>() {

    var loadState: LoadState = LoadState.Done
        set(loadState) {
            if (field != loadState) {

                when {
                    field is LoadState.Done -> notifyItemInserted(0)
                    loadState is LoadState.Done -> notifyItemRemoved(0)
                    else -> notifyItemChanged(0)
                }

                field = loadState
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FooterStateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.loading, parent, false)

        val binding = LoadingBinding.bind(view)

        return FooterStateViewHolder(binding) {}
    }

    override fun onBindViewHolder(holder: FooterStateViewHolder, position: Int) {
        holder.bind(loadState)

    }

    override fun getItemCount(): Int =
        if (loadState is LoadState.Loading || loadState is LoadState.Error) 1 else 0

}


class FooterStateViewHolder(private val binding: LoadingBinding, retry: () -> Unit) :
    RecyclerView.ViewHolder(binding.root) {

    init {
        binding.retryButton.apply {
            setOnClickListener { retry.invoke() }
        }
    }

    fun bind(loadState: LoadState) {
        if (loadState is LoadState.Error) {
            binding.retryText.text = loadState.error.message
        }

        binding.retry.isVisible = loadState is LoadState.Error
        binding.loading.isVisible = loadState is LoadState.Loading

    }

    companion object {
        fun create(parent: ViewGroup, retry: () -> Unit): FooterStateViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.loading, parent, false)

            val binding = LoadingBinding.bind(view)

            return FooterStateViewHolder(binding, retry)
        }
    }
}


sealed class LoadState {
    object Loading : LoadState()
    data class Error(val error: Throwable) : LoadState()
    object Done : LoadState()
}
