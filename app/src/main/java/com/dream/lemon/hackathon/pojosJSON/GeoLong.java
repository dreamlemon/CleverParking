
package com.dream.lemon.hackathon.pojosJSON;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class GeoLong {

    @SerializedName("datatype")
    @Expose
    private String datatype;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("value")
    @Expose
    private String value;

    public String getDatatype() {
        return datatype;
    }

    public void setDatatype(String datatype) {
        this.datatype = datatype;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
