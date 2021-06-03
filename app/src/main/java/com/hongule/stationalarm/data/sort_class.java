package com.hongule.stationalarm.data;

public class sort_class {
    private String name;
    private Double point;

    public sort_class(String name, double point) {
        this.name = name;
        this.point = point;
    }

    public String getName() {
        return this.name;
    }

    public Double getPoint() {
        return point;
    }

    @Override
    public String toString() {
        return "[name=" + name + ", point=" + point + "]";
    }

}
