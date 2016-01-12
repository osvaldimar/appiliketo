package com.mobile.iliketo.appiliketo.webcontent;

import android.content.Context;

import com.mobile.iliketo.appiliketo.dao.DBILiketo;
import com.mobile.iliketo.appiliketo.util.StrConstant;

/**
 * Created by OSVALDIMAR on 8/27/2015.
 */
public class DataHtml {

    public static String getIndexHtml(String action){

        String indexHtml = "<html>" +
                "<body>" +
                "<script type=\"text/javascript\">" +
                //"window.location.href = \"" + StrConstant.URL_ILIKETO + "\";"+
                "window.location.href = \"" + StrConstant.URL_ILIKETO + action + "\";"+
                "</script>" +
                "</body>" +
                "</html>";
        return indexHtml;
    }

    public static String getHtmlScriptNotifications(){

        String html = "<html>" +
                "<body>" +
                "<script type=\"text/javascript\">" +
                //"alert(\"teste script\");" +
                "ajaxNotificationsMobile();" +
                "function ajaxNotificationsMobile(){" +
                //"alert(\"teste metodo ajaxNotificationsMobile\");" +
                "$.ajax({type: \"post\",url: \"" + StrConstant.URL_NEWS_NOTIFICATIONS + "\"," +
                "success: function(jsonObject){AppILiketo.generateNotificationsMobile(JSON.stringify(jsonObject));}});" +
                "setTimeout(\"ajaxNotificationsMobile()\", 10000);}" +
                "</script>" +
                "</body>" +
                "</html>";
        return html;
    }

}
