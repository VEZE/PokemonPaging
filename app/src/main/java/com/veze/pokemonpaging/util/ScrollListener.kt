package com.veze.pokemonpaging.util

import androidx.recyclerview.widget.RecyclerView

class ScrollListener(private val pagingListener: PagingListener) :
    RecyclerView.OnScrollListener() {

    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        if (!recyclerView.canScrollVertically(1) && newState == RecyclerView.SCROLL_STATE_IDLE) {
            pagingListener.onNextPage(recyclerView.adapter!!.itemCount)
        }
    }
}