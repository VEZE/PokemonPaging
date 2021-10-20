package com.veze.pokemonpaging.ui.main

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.veze.pokemonpaging.R
import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.databinding.ActivityPokemonsBinding
import com.veze.pokemonpaging.util.*
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject


class PokemonActivity : AppCompatActivity(), PokemonView {

    private lateinit var binding: ActivityPokemonsBinding

    private lateinit var pokemonAdapter: PokemonAdapter

    private val mainPresenter: PokemonPresenter by lazy { PokemonPresenter(PokemonInteractor()) }

    private val refreshActionPublisher = PublishSubject.create<PokemonView.PokemonIntent.Refresh>()
    private val pagingActionPublisher =
        PublishSubject.create<PokemonView.PokemonIntent.LoadPagination>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPokemonsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setUpSwipeToRefresh()

        setUpRecycler()

        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun render(state: PokemonViewState) = with(state) {
        showLoading(progress, pagingProgress)
        showPokemonList(pokemonList)

        if (error != null) showError(error)

    }

    private fun setUpRecycler() = with(binding.contentScrolling.pokemonRecycler) {
        layoutManager = LinearLayoutManager(this@PokemonActivity)

        pokemonAdapter = PokemonAdapter { Log.d("TAG", "onCreate: ") }
        adapter = pokemonAdapter

        addOnScrollListener(ScrollListener(PokemonNewPageListener(pagingActionPublisher)))
    }

    private fun setUpSwipeToRefresh() {
        binding.contentScrolling.swiperefresh.setOnRefreshListener {
            showPokemonList(emptyList())
            refreshActionPublisher.onNext(PokemonView.PokemonIntent.Refresh)
        }
    }

    private fun showPokemonList(pokemonList: List<Pokemon>) {
        pokemonAdapter.submitList(pokemonList)
    }

    private fun showLoading(progress: Boolean, pagingProgress: Boolean) {
        val resultProgress = progress || pagingProgress

        binding.contentScrolling.swiperefresh.isRefreshing = resultProgress
    }

    private fun showError(error: Throwable) = showToast("${error.message}")


    private fun refreshAction(): Observable<PokemonView.PokemonIntent.Refresh> {
        return refreshActionPublisher
    }

    private fun initAction(): Observable<PokemonView.PokemonIntent.Initial> {
        return Observable.just(PokemonView.PokemonIntent.Initial)
    }

    private fun loadPaginationAction(): Observable<PokemonView.PokemonIntent.LoadPagination> {
        return pagingActionPublisher
    }

    override fun getIntentsStream(): Observable<PokemonView.PokemonIntent> {
        return Observable.merge(refreshAction(), initAction(), loadPaginationAction())
    }

    override fun onStart() {
        super.onStart()
        mainPresenter.bind(this)

    }

    override fun onStop() {
        mainPresenter.unbind()
        super.onStop()
    }

}
