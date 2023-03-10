package edu.ncku.application.model;


import android.util.Pair;

public class Occupancy {

    private String name_id;
    private String title;
    private int cur_occupancy;
    private int total_occupancy;
    private String manage_dept;
    private String contact;
    private int percentage;
    private Pair<Double, Double> lat_lng;


    public Occupancy(String name_id, String[] title_manage_lst,int cur_occupancy, int total_occupancy, String contact,Pair<Double, Double> lat_lng) { //int titleId
        this.name_id = name_id;
        this.title = title_manage_lst[0];
        this.cur_occupancy = cur_occupancy;
        this.total_occupancy = total_occupancy;
        this.manage_dept = title_manage_lst[1];
        this.contact = contact;
        this.lat_lng = lat_lng;
    }
    public String getNameID(){
        return name_id;
    }
    public String getTitle(){
        return title;
    }

    public String getCurOccupancy() {
        return String.valueOf(cur_occupancy);
    }

    public void setCurOccupancy(int number){
        this.cur_occupancy = number;
    }
    public String getTotalOccupancy(){
        return String.valueOf(total_occupancy);
    }
    public void setTotalOccupancy(int number){
        this.total_occupancy = number;
    }
    public String getManageDept() {
        return manage_dept;
    }

    public String getContact() { return contact; }
    public int getPercentage() {
        percentage = (cur_occupancy*100/total_occupancy);
        if (percentage>100){
            percentage=100;
        }
        return percentage;
    }
    public Pair<Double, Double> getLatLng(){ return lat_lng;}
}
