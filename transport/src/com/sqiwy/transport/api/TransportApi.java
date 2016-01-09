/**
 * Created by abrysov
 */
package com.sqiwy.transport.api;

import java.util.List;

import retrofit.Callback;
import retrofit.http.*;

public interface TransportApi {
    @FormUrlEncoded
    @POST("/api/v1/register")
    void register(@Field("number") String number, Callback<RegisterResponse> cb);
     
    @POST("/api/v1/ads_report")
    void sendReports(@Query("key") String key, @Body List<AdReport> reports, Callback<Void> onResponse);

    @GET("/api/v1/route")
    GetRouteResponse getRoute(@Query("key") String key);

    @GET("/api/v1/test_route")
    GetTestRouteResponse getTestRoute(@Query("key") String key);
    
    @GET("/api/v1/ads")
    GetAdsResponse getAds(@Query("key") String key);

    @GET("/api/v1/news")
    GetNewsResponse getNews(@Query("key") String key, @Query("date") long date);

    @GET("/api/v1/horoscope")
    GetHoroscopeResponse getHoroscope(@Query("key") String key);

    @GET("/api/v1/currency")
    GetCurrencyResponse getCurrency(@Query("key") String key);

    @POST("/api/v1/iamalive")
    PingResponse ping(@Query("key") String key, @Body List<GeoPoint> locations);
}
