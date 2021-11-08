package com.veze.pokemonpaging.ui.main

import com.veze.pokemonpaging.util.PaginationException
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.functions.BiFunction
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
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
    private val stateSubject = BehaviorSubject.createDefault(PokemonViewState())
    private val reducer = PokemonReducer()

    fun bind(mainView: PokemonView) {
        compositeDisposable += mainView.getIntentsStream()
            .switchMap { intent ->
                when (intent) {
                    PokemonIntent.Initial -> {
                        stateSubject.firstOrError()
                            .map<PokemonAction> { PokemonAction.Initial.Initialize(it) }
                            .toObservable()
                            .onErrorReturn { error -> PokemonAction.Initial.Failure(error = error) }

                    }
                    PokemonIntent.Refresh -> {
                        interactor.getPokemons()
                            .map<PokemonAction> { PokemonAction.Initial.Success(it) }
                            .startWith(Observable.just(PokemonAction.Initial.Loading))
                            .onErrorReturn { error -> PokemonAction.Initial.Failure(error = error) }

                    }
                    is PokemonIntent.LoadMore -> {
                        interactor.getPokemons(intent.offset)
                            .map<PokemonAction> { PokemonAction.Paging.Success(it) }
                            .delay(2000, TimeUnit.MILLISECONDS)
                            .startWith(Observable.just(PokemonAction.Paging.Loading))
                            .onErrorReturn { PokemonAction.Paging.Failure(it) }
                    }
                }
            }
            .scan(PokemonViewState(), reducer::apply)
            .observeOn(AndroidSchedulers.mainThread())
            .distinctUntilChanged()
            .subscribeBy(
                onNext = {
                    stateSubject.onNext(it)
                    mainView.render(it)
                }
            )
    }


    class PokemonReducer : BiFunction<PokemonViewState, PokemonAction, PokemonViewState> {
        override fun apply(
            previousViewState: PokemonViewState,
            action: PokemonAction
        ): PokemonViewState {
            return when (action) {
                is PokemonAction.Initial.Initialize -> action.lastState
                is PokemonAction.Initial.Loading -> previousViewState.copy(progress = true)
                is PokemonAction.Initial.Failure -> previousViewState.copy(
                    progress = false,
                    exception = action.error
                )
                is PokemonAction.Initial.Success -> previousViewState.copy(
                    progress = false,
                    pokemonList = action.result,
                    exception = null
                )
                PokemonAction.Paging.Loading -> previousViewState.copy(
                    pagingProgress = true,
                    exception = null
                )
                is PokemonAction.Paging.Failure -> previousViewState.copy(
                    pagingProgress = false,
                    exception = action.error as PaginationException
                )
                is PokemonAction.Paging.Success -> previousViewState.copy(
                    pokemonList = previousViewState.pokemonList + action.result,
                    pagingProgress = false,
                    exception = null
                )
            }
        }

    }

    fun unbind() {
        compositeDisposable.clear()
    }

}
