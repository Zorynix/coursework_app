package com.example.coursework.data

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.orhanobut.logger.Logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    fun provideClient(session: CourseWorkSession, @ApplicationContext context: Context): OkHttpClient {
        val client = OkHttpClient.Builder()
        client.addInterceptor { chain ->
            val token = session.getToken()
            val packageName = context.packageName
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("X-Package-Name", packageName)
                .build()
            Logger.t("OkHttpClient").d("Request headers: Authorization=Bearer $token, X-Package-Name=$packageName")
            chain.proceed(request)
        }
        client.addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            },
        )
        return client.build()
    }

    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        val baseUrl = "http://109.68.214.122"
        val retrofit = Retrofit.Builder()
            .client(client)
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        Logger.t("Retrofit").d("Retrofit initialized with baseUrl: $baseUrl")
        return retrofit
    }

    @Provides
    fun provideFoodApi(retrofit: Retrofit): FoodApi {
        return retrofit.create(FoodApi::class.java)
    }

    @Provides
    fun provideSession(@ApplicationContext context: Context): CourseWorkSession {
        return CourseWorkSession(context)
    }

    @Provides
    fun provideLocationService(@ApplicationContext context: Context): FusedLocationProviderClient {
        return LocationServices.getFusedLocationProviderClient(context)
    }
}
