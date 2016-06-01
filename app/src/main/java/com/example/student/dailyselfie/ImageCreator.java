package com.example.student.dailyselfie;/**
 * Created by aalhassan on 11/16/15.
 */


import android.os.AsyncTask;
import java.lang.ref.WeakReference;


public class ImageCreator extends AsyncTask<String, Void , Image> {

    private WeakReference<DailySelfie> mParent;

    public  ImageCreator(DailySelfie activity) {
        mParent =  new  WeakReference<DailySelfie>(activity);
    }
    @Override
    protected Image doInBackground(String... params) {
        String filePath = params[0];
        int targetH = Integer.parseInt(params[1]);
        int targetW = Integer.parseInt(params[2]);

       return new Image(filePath,targetH,targetW);

    }

    @Override
    protected void onPostExecute(Image result) {
        if (mParent.get() != null) {
            //Add Image to activity adapter
            mParent.get().addImage(result);
            //Set item Listener if it's not already set.
           if   (!mParent.get().listenerSet && mParent.get().setItemListener() && (mParent.get().listenerSet = !mParent.get().listenerSet));
        }
    }
}
