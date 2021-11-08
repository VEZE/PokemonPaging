package com.veze.pokemonpaging.ui.main

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.veze.pokemonpaging.R
import com.veze.pokemonpaging.databinding.LoadingBinding
import com.veze.pokemonpaging.util.inflateView

/**
 *  Adapter presents LoadingView at the end of list with states:
 *
 *  [LoadState.Loading] - Showing progress
 *  [LoadState.Done] - Removing Progress
 *  [LoadState.Error] - Show view with retry button and exception
 *
 */
class LoadingAdapter(private val retryAction: () -> Unit) :
    RecyclerView.Adapter<FooterViewHolder>() {

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FooterViewHolder {
        val binding = LoadingBinding.bind(parent.inflateView(R.layout.loading))
        return FooterViewHolder(binding, retryAction)
    }

    override fun onBindViewHolder(holder: FooterViewHolder, position: Int) {
        holder.bind(loadState)

    }

    override fun getItemCount(): Int =
        if (loadState is LoadState.Loading || loadState is LoadState.Error) 1 else 0

}


class FooterViewHolder(private val binding: LoadingBinding, retry: () -> Unit) :
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

}


sealed class LoadState {
    object Loading : LoadState()
    data class Error(val error: Throwable) : LoadState()
    object Done : LoadState()
}
