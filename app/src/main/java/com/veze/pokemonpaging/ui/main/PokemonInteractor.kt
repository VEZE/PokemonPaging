package com.veze.pokemonpaging.ui.main

import com.veze.pokemonpaging.data.api.PokeApi
import com.veze.pokemonpaging.data.client.PokeClient
import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.data.model.urlToId
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers


class PokemonInteractor(private val pokeApi: PokeApi = PokeClient()) {

    fun updateList(numberArea: Int = 8): Observable<MutableList<Pokemon>> {
        return pokeApi.getLocationArea(numberArea)
            .flatMap { locationArea ->
                return@flatMap Observable.fromIterable(locationArea.pokemonEncounters).flatMap {
                    pokeApi.getPokemon(urlToId(it.pokemon.url))
                }.toList().toObservable()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())


    }

    fun updateListWithOffset(
        offset: Int = 0,
        limit: Int = 10
    ): Observable<MutableList<Pokemon>> {
        return pokeApi.getPokemonList(offset, limit).flatMap { namedApiResource ->
            return@flatMap Observable.fromIterable(namedApiResource.results).flatMap {
                pokeApi.getPokemon(urlToId(it.url))
            }.toList().toObservable()
        }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}
