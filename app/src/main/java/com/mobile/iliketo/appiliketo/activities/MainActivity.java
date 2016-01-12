package com.mobile.iliketo.appiliketo.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ViewGroup.LayoutParams;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mobile.iliketo.appiliketo.R;
import com.mobile.iliketo.appiliketo.dao.DBILiketo;
import com.mobile.iliketo.appiliketo.model.ContentNotification;
import com.mobile.iliketo.appiliketo.model.User;
import com.mobile.iliketo.appiliketo.notifications.NotificationsILiketo;
import com.mobile.iliketo.appiliketo.services.ILiketoService;
import com.mobile.iliketo.appiliketo.util.StrConstant;
import com.mobile.iliketo.appiliketo.webview.MyWebClientILiketo;
import com.mobile.iliketo.appiliketo.webview.WebViewILiketo;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    public static String LAST_SEEN_DATE = "";
    public static int TOTAL_NOTIFICATIONS = 0;
    WebViewILiketo web;
    private static final int FILECHOOSER_RESULTCODE   = 2288;
    private ValueCallback<Uri> valorUploadCallBack;
    private Uri capturaImagemURI = null;

    public static final int INPUT_FILE_REQUEST_CODE = 1;
    public static final String EXTRA_FROM_NOTIFICATION = "EXTRA_FROM_NOTIFICATION";
    private WebView mWebView;
    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;

    ILiketoService meuService;
    boolean meuBound = false;
    private boolean gerarNotificacaoService = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i(StrConstant.TAG_LOGCAT, "MainActivity - metodo onCreate()");
        gerarNotificacaoService = true;
        /*Intent intent = new Intent(this, ILiketoService.class);
        intent.setAction(StrConstant.ACTION_ILIKETO_SERVICE);
        try {
            startService(intent);
            Log.i(StrConstant.TAG_LOGCAT, "MainActivity - executou startService()");
        }catch (Exception e){
            Log.i(StrConstant.TAG_LOGCAT, "MainActivity - erro startService");
        }*/

        //WebViewILiketo
        web = new WebViewILiketo(this);
        FrameLayout.LayoutParams frameLP1 = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        web.setLayoutParams(frameLP1);

        //Frames principal
        final FrameLayout frameLayoutWeb = (FrameLayout) findViewById(R.id.frameLayoutWeb);
        frameLayoutWeb.addView(web);

        //WebViewClient
        MyWebClientILiketo myWebClientILiketo = new MyWebClientILiketo(frameLayoutWeb, this);

        //Configs webview
        web.setWebViewClient(myWebClientILiketo);
        web.addJavascriptInterface(this, "AppILiketo");
        //web.requestFocus(View.FOCUS_DOWN);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setSupportZoom(false);
        //web.getSettings().setAllowFileAccess(true);
        //web.getSettings().setUseWideViewPort(true);
        //web.getSettings().setLoadsImagesAutomatically(true);
        //web.loadData(DataHtml.getIndexHtml(getDadosUsuario()), "text/html", "UTF-8");
        //web.loadUrl(StrConstant.URL_ILIKETO + getDadosUsuario());

        String arquivo = "";
        AssetManager assetManager = getApplicationContext().getResources().getAssets();
        try {
            InputStream inputStream = assetManager.open("index.html");
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);
            //arquivo = String.format(new String(b), null);
            arquivo = new String(b);
            inputStream.close();
        } catch (IOException e) {
            Log.i("LOG ILIKETO", "Erro ao abrir arquivo index.html", e);
        }

        arquivo = arquivo.replaceAll("URL_ILIKETO", StrConstant.URL_ILIKETO + getDadosUsuario());
        web.loadDataWithBaseURL("file:///android_asset/", arquivo, "text/html", "utf-8", null);

        //
        web.setWebChromeClient(new WebChromeClient() {

            // openFileChooser for Android < 3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                openFileChooser(uploadMsg, "");
            }

            // openFileChooser for Android 3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {

                Log.i("LOG ILIKETO", "Android 3.0+ detected .. openFileChooser()");

                // Update message
                valorUploadCallBack = uploadMsg;

                try {
                    // Create AndroidExampleFolder at sdcard
                    File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ILiketo Images");
                    Log.i("LOG ILIKETO", "Android 3.0+ detected - imageStorageDir: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
                    if (!imageStorageDir.exists()) {
                        // cria pasta no sdcard
                        imageStorageDir.mkdirs();
                        Log.i("LOG ILIKETO", "Android 3.0+ detected .. openFileChooser() - imageStorageDir.mkdirs()");
                    }
                    //imagem ou video
                    String s = web.getUrl();
                    if (s.contains("video")) {
                        Log.i("LOG ILIKETO", "Android 3.0+ detected .. openFileChooser() - VIDEO");
                        // Create camera captured image file path and name
                        String path = imageStorageDir + File.separator + "VID_" + String.valueOf(System.currentTimeMillis()) + ".mp4";
                        File file = new File(path);

                        capturaImagemURI = Uri.fromFile(file);

                        final Intent capturaIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                        capturaIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturaImagemURI);

                        Intent intentOpenVideo = new Intent(Intent.ACTION_GET_CONTENT);
                        intentOpenVideo.addCategory(Intent.CATEGORY_OPENABLE);
                        intentOpenVideo.setType("video/*");

                        //Intent para escolher arquivo
                        Intent chooserIntent = Intent.createChooser(intentOpenVideo, "Video Chooser");
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{capturaIntent}); // Set camera intent escolher arquivo

                        startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                        Log.i("LOG ILIKETO", "Android 3.0+ detected .. openFileChooser() - video/*");
                    } else {
                        Log.i("LOG ILIKETO", "Android 3.0+ detected .. openFileChooser() - IMAGE");
                        // Create camera captured image file path and name
                        String path = imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
                        File file = new File(path);

                        capturaImagemURI = Uri.fromFile(file);

                        final Intent capturaIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        capturaIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturaImagemURI);

                        Intent intentOpenImage = new Intent(Intent.ACTION_GET_CONTENT);
                        intentOpenImage.addCategory(Intent.CATEGORY_OPENABLE);
                        intentOpenImage.setType("image/*");

                        //Intent para escolher arquivo
                        Intent chooserIntent = Intent.createChooser(intentOpenImage, "Image Chooser");
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{capturaIntent}); // Set camera intent escolher arquivo

                        startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                        Log.i("LOG ILIKETO", "Android 3.0+ detected .. openFileChooser() - image/*");
                    }
                } catch (Exception e) {
                    Toast.makeText(getBaseContext(), "Exception:" + e, Toast.LENGTH_LONG).show();
                }
            }

            //openFileChooser for other Android versions
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                openFileChooser(uploadMsg, acceptType);
            }

            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams){

                Log.i("LOG ILIKETO", "Android Lollipop detected .. onShowFileChooser()");
                if(mFilePathCallback != null) {
                    mFilePathCallback.onReceiveValue(null);
                }
                mFilePathCallback = filePathCallback;

                // Create AndroidExampleFolder at sdcard
                File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ILiketo Images");
                Log.i("LOG ILIKETO", "Android Lollipop detected - imageStorageDir: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
                if (!imageStorageDir.exists()) {
                    // cria pasta no sdcard
                    imageStorageDir.mkdirs();
                    Log.i("LOG ILIKETO", "Android Lollipop detected .. onShowFileChooser() - imageStorageDir.mkdirs()");
                }
                //imagem ou video
                String s = web.getUrl();
                if (s.contains("video")) {
                    Log.i("LOG ILIKETO", "Android Lollipop detected .. onShowFileChooser() - VIDEO");
                    // Create camera captured image file path and name
                    String path = imageStorageDir + File.separator + "VID_" + String.valueOf(System.currentTimeMillis()) + ".mp4";
                    File file = new File(path);

                    capturaImagemURI = Uri.fromFile(file);

                    final Intent capturaIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    capturaIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturaImagemURI);

                    Intent intentOpenVideo = new Intent(Intent.ACTION_GET_CONTENT);
                    intentOpenVideo.addCategory(Intent.CATEGORY_OPENABLE);
                    intentOpenVideo.setType("video/*");

                    //Intent para escolher arquivo
                    Intent chooserIntent = Intent.createChooser(intentOpenVideo, "Video Chooser");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{capturaIntent}); // Set camera intent escolher arquivo

                    startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                    Log.i("LOG ILIKETO", "Android Lollipop detected .. onShowFileChooser() - video/*");
                } else {
                    Log.i("LOG ILIKETO", "Android Lollipop detected .. onShowFileChooser() - IMAGE");
                    // Create camera captured image file path and name
                    String path = imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
                    File file = new File(path);

                    capturaImagemURI = Uri.fromFile(file);

                    final Intent capturaIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    capturaIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturaImagemURI);

                    Intent intentOpenImage = new Intent(Intent.ACTION_GET_CONTENT);
                    intentOpenImage.addCategory(Intent.CATEGORY_OPENABLE);
                    intentOpenImage.setType("image/*");

                    //Intent para escolher arquivo
                    Intent chooserIntent = Intent.createChooser(intentOpenImage, "Image Chooser");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{capturaIntent}); // Set camera intent escolher arquivo

                    startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
                    Log.i("LOG ILIKETO", "Android Lollipop detected .. onShowFileChooser() - image/*");
                }

                return true;
            }
        });// End setWebChromeClient

        //disable scrolling
        //web.setVerticalScrollBarEnabled(false);
        web.setHorizontalScrollBarEnabled(false);
        web.setVerticalScrollBarEnabled(false);
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setUseWideViewPort(true);
        //web.setPadding(0, 0, 0, 0);
        web.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        //web.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        Log.i("LOG ILIKETO", "Start AppILiketo");

    }

    //@Override
    protected void onActivityResult1(int requestCode, int resultCode, Intent intent) {
        // Return here when file selected from camera or from SDcard

        if(requestCode == FILECHOOSER_RESULTCODE) {
            if (null == this.valorUploadCallBack) {
                return;
            }
            Uri result = null;
            try{
                if (resultCode != RESULT_OK) {
                    result = null;
                } else {
                    // retrieve from the private variable if the intent is null
                    result = intent == null ? capturaImagemURI : intent.getData();
                }
            }
            catch(Exception e) {
                Toast.makeText(getApplicationContext(), "activity :"+e, Toast.LENGTH_LONG).show();
            }
            valorUploadCallBack.onReceiveValue(result);
            valorUploadCallBack = null;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.i("LOG ILIKETO", "Android upload photo - onActivityResult()");
        if (requestCode == FILECHOOSER_RESULTCODE) {
            Log.i("LOG ILIKETO", "metodo onActivityResult() - requestCode:" +requestCode+ " - FILECHOOSER_RESULTCODE:" + FILECHOOSER_RESULTCODE);
            if (valorUploadCallBack == null && mFilePathCallback == null) {
                return;
            }
            Log.i("LOG ILIKETO", "metodo onActivityResult() - valorUploadCallBack:" +valorUploadCallBack+ " - mFilePathCallback:" + mFilePathCallback);
            Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
            Log.i("LOG ILIKETO", "metodo onActivityResult() - result: " + result);
            if (result!=null){
                /*String filePath = "";
                Log.i("LOG ILIKETO", "metodo onActivityResult() - result.getScheme(): " + result.getScheme());
                Log.i("LOG ILIKETO", "metodo onActivityResult() - result.getPath(): " + result.getPath());
                if ("content".equals(result.getScheme())) {
                    String[] proj = { MediaStore.Images.Media.TITLE };
                    Cursor cursor = this.getContentResolver().query(result, proj, null, null, null);
                    if (cursor != null && cursor.getCount() != 0) {
                        int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.TITLE);
                        cursor.moveToFirst();
                        filePath = cursor.getString(columnIndex);
                    }
                    //Cursor cursor = this.getContentResolver().query(result, null, null, null, null);
                    Log.i("LOG ILIKETO", "metodo onActivityResult() - cursor: " + cursor);
                    //cursor.moveToFirst();
                    //int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    //filePath = cursor.getString(idx);
                    //filePath = cursor.getString(0);
                    cursor.close();
                } else {
                    filePath = result.getPath();
                }
                filePath = result.getPath();
                Log.i("LOG ILIKETO", "metodo onActivityResult() - filePath: " + filePath);
                Uri myUri = Uri.parse(filePath);
                //verifica Android Lollipop
                */
                Uri myUri = result;
                if(mFilePathCallback != null) {
                    Log.i("LOG ILIKETO", "Android Lollipop detected .. result!=null");
                    mFilePathCallback.onReceiveValue(new Uri[]{myUri});
                }else {
                    valorUploadCallBack.onReceiveValue(myUri);
                }
            } else {
                //verifica Android Lollipop
                if(mFilePathCallback != null) {
                    Log.i("LOG ILIKETO", "Android Lollipop detected .. result==null");
                    mFilePathCallback.onReceiveValue(new Uri[]{capturaImagemURI});
                }else {
                    valorUploadCallBack.onReceiveValue(capturaImagemURI);
                }
            }
            valorUploadCallBack = null;
            mFilePathCallback = null;
            return;

        }else{
            Log.i("LOG ILIKETO", "metodo onActivityResult() - requestCode:" +requestCode+ " - FILECHOOSER_RESULTCODE:" + FILECHOOSER_RESULTCODE);
//            //android lollipop
//            if (requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
//                super.onActivityResult(requestCode, resultCode, intent);
//                return;
//            }
//
//            Uri[] results = null;
//
//            // Check that the response is a good one
//            if (resultCode == Activity.RESULT_OK) {
//                if (intent == null) {
//                     //If there is not data, then we may have taken a photo
//                    if (mCameraPhotoPath != null) {
//                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
//                        Log.i("LOG ILIKETO", "results[0] mCameraPhotoPath = " + results[0].getPath());
//                    }
//                    //results = new Uri[]{capturaImagemURI};
//                } else {
//                    String dataString = intent.getDataString();
//                    if (dataString != null) {
//                        results = new Uri[]{Uri.parse(dataString)};
//                        Log.i("LOG ILIKETO", "results[0] path = " + results[0].getPath());
//                    }
//                }
//            }
//
//            mFilePathCallback.onReceiveValue(results);
//            mFilePathCallback = null;
//            return;
        }
    }

    @Override
    public void onBackPressed() {
        //home e login > executa botao back da activity
        String url = web.getUrl();
        Log.i("LOG ILIKETO", "url=" + url);
        if (web.canGoBack() && !url.contains("login.jsp") && !url.contains("home") && !url.contains("page.jsp?id=160")
                && !url.endsWith("/") && !url.endsWith("/?")) {
            Log.i("LOG ILIKETO", "web.goBack()");
            web.goBack();
        } else {
            Log.i("LOG ILIKETO", "super.onBackPressed()");
            super.onBackPressed();
        }
    }

    @JavascriptInterface
    public boolean isAppMobileAndroid(){
        return true;
    }
    @JavascriptInterface
    public int getTotalNotificationsServiceAndroid(){
        int total = meuService.getTotalNotifications();
        return total;
    }

    @JavascriptInterface
    public void generateNotificationsMobile(String responseText){
        Log.i(StrConstant.TAG_LOGCAT, "MainActivity - javascript metodo generateNotificationsMobile | responseText: " + responseText);
        List<ContentNotification> list = new ArrayList<ContentNotification>();
        try {
            JSONArray array = new JSONArray(responseText);
            String total = array.getJSONObject(0).getString("total");
            String data = array.getJSONObject(1).getString("lastSeenDate");
            if(data.equalsIgnoreCase(this.LAST_SEEN_DATE) && this.TOTAL_NOTIFICATIONS == Integer.parseInt(total)){
                return;
            }else {
                for (int i = 2; i < array.length(); i++) {    //inicia contador com 2, pois o 0 eh total de notificacoes e 1 contem data ultimo visto
                    String msgNotific = array.getJSONObject(i).getString("msg");
                    ContentNotification cn = new ContentNotification();
                    cn.setDescriptionText(msgNotific);
                    list.add(cn);
                }
                this.TOTAL_NOTIFICATIONS = Integer.parseInt(total);
                this.LAST_SEEN_DATE = data;
            }
        } catch (JSONException e) {
            //e.printStackTrace();
            Log.e(StrConstant.TAG_LOGCAT, "MainActivity - error JSONException");
        }
        if(!list.isEmpty()) {
            //valida se usuario ja recebeu a mesma notificacao, se for diferente lanca nova notificacao
            NotificationsILiketo n = new NotificationsILiketo();
            n.generateNotifications(this, list);
        }
    }

    @JavascriptInterface
    public void getFormLoginMobile(String username, String password){
        Log.i("LOG ILIKETO", "javascript getFormLoginMobile");
        DBILiketo db = new DBILiketo(this);
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setAtivado(0);            //login desativado
        //insere e salva dados do usuario no bd
        db.insert(user);
        Log.i("LOG ILIKETO", "Tentando se conectar com Android mobile... | usuario: " + username);
    }

    @JavascriptInterface
    public void getHomeLoginMobile(int ok){
        Log.i("LOG ILIKETO", "javascript getHomeLoginMobile");
        int usuarioDefault = 1;
        DBILiketo db = new DBILiketo(this);
        User user = db.readById(usuarioDefault);
        if(user != null){
            //existe usuario
            if(user.getAtivado() != 1){
                user.setAtivado(ok); //valida usuario login ok
                db.update(user);
                Log.i("LOG ILIKETO", "Novo login Android mobile... | usuario: " + user.getUsername());
            }
        }
        if(gerarNotificacaoService) {
            Intent intent = new Intent(this, ILiketoService.class);
            intent.setAction(StrConstant.ACTION_ILIKETO_SERVICE);
            try {
                startService(intent);
                Log.i(StrConstant.TAG_LOGCAT, "MainActivity - executou startService()");
            } catch (Exception e) {
                Log.i(StrConstant.TAG_LOGCAT, "MainActivity - erro startService");
            }
            //gerar notificacao assim que o aplicativo for iniciado
            meuService.setGerarNotificacao(gerarNotificacaoService);
            gerarNotificacaoService = false;
        }
    }

    @JavascriptInterface
    public void getFormLogoutMobile(){
        Log.i("LOG ILIKETO", "javascript getFormLogoutMobile");
        int usuarioDefault = 1;
        DBILiketo db = new DBILiketo(this);
        User user = db.readById(usuarioDefault);
        if(user != null){
            //existe usuario
            //deleta dados usuario logado
            db.delete(usuarioDefault);
            Log.i("LOG ILIKETO", "Logout Android mobile... | usuario: " + user.getUsername());
        }
        //finaliza service
        Intent intent = new Intent(this, ILiketoService.class);
        intent.setAction(StrConstant.ACTION_ILIKETO_SERVICE);
        try {
            stopService(intent);
            Log.i(StrConstant.TAG_LOGCAT, "MainActivity - executou stopService()");
        }catch (Exception e){
            Log.i(StrConstant.TAG_LOGCAT, "MainActivity - erro stopService()");
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
                Log.i("LOG ILIKETO", "Entrou app Android mobile... | usuario: " + user.getUsername());
            }else{
                Log.i("LOG ILIKETO", "Nao foi ativado o login, usario ou senha invalidos... | usuario: " + user.getUsername());
            }
        }else{
            action = "/page.jsp?id=815";
        }

        return (action);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Bind to LocalService
        Intent intent = new Intent(this, ILiketoService.class);
        bindService(intent, myConnection, Context.BIND_AUTO_CREATE);
    }
    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (meuBound) {
            unbindService(myConnection);
            meuBound = false;
        }
    }
    private ServiceConnection myConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            ILiketoService.LocalBinder binder = (ILiketoService.LocalBinder) service;
            meuService = binder.getService();
            meuBound = true;
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            meuBound = false;
        }
    };

    @Override
    public void onDestroy(){
        Log.i(StrConstant.TAG_LOGCAT, "MainActivity - metodo onDestroy()");
        //Intent intent = new Intent(this, ILiketoService.class);
        //intent.setAction(StrConstant.ACTION_ILIKETO_SERVICE);
        //startService(intent);
        //Log.i(StrConstant.TAG_LOGCAT, "MainActivity - executou startService()");
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        web.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        web.restoreState(savedInstanceState);
    }
}
