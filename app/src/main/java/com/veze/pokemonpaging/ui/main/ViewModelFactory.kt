package com.veze.pokemonpaging.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import javax.xml.parsers.FactoryConfigurationError

class ViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when (modelClass) {
            PokemonViewModel::class.java -> {
                return PokemonViewModel(PokemonPresenter(PokemonInteractor())) as T
            }
            else -> {
                throw FactoryConfigurationError("Those factory not exist = $modelClass")
            }
        }
    }
}
