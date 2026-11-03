package cl.louaraya.macrame.data.remote

import cl.louaraya.macrame.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

/** Endpoints REST del backend centralizado (catálogo y solicitudes). */
interface MacrameApi {
    @GET("productos")
    suspend fun getProducts(): List<ProductDto>

    @POST("productos")
    suspend fun createProduct(@Body product: ProductDto): ProductDto

    @PUT("productos/{id}")
    suspend fun updateProduct(@Path("id") id: Long, @Body product: ProductDto): ProductDto

    @POST("pedidos")
    suspend fun createOrder(@Body order: OrderDto): OrderDto

    @PUT("pedidos/{id}")
    suspend fun updateOrder(@Path("id") id: Long, @Body order: OrderDto): OrderDto
}

object ApiClient {
    val api: MacrameApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
                }
            }
            .build()
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MacrameApi::class.java)
    }
}
