package com.veze.pokemonpaging.util

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ScrollListener(private val pagingListener: PagingListener) :
    RecyclerView.OnScrollListener() {
    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
        val lastItem = recyclerView.adapter?.itemCount ?: return

        val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

        if (lastVisibleItem == lastItem - 1) {
            pagingListener.onNextPage(recyclerView.adapter!!.itemCount)
        }
    }

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
            pagingListener.onNextPage(recyclerView.adapter!!.itemCount)
        }
    }
}