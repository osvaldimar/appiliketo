package com.mobile.iliketo.appiliketo.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.mobile.iliketo.appiliketo.util.StrConstant;

/**
 * Created by OSVALDIMAR on 9/7/2015.
 */
public class BroadCastReceiverILiketo extends BroadcastReceiver{

    @Override
    public void onReceive(Context context, Intent intent) {
        intent = new Intent(context, ILiketoService.class);
        intent.setAction(StrConstant.ACTION_ILIKETO_SERVICE);
        //intent.addCategory(StrConstant.CATEGORY_ILIKETO_SERVICE);
        context.startService(intent);
    }

}
