package com.veze.pokemonpaging.mvi

import io.reactivex.rxjava3.core.Observable

/**
 * Object representing a UI that will
 * a) emit its intents to a view,
 * b) subscribes to a view for rendering its UI.
 *
 * @param I Top class of the [MviIntent] that the [MviView] will be emitting.
 * @param S Top class of the [MviViewState] the [MviView] will be subscribing to.
 */
interface MviView<I : MviIntent, S : MviViewState> {

    /**
     * Unique [Observable] to listen to the [MviView].
     * All the [MviView]'s [MviIntent]s must go through this [Observable].
     */
    fun getIntentsStream(): Observable<I>


    /**
     * Entry point for the [MviView] to render itself based on a [MviViewState].
     */
    fun render(state: S)
}