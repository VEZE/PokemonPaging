package com.veze.pokemonpaging.ui.main

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.veze.pokemonpaging.R
import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.data.model.PokemonItemStatus
import com.veze.pokemonpaging.util.inflateView
import io.reactivex.rxjava3.subjects.PublishSubject


class PokemonAdapter(
    private val detailsPublisher: PublishSubject<PokemonIntent.LoadDetails>,
    private val onClick: (Pokemon) -> Unit,
) :
    ListAdapter<Pokemon, PokemonAdapter.PokemonViewHolder>(PokemonDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = parent.inflateView(R.layout.pokemon_item)
        return PokemonViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class PokemonViewHolder(itemView: View, val onClick: (Pokemon) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val pokemonImageView: ImageView = itemView.findViewById(R.id.pokemon_image)
        private val pokemonNameView: TextView = itemView.findViewById(R.id.pokemon_name)
        private val retry: Button = itemView.findViewById(R.id.retryItem)
        private var currentPokemon: Pokemon? = null

        init {
            itemView.setOnClickListener {
                currentPokemon?.let {
                    onClick(it)
                }
            }
        }

        fun bind(pokemon: Pokemon, position: Int) {
            println("state = ${pokemon.status}")

            currentPokemon = pokemon

            pokemonNameView.text = pokemon.name

            retry.isVisible =
                pokemon.status == PokemonItemStatus.Empty || pokemon.status == PokemonItemStatus.Error


            when (pokemon.status) {
                PokemonItemStatus.Empty -> detailsPublisher.onNext(PokemonIntent.LoadDetails(pokemon.url
                    ?: "", position))
            }

            retry.setOnClickListener {
                if (pokemon.url != null)
                    updateRequest(pokemon.url, position)
            }

            val options: RequestOptions = RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.item_loading)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .priority(Priority.HIGH)
                .dontTransform()

            Glide.with(itemView.context).load(pokemon.sprites?.frontDefault).apply(options)
                .into(pokemonImageView)

        }

        fun updateRequest(pokemonUrl: String, position: Int, afterError: Boolean = false) {
            detailsPublisher.onNext(PokemonIntent.LoadDetails(pokemonUrl, position))
        }
    }
}

object PokemonDiffCallback : DiffUtil.ItemCallback<Pokemon>() {
    override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
        return oldItem.status == newItem.status && oldItem.name == newItem.name
    }
}
