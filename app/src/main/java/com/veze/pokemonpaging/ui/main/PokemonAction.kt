package com.veze.pokemonpaging.ui.main

import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.mvi.MviAction

/**
 * Represents all [PokemonView] action's
 */
sealed class PokemonAction : MviAction {

    sealed class Initial : PokemonAction() {
        data class Success(val result: MutableList<Pokemon>) : Initial()
        data class Failure(val error: Throwable) : Initial()
        data class Initialize(val lastState: PokemonViewState) : Initial()
        object Loading : Initial()
    }

    sealed class Paging : PokemonAction() {
        data class Success(val result: MutableList<Pokemon>) : Paging()
        data class Failure(val error: Throwable) : Paging()
        object Loading : Paging()
    }

}
