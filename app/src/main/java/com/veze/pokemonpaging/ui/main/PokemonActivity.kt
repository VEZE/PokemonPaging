package com.veze.pokemonpaging.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.veze.pokemonpaging.R
import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.databinding.ActivityPokemonsBinding
import com.veze.pokemonpaging.util.EndlessScrollListener
import com.veze.pokemonpaging.util.PaginationException
import com.veze.pokemonpaging.util.PokemonPagingListener
import com.veze.pokemonpaging.util.showToast
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject


class PokemonActivity : AppCompatActivity(), PokemonView {

    private lateinit var binding: ActivityPokemonsBinding

    private val viewModel by viewModels<PokemonViewModel> { ViewModelFactory() }

    private var pokemonAdapter =
        PokemonAdapter { Log.d("TAG", "onCreate: ") }.apply {
            setHasStableIds(true)
        }
    private var loadingAdapter =
        LoadingAdapter() {
            pagingPublisher.onNext(PokemonIntent.LoadMore(pokemonAdapter.itemCount))
        }

    private val concatAdapter: ConcatAdapter by lazy {
        ConcatAdapter(pokemonAdapter, loadingAdapter)
    }

    private val initialPublisher = BehaviorSubject.create<PokemonIntent.Initial>()
    private val refreshPublisher = PublishSubject.create<PokemonIntent.Refresh>()
    private val pagingPublisher = PublishSubject.create<PokemonIntent.LoadMore>()

    override fun onResume() {
        super.onResume()
        initialPublisher.onNext(PokemonIntent.Initial)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPokemonsBinding.inflate(layoutInflater)

        setContentView(binding.root)

        setUpSwipeToRefresh()

        setUpRecycler()

        setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun render(state: PokemonViewState) = with(state) {
        showLoading(progress)
        pagingLoading(pagingProgress)
        submitList(pokemonList)
        handleException(exception)
    }


    private fun setUpRecycler() = with(binding.contentScrolling.pokemonRecycler) {
        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        adapter = concatAdapter

        addOnScrollListener(EndlessScrollListener(PokemonPagingListener(pagingPublisher)))
    }

    private fun setUpSwipeToRefresh() {
        binding.contentScrolling.swiperefresh.setOnRefreshListener {
            submitList(emptyList())
            refreshPublisher.onNext(PokemonIntent.Refresh)
        }
    }

    private fun submitList(pokemonList: List<Pokemon>) {
        pokemonAdapter.submitList(pokemonList)
    }

    private fun showLoading(progress: Boolean) {
        binding.contentScrolling.swiperefresh.isRefreshing = progress
    }

    private fun pagingLoading(pagingProgress: Boolean) = when (pagingProgress) {
        true -> loadingAdapter.loadState = LoadState.Loading
        false -> loadingAdapter.loadState = LoadState.Done
    }

    private fun handleException(exception: Throwable?) = when (exception) {
        null -> {
        }
        is PaginationException -> loadingAdapter.loadState = LoadState.Error(exception)
        else -> showException(exception)
    }

    private fun initIntent(): Observable<PokemonIntent.Initial> = initialPublisher

    private fun refreshIntent(): Observable<PokemonIntent.Refresh> = refreshPublisher

    private fun loadMoreIntent(): Observable<PokemonIntent.LoadMore> =
        pagingPublisher

    override fun getIntentsStream(): Observable<PokemonIntent> {
        return Observable.merge(
            initIntent(),
            refreshIntent(),
            loadMoreIntent()
        )
    }

    private fun showException(exception: Throwable) = showToast("${exception.message}")

    override fun onStart() {
        super.onStart()
        viewModel.presenter.bind(this)

    }

    override fun onStop() {
        viewModel.presenter.unbind()
        super.onStop()
    }

}
