package com.dream.lemon.hackathon.data.source;

import com.dream.lemon.hackathon.data.Parking;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public class ParkingRepository implements ParkingDataSource {

    public interface GitHubService {
        @GET("/GetData/GetData?dataset=om:PlazaMovilidadReducida&format=json")
        Call<List<Parking>> listParkings();
    }



}
