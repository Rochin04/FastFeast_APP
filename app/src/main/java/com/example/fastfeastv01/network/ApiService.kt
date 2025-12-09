package com.example.fastfeastv01.network
import android.util.Log
import com.example.fastfeastv01.data.ApiResponse
import com.example.fastfeastv01.data.CategoriaDto
import com.example.fastfeastv01.data.ComidaDto
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import java.util.concurrent.TimeUnit
import com.example.fastfeastv01.data.CategoriasResponse
import com.example.fastfeastv01.data.ComercianteRegisterRequest
import com.example.fastfeastv01.data.ComercianteResponseWrapper
import com.example.fastfeastv01.data.ComidasResponse
import com.example.fastfeastv01.data.CreatePlatilloRequest
import com.example.fastfeastv01.data.CreatePlatilloWrapper
import com.example.fastfeastv01.data.LoginRequest
import com.example.fastfeastv01.data.LoginResponse
import com.example.fastfeastv01.data.StudentRegisterRequest
import com.example.fastfeastv01.data.StudentResponseWrapper
import com.example.fastfeastv01.data.UserDetailResponse
import com.example.fastfeastv01.data.UserDetailWrapper
import com.example.fastfeastv01.data.UserListResponse
import com.example.fastfeastv01.data.UserRegisterRequest
import com.example.fastfeastv01.data.UserRegisterResponse
import com.example.fastfeastv01.data.UserResponseWrapper
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.http.Path

// 1. URL Base
private const val BASE_URL = "https://fastfeast-apiv2.onrender.com/api/v1/"


// 2. Configuración de Moshi
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// 3. Interceptor personalizado para logging
//private val loggingInterceptor = Interceptor { chain ->
//    val request = chain.request()
//    Log.d("OkHttp", "Request: ${request.method} ${request.url}")
//    val response = chain.proceed(request)
//    Log.d("OkHttp", "Response: ${response.code}")
//    response
//}
private val loggingInterceptor = HttpLoggingInterceptor().apply {
    // Level.BODY imprime todo: Cabeceras y el JSON de respuesta
    level = HttpLoggingInterceptor.Level.BODY
}

// 4. Configuración de OkHttpClient con timeouts y logging
private val httpClient = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .addInterceptor(loggingInterceptor)
    .build()

// 5. Configuración de Retrofit
private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(httpClient)
    .build()

// 4. Interfaz de Endpoints
interface FastFeastApiService {
    @GET("comidas")
    suspend fun getComidas(): ComidasResponse
    @GET("categories")
    suspend fun getCategorias(): CategoriasResponse
    @POST("usuarios")
    suspend fun registerUser(@Body request: UserRegisterRequest): UserResponseWrapper
    @POST("comerciantes")
    suspend fun registrarComerciante(@Body request: ComercianteRegisterRequest): ComercianteResponseWrapper
    @POST("estudiantes") // Verifica si la ruta es "estudiantes" o "students"
    suspend fun registrarEstudiante(@Body request: StudentRegisterRequest): StudentResponseWrapper
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    // 2. Endpoint para obtener datos extras del usuario (si el login no devolviera el tipo)
    @GET("usuarios/{id}")
    suspend fun getUserById(@Path("id") userId: String): UserDetailWrapper
    @POST("comidas")
    suspend fun createPlatillo(@Body request: CreatePlatilloRequest): CreatePlatilloWrapper
    @GET("comerciantes/{id}")
    suspend fun getMerchantByOwnerId(@Path("id") ownerId: String): ComercianteResponseWrapper
}

// 5. Singleton de acceso público
object Api {
    val retrofitService: FastFeastApiService by lazy {
        retrofit.create(FastFeastApiService::class.java)
    }
}