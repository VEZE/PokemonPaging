package com.veze.pokemonpaging.ui.main

import io.reactivex.rxjava3.core.Observable

interface PokemonView {

    fun render(state: PokemonState)

    fun getActionStream(): Observable<PokemonAction>

    sealed class PokemonAction {
        data class LoadPagination(val offset: Int) : PokemonAction()
        object Refresh : PokemonAction()
        object Initial : PokemonAction()
    }
}