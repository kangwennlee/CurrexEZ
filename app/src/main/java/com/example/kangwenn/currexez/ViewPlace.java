package com.example.kangwenn.currexez;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.kangwenn.currexez.MapModel.Common;
import com.example.kangwenn.currexez.MapModel.IGoogleAPIService;
import com.example.kangwenn.currexez.MapModel.Photos;
import com.example.kangwenn.currexez.MapModel.PlaceDetail;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ViewPlace extends AppCompatActivity {

    ImageView photo;
    RatingBar ratingBar;
    TextView opening_hours, place_Address, place_Name;
    Button btnViewOnMap;

    IGoogleAPIService service;
    PlaceDetail placeDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_place);

        service = Common.getGoogleAPIService();

        ratingBar = findViewById(R.id.ratingBar);

        opening_hours = findViewById(R.id.placeOpenHour);
        place_Address = findViewById(R.id.placeAddress);
        place_Name = findViewById(R.id.placeName);
        btnViewOnMap = findViewById(R.id.btnShowMap);

        photo = findViewById(R.id.placePhoto);

        place_Name.setText("");
        place_Address.setText("");
        opening_hours.setText("");

        //photo
        if (Common.currentResult.getPhotos() != null && Common.currentResult.getPhotos().length > 0)
        {
            Picasso.with(this)
                    .load(getPhotoOfPlace(Common.currentResult.getPhotos()[0].getPhoto_reference(),1000)) // because getPhotos() is array, so take first item
                    .placeholder(R.drawable.ic_image_black_24dp)
                    .error(R.drawable.ic_error_outline_black_24dp)
                    .into(photo);
        }

        //rating
        if (Common.currentResult.getRating() != null && !TextUtils.isEmpty(Common.currentResult.getRating())){
            ratingBar.setRating(Float.parseFloat(Common.currentResult.getRating()));
        }else {
            ratingBar.setVisibility(View.GONE);
        }

        //opening hours
        if (Common.currentResult.getOpening_hours() != null){
            if (Common.currentResult.getOpening_hours().getOpen_now().equals("true"))
            opening_hours.setText("Now Opening");
        }else {
            opening_hours.setVisibility(View.GONE);
        }

        //use Service to fetch Address and name
        service.getDetailPlaces(getPlaceDetailUrl(Common.currentResult.getPlace_id()))
                .enqueue(new Callback<PlaceDetail>() {
                    @Override
                    public void onResponse(Call<PlaceDetail> call, Response<PlaceDetail> response) {
                        placeDetail = response.body();

                        String test = placeDetail.getResult().getFormatted_address();
                        place_Address.setText(placeDetail.getResult().getFormatted_address());
                        place_Name.setText(placeDetail.getResult().getName());
                    }

                    @Override
                    public void onFailure(Call<PlaceDetail> call, Throwable t) {

                    }
                });


        btnViewOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(placeDetail.getResult().getUrl()));
                startActivity(mapIntent);
            }
        });
    }

    private String getPlaceDetailUrl(String place_id) {

        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/details/json?");
        url.append("placeid=" + place_id);
        url.append("&key=" + "AIzaSyCyOmbfQGcHs4DhRv9edOrAC-FnWG8iC3c");

        return url.toString();
    }

    private String getPhotoOfPlace(String photos, int maxWidth) {
        StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/place/photo?");
        url.append("maxwidth=" + maxWidth);
        url.append("&photoreference=" + photos);
        url.append("&key=" + "AIzaSyCyOmbfQGcHs4DhRv9edOrAC-FnWG8iC3c");

        return url.toString();
    }
}
