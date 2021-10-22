package com.veze.pokemonpaging.ui.main

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
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
    private val stateSubject = BehaviorSubject.create<NewPokemonViewState>()

    fun bind(mainView: PokemonView) {
        compositeDisposable += mainView.getIntentsStream()
            .switchMap { pokemonAction ->
                when (pokemonAction) {
                    PokemonView.PokemonIntent.Initial -> {
                        stateSubject.firstOrError()

                        Observable.just(PokemonAction.SubmitList(emptyList()))
                            .delay(1000, TimeUnit.MILLISECONDS)
                    }
                    PokemonView.PokemonIntent.Refresh -> {
                        interactor.getPokemons().map {
                            return@map PokemonAction.SubmitList(it) as PokemonAction
                        }.startWith(Observable.just(PokemonAction.Loading))
                    }
                    is PokemonView.PokemonIntent.LoadPagination -> {
                        interactor.getPokemons(pokemonAction.offset).map {
                            return@map PokemonAction.SubmitPagingList(it) as PokemonAction
                        }.startWith(Observable.just(PokemonAction.PagingLoading))
                    }
                }
            }
            .onErrorReturn { error -> PokemonAction.Error(error = error) }
            .map(this::reduce)
            .observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .subscribe {
                stateSubject.onNext(it)
                mainView.render(it)
            }

    }

    private fun reduce(action: PokemonAction): NewPokemonViewState {
        val prevState = stateSubject.value ?: NewPokemonViewState.Loading(
            emptyList(),
            NewPokemonViewState.Progress(
                screenProgress = false,
                pagingProgress = false
            )
        )

        return when (action) {
            is PokemonAction.Error -> NewPokemonViewState.LoadingError(
                emptyList(),
                action.error,
                prevState.innerSource.copy(screenProgress = false, pagingProgress = false)
            )
            PokemonAction.Loading -> NewPokemonViewState.Loading(
                prevState.innerPokemonSource,
                prevState.innerSource.copy(
                    screenProgress = true,
                    pagingProgress = false
                )
            )
            PokemonAction.PagingLoading -> NewPokemonViewState.Loading(
                prevState.innerPokemonSource,
                prevState.innerSource.copy(
                    screenProgress = false,
                    pagingProgress = true
                )
            )
            is PokemonAction.SubmitList -> NewPokemonViewState.PokemonData(
                action.pokemonList, prevState.innerSource.copy(
                    screenProgress = false,
                    pagingProgress = false
                )
            )
            is PokemonAction.SubmitPagingList -> {
                return NewPokemonViewState.PokemonData(
                    data = prevState.innerPokemonSource + action.pagingPokemonList,
                    progress = prevState.innerSource.copy(
                        screenProgress = false,
                        pagingProgress = false
                    )

                )
            }
        }
    }

//    private val reducer =
//        BiFunction { previousViewState: NewPokemonViewState, result: PokemonAction ->
//
//            return@BiFunction when (result) {
//                is PokemonAction.Loading -> previousViewState.copy(progress = true)
//                is PokemonAction.Error -> previousViewState.copy(
//                    progress = false,
//                    error = result.error
//                )
//                is PokemonAction.SubmitList -> previousViewState.copy(
//                    progress = false,
//                    pokemonList = result.pokemonList,
//                    pagingProgress = false
//                )
//                is PokemonAction.SubmitPagingList -> previousViewState.copy(
//                    pokemonList = previousViewState.pokemonList + result.pagingPokemonList,
//                    progress = false,
//                    pagingProgress = false
//                )
//                PokemonAction.PagingLoading -> previousViewState.copy(
//                    progress = false,
//                    pagingProgress = true
//                )
//            }
//        }

    fun unbind() {
        compositeDisposable.clear()
    }
}
