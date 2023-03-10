package edu.ncku.application.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by ncku on 2017/4/12.
 */

public class ActivityInfo implements Serializable {
    Date StartTime;
    Date EndTime;
    String ImgUrl;
    String ActivityUrl;

    public ActivityInfo(){
        StartTime = new Date();
        EndTime = new Date();
        ImgUrl = "";
        ActivityUrl = "";
    }

    public ActivityInfo(Date startTime, Date endTime, String imgUrl, String activityUrl) {
        StartTime = startTime;
        EndTime = endTime;
        ImgUrl = imgUrl;
        ActivityUrl = activityUrl;
    }

    public Date getStartTime() {
        return StartTime;
    }

    public Date getEndTime() {
        return EndTime;
    }


    public String getImgUrl() {
        return ImgUrl;
    }


    public String getActivityUrl() {
        return ActivityUrl;
    }

}
