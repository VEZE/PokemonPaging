package com.veze.pokemonpaging.ui.main

import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.mvi.MviViewState

/**
 * State of [PokemonActivity]
 */
data class PokemonViewState(
    val progress: Boolean = false,
    val exception: Throwable? = null,
    val pokemonList: List<Pokemon> = listOf(),
    val pagingProgress: Boolean = false
) : MviViewState
