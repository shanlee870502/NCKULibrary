package edu.ncku.application.model;


public class Occupancy {

    private String title;
    private String subtitle;
    private String manage_dept;
    private String contact;
    private int percentage;
    private double lat;
    private double lng;

    public Occupancy(double percentage, String title, String subtitle, String manage_dept, String contact, double lat, double lng) { //int titleId
        this.title = title;
        this.subtitle = subtitle;
        this.manage_dept = manage_dept;
        this.contact = contact;
        this.percentage = (int)(percentage*100);
        this.lat = lat;
        this.lng = lng;
    }

    public String getTitle(){
        return title;
    }
    public String getSubtitle() {
        return subtitle;
    }
    public String getManage_dept() {return manage_dept;}
    public String getContact() {return contact;}
    public int getPercentage() {
        if (percentage>100){
            percentage=100;
        }
        return percentage;
    }
    public Double getLat(){return lat;}
    public Double getLng(){return lng;}
}
