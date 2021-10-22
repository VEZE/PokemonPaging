package com.veze.pokemonpaging.ui.main

import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.mvi.MviViewState

sealed class NewPokemonViewState(
    val innerPokemonSource: List<Pokemon>,
    val innerSource: Progress
) : MviViewState {

    data class Loading(
        val data: List<Pokemon>,
        val progress: Progress
    ) : NewPokemonViewState(data, progress)

    data class PokemonData(val data: List<Pokemon>, val progress: Progress) :
        PokemonDataProvider(data, progress)

    data class LoadingError(
        val data: List<Pokemon>,
        val reason: Throwable,
        val progress: Progress
    ) :
        PokemonDataProvider(data, progress)


    abstract class PokemonDataProvider(
        val dataSource: List<Pokemon>,
        val progressSource: Progress
    ) :
        NewPokemonViewState(dataSource, progressSource)


    data class Progress(val screenProgress: Boolean, val pagingProgress: Boolean)

}