package com.veze.pokemonpaging

import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.ui.main.*
import io.mockk.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
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

        val initialPublisher = PublishSubject.create<PokemonView.PokemonIntent.Initial>()

        val refreshPublisher =
            BehaviorSubject.createDefault(PokemonView.PokemonIntent.Refresh)


        val pagingPublisher =
            PublishSubject.create<PokemonView.PokemonIntent.LoadPagination>()



        every { view.getIntentsStream() } returns Observable.fromIterable(
            listOf<PokemonView.PokemonIntent>(
                PokemonView.PokemonIntent.Refresh,
                PokemonView.PokemonIntent.Initial,
                PokemonView.PokemonIntent.LoadPagination(10)
            )
        )
        every { view.render(any()) } just Runs

        mockPresenter.bind()

        verify { pokemonInteractor.getPokemons(any(), any()) }

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
                PokemonAction.Loading,
                pokemonViewState.copy(progress = true)
            )


            assertReducer(
                PokemonAction.PagingLoading,
                pokemonViewState.copy(pagingProgress = true)
            )


            assertReducer(
                PokemonAction.SubmitList(resultList),
                pokemonViewState.copy(
                    progress = false,
                    pokemonList = resultList
                )
            )


            assertReducer(
                PokemonAction.SubmitPagingList(resultList),
                pokemonViewState.copy(
                    pagingProgress = false,
                    pokemonList = resultList + pokemonViewState.pokemonList
                )
            )

            val mockError = Throwable("mocked error")

            assertReducer(
                PokemonAction.Error(mockError),
                pokemonViewState.copy(
                    progress = false,
                    error = mockError
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
