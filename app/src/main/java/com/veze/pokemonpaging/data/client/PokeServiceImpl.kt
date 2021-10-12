package com.veze.pokemonpaging.data.client

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.veze.pokemonpaging.data.model.ApiResource
import com.veze.pokemonpaging.data.model.NamedApiResource
import com.veze.pokemonpaging.data.util.ApiResourceAdapter
import com.veze.pokemonpaging.data.util.NamedApiResourceAdapter
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

internal class PokeServiceImpl(
    private val config: ClientConfig
) : PokeService by Retrofit.Builder()
    .baseUrl(config.rootUrl)
    .addConverterFactory(
        GsonConverterFactory.create(
            GsonBuilder().apply {
                setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
//                registerTypeAdapter(
//                    TypeToken.get(ApiResource::class.java).type,
//                    ApiResourceAdapter()
//                )
//                registerTypeAdapter(
//                    TypeToken.get(NamedApiResource::class.java).type,
//                    NamedApiResourceAdapter()
//                )
            }.create()
        )
    )
    .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
    .client(OkHttpClient.Builder().(config.okHttpConfig)().build())
    .build()
    .create(PokeService::class.java)