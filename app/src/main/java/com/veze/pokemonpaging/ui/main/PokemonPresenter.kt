package com.veze.pokemonpaging.ui.main

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

class PokemonPresenter(val interactor: PokemonInteractor) {

    private val compositeDisposable = CompositeDisposable()
    private val stateSubject = BehaviorSubject.create<PokemonViewState>()

    fun bind(mainView: PokemonView) {

        val actionStreamObservable =
            mainView.getActionStream()
                .flatMap<PokemonViewState> { pokemonAction ->
                    when (pokemonAction) {
                        PokemonView.PokemonAction.Initial -> {
                            Observable.just(PokemonViewState.PokemonData(emptyList()))
                                .delay(1000, TimeUnit.MILLISECONDS)
                        }
                        PokemonView.PokemonAction.Refresh -> {
                            interactor.updateListWithOffset().map {
                                return@map PokemonViewState.PokemonData(it)
                            }
                        }
                        is PokemonView.PokemonAction.LoadPagination -> {
                            interactor.updateListWithOffset(pokemonAction.offset).map {
                                return@map PokemonViewState.PagingPokemonData(it)
                            }.doOnSubscribe { PokemonViewState.PagingLoading }
                        }
                    }
                }
                .startWithArray(PokemonViewState.Loading)
                .onErrorReturn { error -> PokemonViewState.Error(error = error) }


        val mergedIntentsObservable =
            Observable.merge(listOf(actionStreamObservable)).subscribeWith(stateSubject)

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
                pokemonList = partialState.pokemonList,
                pagingProgress = false
            )
            is PokemonViewState.PagingPokemonData -> PokemonState(
                progress = false,
                pagingPokemonList = partialState.pagingPokemonList,
                pagingProgress = false
            )
            PokemonViewState.PagingLoading -> PokemonState(progress = false, pagingProgress = true)
        }
    }

    fun unbind() {
        compositeDisposable.clear()
    }
}