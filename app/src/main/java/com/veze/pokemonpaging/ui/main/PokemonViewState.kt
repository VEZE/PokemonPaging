package com.veze.pokemonpaging.ui.main

import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.mvi.MviViewState

sealed class PokemonViewState : MviViewState {
    object Loading : PokemonViewState()
    data class PokemonData(val pokemonList: List<Pokemon>) : PokemonViewState()
    data class PagingPokemonData(val pagingPokemonList: List<Pokemon>) : PokemonViewState()
    object PagingLoading : PokemonViewState()
    data class Error(val error: Throwable) : PokemonViewState()
}