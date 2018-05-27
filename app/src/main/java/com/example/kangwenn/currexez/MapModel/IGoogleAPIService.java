package com.example.kangwenn.currexez.MapModel;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface IGoogleAPIService {

    @GET
    Call<MyPlace> getNearByPlaces(@Url String url);
}
