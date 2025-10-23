package gg.tropic.practice.messaging

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface PlayerCountService
{
    @GET("player-counts/{id}")
    fun getPlayerCount(@Path("id") server: String): Call<Int>

    @POST("player-counts/{id}/update")
    fun setPlayerCount(@Path("id") server: String, @Query("count") playerCount: Int): Call<String>
}