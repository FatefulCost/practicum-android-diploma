package ru.practicum.android.diploma.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.practicum.android.diploma.BuildConfig
import ru.practicum.android.diploma.data.network.ApiService
import ru.practicum.android.diploma.data.network.NetworkClient
import ru.practicum.android.diploma.data.network.AuthInterceptor
import java.util.concurrent.TimeUnit

val networkModule = module {
    single { provideGson() }
    single { provideAuthInterceptor() }
    single { provideOkHttpClient(get()) }
    single { provideRetrofit(get(), get()) }
    single { provideApiService(get()) }
    single { NetworkClient(get()) }
}

private const val TIMEOUT_SECONDS = 30L

private fun provideGson(): Gson {
    return GsonBuilder().setLenient().create()
}

private fun provideAuthInterceptor(): AuthInterceptor {
    return AuthInterceptor()
}

private fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
    val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    return OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
}

private fun provideRetrofit(gson: Gson, client: OkHttpClient): Retrofit {
    return Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}

private fun provideApiService(retrofit: Retrofit): ApiService {
    return retrofit.create(ApiService::class.java)
}
