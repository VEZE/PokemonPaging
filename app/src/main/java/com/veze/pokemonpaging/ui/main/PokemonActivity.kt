package com.veze.pokemonpaging.ui.main

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.data.model.PokemonItemStatus
import com.veze.pokemonpaging.databinding.ActivityPokemonsBinding
import com.veze.pokemonpaging.util.EndlessScrollListener
import com.veze.pokemonpaging.util.PaginationException
import com.veze.pokemonpaging.util.PokemonPagingListener
import com.veze.pokemonpaging.util.showToast
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject
import java.util.concurrent.TimeUnit


class PokemonActivity : AppCompatActivity(), PokemonView {

    private lateinit var binding: ActivityPokemonsBinding

    private val viewModel by viewModels<PokemonViewModel> { ViewModelFactory() }

    private val emptyAdapter = EmptyAdapter()

    private val detailsPublisher = PublishSubject.create<PokemonIntent.LoadDetails>()

    private var pokemonAdapter =
        PokemonAdapter(detailsPublisher) { Log.d("TAG", "onCreate: ") }.apply {
            setHasStableIds(true)
        }
    private var loadingAdapter =
        LoadingAdapter() {
            pagingPublisher.onNext(PokemonIntent.LoadMore(pokemonAdapter.itemCount))
        }

    private val concatAdapter: ConcatAdapter by lazy {
        ConcatAdapter(emptyAdapter, pokemonAdapter, loadingAdapter)
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

        // setSupportActionBar(findViewById(R.id.toolbar))
    }

    override fun render(state: PokemonViewState) = with(state) {
        if (pokemonList.isNotEmpty()) dismissEmptyMessage()
        showLoading(progress)
        pagingLoading(pagingProgress)
        if (pokemonList.isNotEmpty()) submitList(pokemonList)
        handleException(exception)
    }

    private fun dismissEmptyMessage() {
        emptyAdapter.visible = true
    }


    private fun setUpRecycler() = with(binding.contentScrolling.pokemonRecycler) {
        layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        adapter = concatAdapter

        addOnScrollListener(EndlessScrollListener(PokemonPagingListener(pagingPublisher)))


        addOnScrollListener(object : RecyclerView.OnScrollListener() {

            private val subject = PublishSubject.create<Map<Int, String>>()

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val linearLayout = recyclerView.layoutManager as LinearLayoutManager

                val firstVisibleItem = linearLayout.findFirstCompletelyVisibleItemPosition()

                var lastVisibleItem = linearLayout.findLastCompletelyVisibleItemPosition()

                if (lastVisibleItem == pokemonAdapter.itemCount) {
                    lastVisibleItem -= 1
                }

                if (newState == SCROLL_STATE_IDLE) {

                    val eventMap = (firstVisibleItem..lastVisibleItem).map {
                        it to pokemonAdapter.currentList[it]
                    }.toMap()
                        .filterValues { it.url != null && it.status != PokemonItemStatus.Updated }
                        .map { it.key to it.value.url!! }
                        .toMap()

                    if (eventMap.isNotEmpty()) {

                        subject.startWith(Observable.just(eventMap))
                            .debounce(1500, TimeUnit.MILLISECONDS)
                            .distinctUntilChanged()
                            .subscribe {
                                detailsPublisher.onNext(PokemonIntent.LoadDetails(
                                    it
                                ))
                            }
                    }

                }
            }
        })

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

    private fun loadDetails(): Observable<PokemonIntent.LoadDetails> =
        detailsPublisher

    override fun getIntentsStream(): Observable<PokemonIntent> {
        return Observable.merge(
            initIntent(),
            refreshIntent(),
            loadMoreIntent(),
            loadDetails()
        )
    }

    private fun showException(exception: Throwable) =
        showToast("${exception.message}")

    override fun onStart() {
        super.onStart()
        viewModel.presenter.bind(this)

    }

    override fun onStop() {
        viewModel.presenter.unbind()
        super.onStop()
    }

}
