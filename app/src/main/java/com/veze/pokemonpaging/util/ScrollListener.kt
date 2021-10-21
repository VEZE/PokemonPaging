package com.veze.pokemonpaging.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScrollListener(private val pagingListener: PagingListener) :
    RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager

        if (linearLayoutManager.findLastVisibleItemPosition() == linearLayoutManager.itemCount - 1) {
            pagingListener.onNextPage(recyclerView.adapter!!.itemCount)
        }

        if (dy > 0
            && linearLayoutManager.findLastVisibleItemPosition() + VISIBLE_THRESHOLD >= linearLayoutManager.itemCount
        ) {
            pagingListener.onNextPage(recyclerView.adapter!!.itemCount)
        }
    }

    companion object {
        private const val VISIBLE_THRESHOLD = 3
    }

}