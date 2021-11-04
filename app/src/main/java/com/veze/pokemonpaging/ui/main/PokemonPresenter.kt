package com.veze.pokemonpaging.ui.main

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.subjects.BehaviorSubject

/**
 *  Presents  data in reactive way to view.
 *
 *  bind -> should be called when view is attached (Example: onStart())
 *  unbind -> should be called when view is detached (Example: onStop())
 *
 * @param interactor  used for interaction with data layer
 */
class PokemonPresenter(
    private val interactor: PokemonInteractor,
    private val mainView: PokemonView
) {

    private val compositeDisposable = CompositeDisposable()
    private val stateSubject = BehaviorSubject.createDefault(PokemonViewState())
    private val reducer = PokemonReducer()

    fun bind() {
        compositeDisposable += mainView.getIntentsStream()
            .switchMap { pokemonAction ->
                when (pokemonAction) {
                    PokemonView.PokemonIntent.Initial -> {
                        stateSubject.firstOrError().map<PokemonAction> {
                            return@map PokemonAction.Initial(it)
                        }.toObservable()
                    }
                    PokemonView.PokemonIntent.Refresh -> {
                        interactor.getPokemons().map<PokemonAction> {
                            return@map PokemonAction.SubmitList(it)
                        }.startWith(Observable.just(PokemonAction.Loading))
                    }
                    is PokemonView.PokemonIntent.LoadPagination -> {
                        interactor.getPokemons(pokemonAction.offset).map<PokemonAction> {
                            return@map PokemonAction.SubmitPagingList(it)
                        }.startWith(Observable.just(PokemonAction.PagingLoading))
                    }
                }
            }
            .onErrorReturn { error -> PokemonAction.Error(error = error) }
            .scan(PokemonViewState(), reducer::apply)
            .observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .subscribeBy(
                onNext = {
                    stateSubject.onNext(it)
                    mainView.render(it)
                }
            )
        stateSubject.subscribeBy(onNext =
        { println("OnNext + $it") })

    }


    class PokemonReducer : BiFunction<PokemonViewState, PokemonAction, PokemonViewState> {
        override fun apply(
            previousViewState: PokemonViewState,
            action: PokemonAction
        ): PokemonViewState {
            return when (action) {
                is PokemonAction.Initial -> action.lastState
                is PokemonAction.Loading -> previousViewState.copy(progress = true)
                is PokemonAction.Error -> previousViewState.copy(
                    progress = false,
                    error = action.error
                )
                is PokemonAction.SubmitList -> previousViewState.copy(
                    progress = false,
                    pokemonList = action.pokemonList,
                    pagingProgress = false
                )
                is PokemonAction.SubmitPagingList -> previousViewState.copy(
                    pokemonList = previousViewState.pokemonList + action.pagingPokemonList,
                    progress = false,
                    pagingProgress = false
                )
                PokemonAction.PagingLoading -> previousViewState.copy(
                    progress = false,
                    pagingProgress = true
                )
            }
        }

    }

    fun unbind() {
        compositeDisposable.clear()
    }

}
