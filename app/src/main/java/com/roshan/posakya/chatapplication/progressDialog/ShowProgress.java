package com.roshan.posakya.chatapplication.progressDialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.ContextThemeWrapper;

import com.roshan.posakya.chatapplication.R;


public class ShowProgress {

    Context context;
    ProgressDialog pDialog;
    String pleaseWait = "loading ....";

    public ShowProgress(Context context) {
        this.context = context;
    }

    ////// show progress dialog /////
    public void showProgress(){

        pDialog = ProgressDialog.show(new ContextThemeWrapper(context, R.style.NewDialog),"", pleaseWait,true);

        pDialog.show();

    }

    public void hideProgress(){
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
