package com.veze.pokemonpaging.util

import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.veze.pokemonpaging.ui.main.LoadState
import com.veze.pokemonpaging.ui.main.LoadingAdapter
import com.veze.pokemonpaging.ui.main.PokemonAdapter

class EndlessScrollListener(private val pagingListener: PagingListener) :
    RecyclerView.OnScrollListener() {

    private var itemHeight: Int = 1

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager


        val adapter = recyclerView.adapter as ConcatAdapter

        val loadingAdapter =
            (adapter.adapters.findLast { it is LoadingAdapter } ?: return) as LoadingAdapter

        val pokemonAdapter =
            (adapter.adapters.findLast { it is PokemonAdapter } ?: return) as PokemonAdapter

        val itemsCount = pokemonAdapter.itemCount

        if (loadingAdapter.loadState is LoadState.Error) {
            return
        }

        val containerHeight = recyclerView.measuredHeight
        recyclerView.addRecyclerListener { listener ->
            itemHeight = listener.itemView.height
        }

        if (dy == 0 && itemsCount <= containerHeight / itemHeight + VISIBLE_THRESHOLD) {
            pagingListener.onNextPage(itemsCount)
        }

        if (dy > 0
            && layoutManager.findLastVisibleItemPosition() + VISIBLE_THRESHOLD >= layoutManager.itemCount
        ) {
            pagingListener.onNextPage(itemsCount)
        }
    }

    companion object {
        private const val VISIBLE_THRESHOLD = 3
    }

}