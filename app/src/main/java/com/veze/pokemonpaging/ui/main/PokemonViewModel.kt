package com.veze.pokemonpaging.ui.main

import androidx.lifecycle.ViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable

class PokemonViewModel(val presenter: PokemonPresenter) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

}
