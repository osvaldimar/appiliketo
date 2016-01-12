package com.mobile.iliketo.appiliketo.model;

/**
 * Created by OSVALDIMAR on 8/30/2015.
 */
public class ContentNotification {

    private String descriptionTitle;
    private String descriptionText;
    private String descriptionStyleLine;
    private String urlNotification;

    public ContentNotification(){

    }


    public String getDescriptionTitle() {
        return descriptionTitle;
    }

    public void setDescriptionTitle(String descriptionTitle) {
        this.descriptionTitle = descriptionTitle;
    }

    public String getDescriptionText() {
        return descriptionText;
    }

    public void setDescriptionText(String descriptionText) {
        this.descriptionText = descriptionText;
    }

    public String getDescriptionStyleLine() {
        return descriptionStyleLine;
    }

    public void setDescriptionStyleLine(String descriptionStyleLine) {
        this.descriptionStyleLine = descriptionStyleLine;
    }

    public String getUrlNotification() {
        return urlNotification;
    }

    public void setUrlNotification(String urlNotification) {
        this.urlNotification = urlNotification;
    }
}
