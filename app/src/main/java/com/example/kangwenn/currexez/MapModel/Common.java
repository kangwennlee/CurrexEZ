package com.example.kangwenn.currexez.MapModel;

public class Common {
    private static final String GOOGLE_API_URL = "https://maps.googleapis.com/";

    public static Results currentResult;

    public static IGoogleAPIService getGoogleAPIService(){
        return RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService.class);
    }
}
