package com.hongule.stationalarm.data;

public class location_line_data {
    public String line = "";
    public String name = "";
    public String name_no = "";
    public Double lot =0.0;
    public Double lon =0.0;

    public location_line_data(String line, String name, String name_no, double lot, double lon) {
        this.line = line;
        this.name = name;
        this.name_no = name_no;
        this.lot =lot;
        this.lon =lon;
    }
}
