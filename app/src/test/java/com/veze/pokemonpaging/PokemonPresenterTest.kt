package com.veze.pokemonpaging

import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.ui.main.*
import io.mockk.*
import io.reactivex.rxjava3.android.plugins.RxAndroidPlugins
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


class PokemonPresenterTest {

    private val pokemonInteractor: PokemonInteractor = mockk()
    private val view: PokemonView = mockk()

    private val pokemonReducer = PokemonPresenter.PokemonReducer()

    private lateinit var mockPresenter: PokemonPresenter

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockPresenter = spyk(PokemonPresenter(pokemonInteractor))
        RxAndroidPlugins.setInitMainThreadSchedulerHandler { Schedulers.trampoline() }
    }

    @Test
    fun testPresenter() {
        val result = mutableListOf<Pokemon>()

        val mockedPokemon1: Pokemon = mockk()
        val mockedPokemon2: Pokemon = mockk()

        result.addAll(listOf(mockedPokemon1, mockedPokemon2))

        every {
            pokemonInteractor.getPokemons(
                any(),
                any()
            )
        } returns Observable.just(result)

        every { view.getIntentsStream() } returns Observable.fromIterable(
            listOf(
                PokemonIntent.Refresh,
                PokemonIntent.Initial,
                PokemonIntent.LoadMore(10)
            )
        )

        every { view.render(any()) } just Runs

        mockPresenter.bind(view)

        verify(exactly = 4) {
            view.render(any())
        }
        verify { view.render(PokemonViewState(pokemonList = result)) }
    }

    @Test
    fun testReducer() {
        val pokemonViewState = PokemonViewState()

        val pokemon1: Pokemon = mockk()
        val pokemon2: Pokemon = mockk()

        val resultList = mutableListOf(pokemon1, pokemon2)

        pokemonViewState.apply {

            assertReducer(
                PokemonAction.Initial.Loading,
                pokemonViewState.copy(progress = true)
            )


            assertReducer(
                PokemonAction.Paging.Loading,
                pokemonViewState.copy(pagingProgress = true)
            )


            assertReducer(
                PokemonAction.Initial.Success(resultList),
                pokemonViewState.copy(
                    progress = false,
                    pokemonList = resultList
                )
            )


            assertReducer(
                PokemonAction.Paging.Success(resultList),
                pokemonViewState.copy(
                    pagingProgress = false,
                    pokemonList = resultList + pokemonViewState.pokemonList
                )
            )

            val mockError = Throwable("mocked error")

            assertReducer(
                PokemonAction.Initial.Failure(mockError),
                pokemonViewState.copy(
                    progress = false,
                    exception = mockError
                )
            )
        }

    }

    fun PokemonViewState.assertReducer(
        pokemonAction: PokemonAction,
        resultState: PokemonViewState
    ) {
        pokemonReducer.apply(this, pokemonAction).apply {
            assertEquals(this, resultState)
        }

    }

}
