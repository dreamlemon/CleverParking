
package com.dream.lemon.hackathon.pojosJSON;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import retrofit2.Call;
import retrofit2.http.GET;

public class ParkingJSON {

    public interface ParkingService {
        @GET("/GetData/GetData?dataset=om:PlazaMovilidadReducida&format=json")
        Call<List<ParkingJSON>> listParkingJson();
    }

    @SerializedName("bindings")
    @Expose
    private List<Binding> bindings = null;

    public List<Binding> getBindings() {
        return bindings;
    }

    public void setBindings(List<Binding> bindings) {
        this.bindings = bindings;
    }
}
