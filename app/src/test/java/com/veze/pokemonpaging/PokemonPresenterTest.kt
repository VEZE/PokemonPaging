package com.veze.pokemonpaging

import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.ui.main.*
import io.mockk.*
import io.reactivex.rxjava3.core.Observable
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PokemonPresenterTest {

    val pokemonInteractor: PokemonInteractor = mockk()
    val view: PokemonView = mockk()

    lateinit var mockPresenter: PokemonPresenter

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockPresenter = spyk(
            PokemonPresenter(pokemonInteractor, view),
            recordPrivateCalls = true
        )
    }

    @Test
    fun testPresenter() {
        val result = mutableListOf<Pokemon>()

        val mockedPokemon1: Pokemon = mockk()
        val mockedPokemon2: Pokemon = mockk()

        result.addAll(listOf(mockedPokemon1, mockedPokemon2))

        every { pokemonInteractor.getPokemons() } returns Observable.just(result)

        every { view.getIntentsStream() } returns Observable.merge(
            Observable.just(PokemonView.PokemonIntent.Refresh),
            Observable.just(PokemonView.PokemonIntent.Initial),
            Observable.just(PokemonView.PokemonIntent.LoadPagination(10))
        )
        every { view.render(any()) } just Runs

        mockPresenter.bind()

        verify { pokemonInteractor.getPokemons(any(), any()) }

        verify(exactly = 1) {
            view.render(withArg {
                assertTrue(it == PokemonViewState(pokemonList = result))
            })
        }
        verify { view.render(PokemonViewState(pokemonList = result)) }
    }

    @Test
    fun testReducer() {
        val pokemonViewState = PokemonViewState()
        var pokemonAction: PokemonAction


        val pokemon1: Pokemon = mockk()
        val pokemon2: Pokemon = mockk()

        val resultList = mutableListOf(pokemon1, pokemon2)


        pokemonAction = PokemonAction.Loading

        callReducer(pokemonViewState, pokemonAction, pokemonViewState.copy(progress = true))

        pokemonAction = PokemonAction.PagingLoading

        callReducer(
            pokemonViewState,
            pokemonAction,
            pokemonViewState.copy(pagingProgress = true)
        )

        pokemonAction = PokemonAction.SubmitList(resultList)

        callReducer(
            pokemonViewState,
            pokemonAction,
            pokemonViewState.copy(
                progress = false,
                pokemonList = resultList
            )
        )

        pokemonAction = PokemonAction.SubmitPagingList(resultList)

        callReducer(
            pokemonViewState,
            pokemonAction,
            pokemonViewState.copy(
                pagingProgress = false,
                pokemonList = resultList + pokemonViewState.pokemonList
            )
        )

        val mockError = Throwable("mocked error")

        pokemonAction = PokemonAction.Error(mockError)

        callReducer(
            pokemonViewState,
            pokemonAction,
            pokemonViewState.copy(
                progress = false,
                error = mockError
            )
        )

    }

    private fun callReducer(
        pokemonViewState: PokemonViewState,
        pokemonAction: PokemonAction,
        resultState: PokemonViewState
    ) {
        val result = mockPresenter.reducer.apply(pokemonViewState, pokemonAction)

        assertEquals(result, resultState)
    }

}
