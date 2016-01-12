package com.mobile.iliketo.appiliketo.notifications;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.mobile.iliketo.appiliketo.R;
import com.mobile.iliketo.appiliketo.activities.MainActivity;
import com.mobile.iliketo.appiliketo.model.ContentNotification;
import com.mobile.iliketo.appiliketo.util.StrConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NotificationsILiketo {


	public void executeNotification(Context context){

//		List<ContentNotification> list = new ArrayList<ContentNotification>();
//		try {
//
//			Log.i(StrConstant.TAG_LOGCAT, "Metodo executeNotification - " + this.getClass().getName());
//
//			String responseText = HttpConnectionServer.getNewsNotifications();
//			Log.i(StrConstant.TAG_LOGCAT, "responseText: " + responseText);
//
//			JSONArray array = new JSONArray(responseText);
//			for(int i = 0; i < array.length(); i++){
//				String msgNotific = array.getJSONObject(i).getString("msg");
//				ContentNotification cn = new ContentNotification();
//				cn.setDescriptionText(msgNotific);
//				list.add(cn);
//			}
//
//		} catch (JSONException e) {
//			e.printStackTrace();
//		}
//
//		if(!list.isEmpty()) {
//			generateNotifications(context, list);
//		}
	}

	public void generateNotifications(Context context, List<ContentNotification> list){

		Log.i(StrConstant.TAG_LOGCAT, "Metodo - generateNotification");

		NotificationManager nm = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
		Intent intent = new Intent(context, MainActivity.class);
		//intent.putExtra(StrConstant.URL_NOTIFIC, cn.getUrlNotification());
		PendingIntent pi = PendingIntent.getActivity(context, 0, intent, 0);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
		builder.setTicker(StrConstant.MSG_NOTIFIC_ILIKETO);
		builder.setContentTitle(StrConstant.ILT);
		if(list.size() == 1) {
			builder.setContentText(list.get(0).getDescriptionText());
		}else{
			builder.setContentText(list.size() + " Notifications");
		}
		builder.setSmallIcon(R.drawable.logo_ilt_transparente);
		builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.logo_ilt));
		builder.setContentIntent(pi);

		NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
		builder.setStyle(getStyleNotification(list));

		android.app.Notification n = builder.build();
		n.vibrate = new long[]{150, 300, 150, 600};
		n.flags = android.app.Notification.FLAG_AUTO_CANCEL;
		nm.notify(R.drawable.logo_ilt, n);

		try{
			Uri som = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			Ringtone toque = RingtoneManager.getRingtone(context, som);
			toque.play();
		}catch(Exception e){
		}

	}


	private NotificationCompat.InboxStyle getStyleNotification(List<ContentNotification> list){
		NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
//		String [] descs = new String[]{"Descricao 1 abcdefghijklmnopqrstuvxzabcdefghijklmnopqrstuvxzabcdefghijklmnopqrstuvxz",
//				"Descricao 2 abcdefghijklmnopqrstuvxzabcdefghijklmnopqrstuvxzabcdefghijklmnopqrstuvxz",
//				"Descricao 3",
//				"Descricao 4",
//				"Descricao 5",
//				"Descricao 6",
//				"8 notificacoes"};
		for(int i = 0; i < list.size(); i++){
			if(i >= 7){ break; }
			style.addLine(list.get(i).getDescriptionText());
		}
		style.setSummaryText(list.size() + (list.size()==1 ? " Notification": " Notifications"));
		return (style);
	}
}
