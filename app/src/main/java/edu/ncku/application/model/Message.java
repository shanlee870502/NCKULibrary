package edu.ncku.application.model;

/**
 * Created by NCKU on 2016/3/8.
 */
public class Message extends News {

    private boolean isRead;

    public Message(String title, int pubTime, String contents) {
        super(title, "NULL", pubTime, pubTime, contents);
    }
    public Message(String title, int pubTime, String contents, boolean isRead) {
        super(title, "NULL", pubTime, pubTime, contents);
        this.isRead = isRead;
    }
    public boolean getIsRead() {
        return this.isRead;
    }
    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}
