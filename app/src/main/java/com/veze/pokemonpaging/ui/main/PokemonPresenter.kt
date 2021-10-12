package com.veze.pokemonpaging.ui.main

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class PokemonPresenter(val interactor: PokemonInteractor) {

    private val compositeDisposable = CompositeDisposable()
    private val stateSubject = BehaviorSubject.create<PokemonViewState>()

    fun bind(mainView: PokemonView) {
        val updateListObservable = mainView.getActionStream().flatMap {
            interactor.updateList().map {
                return@map if (it.isEmpty()) {
                    PokemonViewState.Error(Throwable("is empty"))
                } else {
                    PokemonViewState.PokemonData(it)
                }
            }
                .doOnSubscribe { PokemonViewState.Loading }
                .onErrorReturn { error -> PokemonViewState.Error(error = error) }
        }

        val mergedIntentsObservable =
            Observable.merge(listOf(updateListObservable)).subscribeWith(stateSubject)

        compositeDisposable.add(
            mergedIntentsObservable
                .subscribe { mainView.render(reduce(it)) }
        )
    }

    private fun reduce(partialState: PokemonViewState): PokemonState {
        return when (partialState) {
            is PokemonViewState.Loading -> PokemonState(progress = true)
            is PokemonViewState.Error -> PokemonState(progress = false, error = partialState.error)
            is PokemonViewState.PokemonData -> PokemonState(
                progress = false,
                pokemonList = partialState.pokemonList
            )
        }
    }

    fun unbind() {
        compositeDisposable.clear()
    }
}