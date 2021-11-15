package com.veze.pokemonpaging.ui.main

import com.veze.pokemonpaging.mvi.MviIntent
import com.veze.pokemonpaging.mvi.MviView

interface PokemonView : MviView<PokemonIntent, PokemonViewState>

sealed class PokemonIntent : MviIntent {
    data class LoadMore(val offset: Int) : PokemonIntent()
    data class LoadDetails(val url: String,val id: Int) : PokemonIntent()
    object Refresh : PokemonIntent()
    object Initial : PokemonIntent()
}
