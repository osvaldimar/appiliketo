package com.mobile.iliketo.appiliketo.webview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mobile.iliketo.appiliketo.activities.MainActivity;
import com.mobile.iliketo.appiliketo.util.StrConstant;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by OSVALDIMAR on 8/27/2015.
 */
public class MyWebClientILiketo extends WebViewClient {

    private FrameLayout frameLayoutWeb;
    private ProgressBar pb;
    private Context context;

    public MyWebClientILiketo(FrameLayout frameLayoutWeb, Context context){
        this.frameLayoutWeb = frameLayoutWeb;
        this.context = context;

        //ProgressBar
        pb = new ProgressBar(context);
        FrameLayout.LayoutParams frameLP2 = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        frameLP2.gravity = Gravity.CENTER;
        pb.setLayoutParams(frameLP2);

    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        // TODO Auto-generated method stub
        Log.i("LOG ILIKETO", "url started = " + view.getUrl());
        frameLayoutWeb.removeView(pb);
        frameLayoutWeb.addView(pb);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        // TODO Auto-generated method stub
        Log.i("LOG ILIKETO", "url finished = " + view.getUrl());
        frameLayoutWeb.removeView(pb);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // TODO Auto-generated method stub
        view.loadUrl(url);
        return true;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

        Log.i("LOG ILIKETO", "Erro no internet, connection timeout!");

        super.onReceivedError(view, errorCode, description, failingUrl);

        String arquivo = "";
        AssetManager assetManager = context.getApplicationContext().getResources().getAssets();
        try {
            InputStream inputStream = assetManager.open("error.html");
            byte[] b = new byte[inputStream.available()];
            inputStream.read(b);
            //arquivo = String.format(new String(b), null);
            arquivo = new String(b);
            inputStream.close();
        } catch (IOException e) {
            Log.i("LOG ILIKETO", "Erro ao abrir arquivo error.html", e);
        }

        view.loadDataWithBaseURL("file:///android_asset/", arquivo, "text/html", "utf-8", null);
    }
}
