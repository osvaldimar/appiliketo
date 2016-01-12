package com.mobile.iliketo.appiliketo.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.mobile.iliketo.appiliketo.dao.DBILiketo;
import com.mobile.iliketo.appiliketo.model.ContentNotification;
import com.mobile.iliketo.appiliketo.model.User;
import com.mobile.iliketo.appiliketo.notifications.NotificationsILiketo;
import com.mobile.iliketo.appiliketo.util.StrConstant;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OSVALDIMAR on 9/7/2015.
 */
public class ILiketoService extends Service {

    private Handler handler = new Handler();
    private String LAST_SEEN_DATE = "";
    private int TOTAL_NOTIFICATIONS = 0;
    private static boolean LOGIN_SERVICE = false;
    private static boolean IS_RUN = true;
    final Context context = this;
    final int[] processos = new int[1];
    private boolean sessaoExpirada = false;
    private boolean gerarNotificacao = true;

    private final IBinder meuBinder = new LocalBinder();


    public void setGerarNotificacao(boolean gerar){
        this.gerarNotificacao = true;
    }
    public int getTotalNotifications() {
        return TOTAL_NOTIFICATIONS;
    }
    public void setTotalNotifications(int total) {
        TOTAL_NOTIFICATIONS = total;
    }

    /**
     * Metodo retorna uma classe interna Binder do service
     */
    public class LocalBinder extends Binder {
        public ILiketoService getService() {
            // Return this instance of ILiketoService so clients can call public methods
            return ILiketoService.this;
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return meuBinder;
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.i(StrConstant.TAG_LOGCAT, "ILiketoService - metodo onCreate()");
//        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
//        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);
//        params.gravity = Gravity.TOP | Gravity.LEFT;
//        params.x = 0;
//        params.y = 0;
//        params.width = 0;
//        params.height = 0;
//
//        LinearLayout view = new LinearLayout(this);
//        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
//
//        wv = new WebViewILiketo(this);
//        wv.getSettings().setJavaScriptEnabled(true);
//        wv.addJavascriptInterface(this, "AppILiketo");
//        wv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
//        view.addView(wv);
//        //load html
//        wv.loadUrl(StrConstant.URL_ILIKETO + "/page.jsp?id=879");//page 'Script Notifications for Mobile'     "/page.jsp?id=869"); //'invalid page'

    }

    @JavascriptInterface
    public void generateNotificationsMobileService(String responseText){
        Log.i(StrConstant.TAG_LOGCAT, "ILiketoService - javascript metodo generateNotificationsMobile | responseText: " + responseText);
        LOGIN_SERVICE = true;
        List<ContentNotification> list = new ArrayList<ContentNotification>();
        try {
            JSONArray array = new JSONArray(responseText);
            String total = array.getJSONObject(0).getString("total");
            String data = array.getJSONObject(1).getString("lastSeenDate");
            if(data.equalsIgnoreCase(this.LAST_SEEN_DATE) && this.TOTAL_NOTIFICATIONS == Integer.parseInt(total) && !gerarNotificacao){
                Log.i(StrConstant.TAG_LOGCAT, "ILiketoService - Metodo generateNotificationsMobile Nao gerou notific Total: " + total + " - Date: " + data);
                return;
            }else {
                Log.i(StrConstant.TAG_LOGCAT, "ILiketoService - Metodo generateNotificationsMobile, Gerou notific Total: " + total + " - Date: " + data);
                for (int i = 2; i < array.length(); i++) {    //inicia contador com 2, pois o 0 eh total de notificacoes e 1 contem data ultimo visto
                    String msgNotific = array.getJSONObject(i).getString("msg");
                    ContentNotification cn = new ContentNotification();
                    cn.setDescriptionText(msgNotific);
                    list.add(cn);
                }
                this.TOTAL_NOTIFICATIONS = (Integer.parseInt(total));
                this.LAST_SEEN_DATE = data;
                this.gerarNotificacao = false;
            }
        } catch (JSONException e) {
            //e.printStackTrace();
            Log.e(StrConstant.TAG_LOGCAT, "ILiketoService - error JSONException");
            Log.i(StrConstant.TAG_LOGCAT, "ILiketoService - Sessao Expirada!");     //erro acesso ao sistema
            sessaoExpirada = true;
        }
        if(!list.isEmpty()) {
            //valida se usuario ja recebeu a mesma notificacao, se for diferente lanca nova notificacao
            NotificationsILiketo n = new NotificationsILiketo();
            n.generateNotifications(context, list);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        Log.i(StrConstant.TAG_LOGCAT, "ILiketoService - metodo onStartCommand() - startId: " + startId);
        processos[0] = startId;

        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.x = 0;
        params.y = 0;
        params.width = 0;
        params.height = 0;

        LinearLayout view = new LinearLayout(this);
        view.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

        final WebView wv = new WebView(this);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.addJavascriptInterface(this, "AppILiketo");
        wv.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        wv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        view.addView(wv);

        new Thread() {
            @Override
            public void run() {
                try {
                    Log.i(StrConstant.TAG_LOGCAT, "ILiketoService - Thread run - startId: " + processos[0]);
                    //LOGIN_SERVICE = false;
                    /**
                     handler.post(new Runnable() {
                    @Override public void run() {
                    wv.loadUrl(StrConstant.URL_ILIKETO + "/page.jsp?id=879");//page 'Script Notifications for Mobile'     "/page.jsp?id=869"); //'invalid page'
                    }
                    });
                     Thread.sleep(25000);
                     if(!LOGIN_SERVICE){
                     Log.i("LOG ILIKETO", "ILiketoService - LOGIN_SERVICE=" + LOGIN_SERVICE);
                     handler.post(new Runnable() {
                    @Override public void run() {
                    String dados = getDadosUsuario();
                    if(dados != null) {
                    wv.loadUrl(StrConstant.URL_ILIKETO + dados);
                    }else{
                    Log.i("LOG ILIKETO", "ILiketoService - Nao foi ativado o login");
                    }
                    }
                    });
                     }
                     */
                    while (IS_RUN) {
                        Log.i(StrConstant.TAG_LOGCAT, "ILiketoService - Thread run - startId: " + processos[0]);
                        if (verificaLogin()) {
                            Log.i("LOG ILIKETO", "ILiketoService - Login ativado OK!");
                            if(sessaoExpirada){
                                //força login na webview do service
                                Log.i("LOG ILIKETO", "ILiketoService - Tentando fazer login...!");
                                if (verificaConexao()) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            wv.loadUrl(StrConstant.URL_ILIKETO + getDadosUsuario());   //atualiza pagina script notificacao
                                        }
                                    });
                                } else {
                                    Log.i(StrConstant.TAG_LOGCAT, "ILiketoService - No Internet!");
                                }
                                sessaoExpirada = false;
                                LOGIN_SERVICE = true;
                                gerarNotificacao = true;
                                Thread.sleep(25000);
                                Log.i(StrConstant.TAG_LOGCAT, "ILiketoService - Sessao OK!");
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        wv.loadUrl(StrConstant.URL_ILIKETO + "/page.jsp?id=879");   //atualiza pagina script notificacao
                                    }
                                });
                            }
                            if (!LOGIN_SERVICE) {
                                if (verificaConexao()) {
                                    //internet ok
                                    Log.i(StrConstant.TAG_LOGCAT, "ILiketoService - Internet OK!");
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            wv.loadUrl(StrConstant.URL_ILIKETO + "/page.jsp?id=879");   //atualiza pagina script notificacao
                                        }
                                    });
                                } else {
                                    Log.i(StrConstant.TAG_LOGCAT, "ILiketoService - No Internet!");
                                }
                            }
                        } else {
                            Log.i("LOG ILIKETO", "ILiketoService - Nao foi ativado o login");
                            onDestroy();
                        }
                        LOGIN_SERVICE = false;
                        Thread.sleep(55000);
                    }
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
        }.start();

        return super.onStartCommand(intent, flags, startId);
    }

    private boolean verificaLogin(){
        int usuarioDefault = 1;
        DBILiketo db = new DBILiketo(this);
        User user = db.readById(usuarioDefault);
        if(user != null){
            //existe usuario, valida se ta ativado login
            if(user.getAtivado() == 1) {
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    private String getDadosUsuario(){

        String action = "";
        int usuarioDefault = 1;
        DBILiketo db = new DBILiketo(this);
        User user = db.readById(usuarioDefault);
        if(user != null){
            //existe usuario, valida se ta ativado login
            if(user.getAtivado() == 1) {
                action = "/login_post.jsp?username=" + user.getUsername() + "&password=" + user.getPassword();
                Log.i("LOG ILIKETO", "Entrou Service Android mobile... | usuario: " + user.getUsername());
            }else{
                Log.i("LOG ILIKETO", "ILiketoService - Nao foi ativado o login, usario ou senha invalidos... | usuario: " + user.getUsername());
                return null;
            }
        }else{
            Log.i("LOG ILIKETO", "Erro acesso login Service Android mobile...");
            return null;
        }

        return (action);
    }

    /* Função para verificar existência de conexão com a internet
	 */
    public boolean verificaConexao() {
        /*ConnectivityManager conectivtyManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conectivtyManager.getActiveNetworkInfo() != null
                && conectivtyManager.getActiveNetworkInfo().isAvailable()
                && conectivtyManager.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }*/
        return true;
    }

    @Override
    public void onDestroy() {
        IS_RUN = false;
        stopSelf(processos[0]);
        Log.i(StrConstant.TAG_LOGCAT, "ILiketoService - metodo onDestroy() / stopSelf()");
    }

//    private class ProcessNotifications extends Thread{
//        public int startId;
//        public boolean ativo;
//        public void run(){
//            while(ativo){
//                try {
//                    Log.i(StrConstant.TAG_LOGCAT, "ILiketoService - inner class ProcessNotifications - run Thread startId: " + startId);
//                    Thread.sleep(1000);
//                    ativo = false;
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//            Log.i(StrConstant.TAG_LOGCAT, "ILiketoService - inner class ProcessNotifications - stop Thread startId: " + startId);
//        }
//
//    }


}
