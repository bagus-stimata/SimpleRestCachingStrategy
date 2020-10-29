package com.example.simplerestcachingstrategy.config;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.toolbox.DiskBasedCache;
import com.example.simplerestcachingstrategy.AppConfig;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Dispatcher;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiRetrofitClient {
//    public static final String BASE_URL = AppConfig.BASE_URL;
    public static OkHttpClient okHttpClient = null;
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Context context) {
        if (retrofit==null) {
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();

            /**
             * 1. Simple
             */
//            OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                    .addInterceptor(interceptor.setLevel(HttpLoggingInterceptor.Level.BODY))
//                    .connectTimeout(2, TimeUnit.MINUTES)
//                    .writeTimeout(2, TimeUnit.MINUTES)
//                    .readTimeout(2, TimeUnit.MINUTES)
//                    .build();

            int cacheSize = 10 * 1024 * 1024; // 10 MiB
            Cache cache = new Cache(context.getCacheDir(), cacheSize);

            /**
             * 2. With Changing Method
             */
            okHttpClient = new OkHttpClient.Builder()
                    .cache(cache)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public okhttp3.Response intercept(Interceptor.Chain chain)
                                throws IOException {
                            Request request = chain.request();
                            if (!isNetworkAvailable(context)) {
//                                int maxAge = 60 * 60 * 24 * 30; // Offline cache available for 30 days
                                int maxAge =15; // Offline cache available for 15 detik (TEST BOS)
                                request = request
                                        .newBuilder()
                                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxAge)
//                                        .removeHeader("Pragma")
                                        .build();
                            }
                            return chain.proceed(request);
                        }
                    })
                    .addNetworkInterceptor(new Interceptor() {
                        @Override
                        public okhttp3.Response intercept(Interceptor.Chain chain)
                                throws IOException {
                            Request request = chain.request();
                            if (!isNetworkAvailable(context)) {
                                int maxAge = 60; // read from cache for 60 seconds even if there is internet connection
                                request = request
                                        .newBuilder()
                                        .header("Cache-Control", "public, only-if-cached, max-stale=" + maxAge)
//                                        .removeHeader("Pragma")
                                        .build();
                            }
                            return chain.proceed(request);
                        }
                    })
                    .addInterceptor(interceptor.setLevel(HttpLoggingInterceptor.Level.BODY))
                    .connectTimeout(2, TimeUnit.MINUTES)
                    .writeTimeout(2, TimeUnit.MINUTES)
                    .readTimeout(2, TimeUnit.MINUTES)
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(AppConfig.BASE_URL) //Base Url disediakan disini
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(okHttpClient)
                    .build();
        }

        return retrofit;
    }


    private static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}
