package com.veze.pokemonpaging.ui.main

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.veze.pokemonpaging.R
import com.veze.pokemonpaging.databinding.EmptyBinding
import com.veze.pokemonpaging.util.inflateView

class EmptyAdapter :
    RecyclerView.Adapter<EmptyAdapter.EmptyViewHolder>() {

    var hide = false
        set(value) {
            if (field != value) {
                when {
                    field -> notifyItemInserted(0)
                    value -> notifyItemRemoved(0)
                }
                field = value
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EmptyViewHolder {
        val binding = EmptyBinding.bind(parent.inflateView(R.layout.empty))
        return EmptyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EmptyViewHolder, position: Int) {
    }

    override fun getItemCount(): Int = if (hide) 0 else 1

    class EmptyViewHolder(binding: EmptyBinding) :
        RecyclerView.ViewHolder(binding.root)
}