package com.umutsibara.chess_app

import com.google.gson.annotations.SerializedName
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// --- VERİ MODELLERİ ---
// Sunucudan gelen başarılı veya hatalı cevapları bu model karşılayacak.
data class FenResponse(
    @SerializedName("fen")
    val fen: String?,

    @SerializedName("error")
    val error: String?
)


// --- RETROFIT API ARAYÜZÜ ---
// Sunucuya hangi formatta istek atacağımızı burada tanımlıyoruz.
interface FenApiService {
    @Multipart
    @POST("predict")
    suspend fun uploadImage(
        @Part file: MultipartBody.Part
    ): Response<FenResponse>
}


// --- RETROFIT NESNESİNİ OLUŞTURAN YARDIMCI OBJE ---
// Bu obje, Retrofit'i tek bir yerden yönetmemizi sağlar.
object RetrofitClient {
    // BURADAKİ IP ADRESİNİ KENDİ SUNUCUNUZUN IP ADRESİYLE DEĞİŞTİRİN!
    private const val BASE_URL = "http://192.168.1.24:5000/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    val api: FenApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(FenApiService::class.java)
    }
}