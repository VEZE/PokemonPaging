package com.veze.pokemonpaging.util

import com.veze.pokemonpaging.ui.main.PokemonView
import io.reactivex.rxjava3.subjects.PublishSubject

class PokemonNewPageListener(
    private val publisher: PublishSubject<PokemonView.PokemonIntent.LoadPagination>
) : PagingListener {
    override fun onNextPage(offset: Int) {
        publisher.onNext(PokemonView.PokemonIntent.LoadPagination(offset))
    }


}
