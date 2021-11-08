package com.veze.pokemonpaging.ui.main

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.veze.pokemonpaging.R
import com.veze.pokemonpaging.data.model.Pokemon
import com.veze.pokemonpaging.util.inflateView

class PokemonAdapter(private val onClick: (Pokemon) -> Unit) :
    ListAdapter<Pokemon, PokemonAdapter.PokemonViewHolder>(PokemonDiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonViewHolder {
        val view = parent.inflateView(R.layout.pokemon_item)
        return PokemonViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: PokemonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PokemonViewHolder(itemView: View, val onClick: (Pokemon) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val pokemonImageView: ImageView = itemView.findViewById(R.id.pokemon_image)
        private val pokemonNameView: TextView = itemView.findViewById(R.id.pokemon_name)
        private var currentPokemon: Pokemon? = null

        init {
            itemView.setOnClickListener {
                currentPokemon?.let {
                    onClick(it)
                }
            }
        }

        fun bind(pokemon: Pokemon) {
            currentPokemon = pokemon

            pokemonNameView.text = pokemon.name

            if (pokemon.sprites.frontDefault != null) {
                Glide.with(itemView.context).load(pokemon.sprites.frontDefault)
                    .into(pokemonImageView)
            } else {
                //smth else
            }
        }
    }
}

object PokemonDiffCallback : DiffUtil.ItemCallback<Pokemon>() {
    override fun areItemsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Pokemon, newItem: Pokemon): Boolean {
        return oldItem == newItem
    }
}
