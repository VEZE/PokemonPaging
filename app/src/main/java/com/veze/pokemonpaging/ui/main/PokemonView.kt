package com.veze.pokemonpaging.ui.main

import com.veze.pokemonpaging.mvi.MviIntent
import com.veze.pokemonpaging.mvi.MviView

interface PokemonView : MviView<PokemonView.PokemonIntent, NewPokemonViewState> {

    sealed class PokemonIntent : MviIntent {
        data class LoadPagination(val offset: Int) : PokemonIntent()
        object Refresh : PokemonIntent()
        object Initial : PokemonIntent()
    }
}
