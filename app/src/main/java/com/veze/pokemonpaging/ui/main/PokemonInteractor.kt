package com.veze.pokemonpaging.ui.main

import com.veze.pokemonpaging.data.api.PokeApi
import com.veze.pokemonpaging.data.client.PokeClient
import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.data.model.urlToId
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers

/**
 * Interacts with [PokeApi] to retrieve list of pokemons
 */
class PokemonInteractor(private val pokeApi: PokeApi = PokeClient()) {

    /**
     * Retrieve list of Pokemons in specific Area
     *
     * Default area id = 8
     *
     *
     * @param numberArea id of area
     * @return list of Pokemons
     */
    fun getPokemonsFromArea(numberArea: Int = 8): Observable<MutableList<Pokemon>> {
        return pokeApi.getLocationArea(numberArea)
            .flatMap { locationArea ->
                return@flatMap Observable.fromIterable(locationArea.pokemonEncounters).flatMap {
                    pokeApi.getPokemon(urlToId(it.pokemon.url))
                }.toList().toObservable()
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())


    }

    /**
     * Retrieve list of Pokemons in pages
     *
     * default offset = 0, default limit = 10
     *
     * @param offset offset relative to the current loaded list of Pokemons
     * @param limit number of loaded Pokemons
     * @return list of Pokemons
     */

//    var isError = false

    fun getPokemons(
        offset: Int = 0,
        limit: Int = 10,
    ): Observable<List<Pokemon>> {
//        if (offset > 20 && isError == false) {
//            isError = true
//            return Observable.error(PaginationException("paginationError"))
//        }

        return pokeApi.getPokemonList(offset, limit).flatMap { namedApiResource ->
            val resource = namedApiResource.results
            return@flatMap Observable.fromIterable(resource).flatMap { namedResource ->
                getPokemonDetails(namedResource.url).map { return@map Pokemon(it.id, it.name, url = namedResource.url) }
            }
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .toSortedList { pokemonFirst, pokemonSecond -> pokemonFirst.id - pokemonSecond.id }
                .toObservable()
        }
    }


    fun getPokemonDetails(
        pokemonUrl: String
    ): Observable<Pokemon> {
        return pokeApi.getPokemon(urlToId(pokemonUrl))
    }
}
