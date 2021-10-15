package com.veze.pokemonpaging.ui.main

import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.mvi.MviViewState

/**
 * State of [PokemonActivity]
 */
sealed class PokemonViewState : MviViewState {
    object StartProgress : PokemonViewState()
    data class StartError(val reason: Throwable) : PokemonViewState()
    data class PokemonData(val data: List<Pokemon>, val pagingProgress: Boolean) :
        PokemonDataProvider(data)

    data class LoadingError(val data: List<Pokemon>, val reason: Throwable) :
        PokemonDataProvider(data)

    abstract class PokemonDataProvider(val dataSource: List<Pokemon>) : PokemonViewState()
}

/*
    val progress: Boolean = false,
    val error: Throwable? = null,
    val pokemonList: List<Pokemon> = listOf(),
    val pagingProgress: Boolean = false,
    val pagingPokemonList: List<Pokemon> = listOf()
 */