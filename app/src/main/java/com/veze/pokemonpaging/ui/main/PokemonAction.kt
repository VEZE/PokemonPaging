package com.veze.pokemonpaging.ui.main

import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.mvi.MviAction

/**
 *
 */
sealed class PokemonAction : MviAction {
    data class Initial(val lastState: PokemonViewState) : PokemonAction()

    object Loading : PokemonAction()
    data class SubmitList(val pokemonList: List<Pokemon>) : PokemonAction()

    object PagingLoading : PokemonAction()
    data class SubmitPagingList(val pagingPokemonList: List<Pokemon>) : PokemonAction()

    data class Error(val error: Throwable) : PokemonAction()
}
