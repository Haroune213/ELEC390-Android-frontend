package com.example.myapplication.Model;

public class UpdatePreferencesRequest {
    private Double tempMin, tempMax;
    private Double phMin, phMax;
    private Double tdsMin, tdsMax;
    private Double depthMin, depthMax;

    public UpdatePreferencesRequest(Double tempMin, Double tempMax,
                                    Double phMin, Double phMax,
                                    Double tdsMin, Double tdsMax,
                                    Double depthMin, Double depthMax) {
        this.tempMin = tempMin; this.tempMax = tempMax;
        this.phMin = phMin;     this.phMax = phMax;
        this.tdsMin = tdsMin;   this.tdsMax = tdsMax;
        this.depthMin = depthMin; this.depthMax = depthMax;
    }

    public Double getTempMin() { return tempMin; }
    public Double getTempMax() { return tempMax; }
    public Double getPhMin() { return phMin; }
    public Double getPhMax() { return phMax; }
    public Double getTdsMin() { return tdsMin; }
    public Double getTdsMax() { return tdsMax; }
    public Double getDepthMin() { return depthMin; }
    public Double getDepthMax() { return depthMax; }
}