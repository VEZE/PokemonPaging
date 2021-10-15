package com.veze.pokemonpaging.ui.main

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit

/**
 *  Presents  data in reactive way to view.
 *
 *  bind -> should be called when view is attached (Example: onStart())
 *  unbind -> should be called when view is detached (Example: onStop())
 *
 * @param interactor  used for interaction with data layer
 */
class PokemonPresenter(private val interactor: PokemonInteractor) {

    private val compositeDisposable = CompositeDisposable()
    private val stateSubject =
        BehaviorSubject.create<PokemonViewState>(PokemonViewState.StartProgress)



    fun bind(mainView: PokemonView) {
        compositeDisposable +=
            mainView.getIntentsStream()
                .switchMap { pokemonAction ->
                    when (pokemonAction) {
                        PokemonView.PokemonIntent.InitView {
                            stateSubject.firstOrError()
                        }

                                PokemonView . PokemonIntent . Initial -> {
                            Observable.just(PokemonAction.SubmitList(emptyList()))
                                .delay(1000, TimeUnit.MILLISECONDS)
                        }
                        PokemonView.PokemonIntent.Refresh -> {
                            interactor.getPokemons().map {
                                return@map PokemonAction.SubmitList(it)
                            }.startWith(Observable.just(PokemonAction.PagingLoading))
                        }
                        is PokemonView.PokemonIntent.LoadPagination -> {
                            interactor.getPokemons(pokemonAction.offset).map {
                                return@map PokemonAction.SubmitPagingList(it)
                            }.startWith(Observable.just(PokemonAction.PagingLoading))
                        }
                    }
                }
                .onErrorReturn { error -> PokemonAction.Error(error = error) }
                .scan(PokemonViewState(), reducer)
                .distinctUntilChanged()
                .observeOn()
                .subscribe {
                    stateSubject.onNext(it)
                    mainView.render(it)
                }

    }

    private val reducer = BiFunction { previousViewState: PokemonViewState, result: PokemonAction ->

        return@BiFunction when (result) {
            is PokemonAction.Loading -> previousViewState.copy(progress = true)
            is PokemonAction.Error -> previousViewState.copy(progress = false, error = result.error)
            is PokemonAction.SubmitList -> previousViewState.copy(
                progress = false,
                pokemonList = result.pokemonList,
                pagingProgress = false
            )
            is PokemonAction.SubmitPagingList -> previousViewState.copy(
                progress = false,
                pagingPokemonList = result.pagingPokemonList,
                pagingProgress = false
            )
            PokemonAction.PagingLoading -> previousViewState.copy(
                progress = false,
                pagingProgress = true
            )
        }
    }

    fun unbind() {
        compositeDisposable.clear()
    }


}
