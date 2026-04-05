package com.example.myapplication.Model;

public class PoolInfo {
    private String userId;
    private String poolType;
    private Double width;
    private Double depth;
    private Double length;
    private String unit;

    public PoolInfo(String userId, String poolType,
                    Double width, Double depth, Double length, String unit) {
        this.userId   = userId;
        this.poolType = poolType;
        this.width    = width;
        this.depth    = depth;
        this.length   = length;
        this.unit     = unit;
    }

    public String getUserId()   { return userId; }
    public String getPoolType() { return poolType; }
    public Double getWidth()    { return width; }
    public Double getDepth()    { return depth; }
    public Double getLength()   { return length; }
    public String getUnit()     { return unit; }
}