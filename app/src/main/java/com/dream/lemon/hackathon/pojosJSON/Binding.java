
package com.dream.lemon.hackathon.pojosJSON;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public class Binding {

    @SerializedName("uri")
    @Expose
    private Uri uri;
    @SerializedName("rdfs_comment")
    @Expose
    private RdfsComment rdfsComment;
    @SerializedName("geo_long")
    @Expose
    private GeoLong geoLong;
    @SerializedName("geo_lat")
    @Expose
    private GeoLat geoLat;
    @SerializedName("om_situadoEnVia")
    @Expose
    private OmSituadoEnVia omSituadoEnVia;
    @SerializedName("rdfs_label")
    @Expose
    private RdfsLabel rdfsLabel;
    @SerializedName("enlacesSIG")
    @Expose
    private EnlacesSIG enlacesSIG;

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public RdfsComment getRdfsComment() {
        return rdfsComment;
    }

    public void setRdfsComment(RdfsComment rdfsComment) {
        this.rdfsComment = rdfsComment;
    }

    public GeoLong getGeoLong() {
        return geoLong;
    }

    public void setGeoLong(GeoLong geoLong) {
        this.geoLong = geoLong;
    }

    public GeoLat getGeoLat() {
        return geoLat;
    }

    public void setGeoLat(GeoLat geoLat) {
        this.geoLat = geoLat;
    }

    public OmSituadoEnVia getOmSituadoEnVia() {
        return omSituadoEnVia;
    }

    public void setOmSituadoEnVia(OmSituadoEnVia omSituadoEnVia) {
        this.omSituadoEnVia = omSituadoEnVia;
    }

    public RdfsLabel getRdfsLabel() {
        return rdfsLabel;
    }

    public void setRdfsLabel(RdfsLabel rdfsLabel) {
        this.rdfsLabel = rdfsLabel;
    }

    public EnlacesSIG getEnlacesSIG() {
        return enlacesSIG;
    }

    public void setEnlacesSIG(EnlacesSIG enlacesSIG) {
        this.enlacesSIG = enlacesSIG;
    }

}
