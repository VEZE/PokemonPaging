package com.veze.pokemonpaging.util

import com.veze.pokemonpaging.ui.main.PokemonIntent
import io.reactivex.rxjava3.subjects.PublishSubject

class PokemonPagingListener(
    private val publisher: PublishSubject<PokemonIntent.LoadMore>
) : PagingListener {
    override fun onNextPage(offset: Int) {
        publisher.onNext(PokemonIntent.LoadMore(offset))
    }


}
