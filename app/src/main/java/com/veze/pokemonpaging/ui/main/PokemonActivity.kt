package com.veze.pokemonpaging.ui.main

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.veze.pokemonpaging.R
import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.databinding.ActivityPokemonsBinding
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject


class PokemonActivity : AppCompatActivity(), PokemonView {

    private lateinit var binding: ActivityPokemonsBinding

    private lateinit var pokemonAdapter: PokemonAdapter

    private val mainPresenter: PokemonPresenter by lazy { PokemonPresenter(PokemonInteractor()) }
    private val refreshActionPublisher = PublishSubject.create<PokemonView.PokemonAction.Refresh>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPokemonsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setUpSwipeToRefresh()

        setUpRecycler()

        setSupportActionBar(findViewById(R.id.toolbar))
        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }

    private fun setUpSwipeToRefresh() {
        binding.contentScrolling.swiperefresh.setOnRefreshListener {
            showPokemonList(emptyList())
            refreshActionPublisher.onNext(PokemonView.PokemonAction.Refresh)
        }
    }

    private fun setUpRecycler() {
        pokemonAdapter = PokemonAdapter { _ -> Log.d("TAG", "onCreate: ") }
        binding.contentScrolling.pokemonRecycler.adapter = pokemonAdapter
        binding.contentScrolling.pokemonRecycler.layoutManager = LinearLayoutManager(this)
    }


    override fun onStart() {
        super.onStart()
        mainPresenter.bind(this)

    }

    override fun onStop() {
        mainPresenter.unbind()
        super.onStop()
    }

    override fun render(state: PokemonState) {
        with(state) {
            showLoading(progress)
            showError(error)
            showPokemonList(pokemonList)
        }
    }

    private fun showLoading(progress: Boolean) {
        binding.contentScrolling.swiperefresh.isRefreshing = progress
    }

    private fun showPokemonList(pokemonList: List<Pokemon>) {
        pokemonAdapter.submitList(pokemonList)
    }

    private fun showError(error: Throwable?) {
        if (error != null) {
            Toast.makeText(this, "${error.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun refreshAction(): Observable<PokemonView.PokemonAction.Refresh> {
        return refreshActionPublisher
    }

    private fun initAction(): Observable<PokemonView.PokemonAction.Initial> {
        return Observable.just(PokemonView.PokemonAction.Initial)
    }

    override fun getActionStream(): Observable<PokemonView.PokemonAction> {
        return Observable.merge(refreshAction(), initAction())
    }

}