package com.veze.pokemonpaging.ui.main

import com.veze.pokemonpaging.data.model.Pokemon

data class PokemonState(
    val progress: Boolean = false,
    val error: Throwable? = null,
    val pokemonList: List<Pokemon> = listOf(),
    val pagingProgress: Boolean = false,
    val pagingPokemonList: List<Pokemon> = listOf()
)
