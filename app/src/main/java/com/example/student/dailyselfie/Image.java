 package com.example.student.dailyselfie;


import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import java.io.File;



 public class Image {
    
         private String filePath;
         private Bitmap pic;
         private String fileName;

         public void setFilePath(String filePath){

             this.filePath = filePath;
         }

     public void setPic(Bitmap bitmap){

         this.pic = bitmap;
     }

         public String getFilePath() {
            return filePath;
        }


        public Bitmap getPic() {
            return this.pic;
        }

     public String getFileName() {
         return this.fileName;
     }


        //Constructor for Image
        public Image(String filePath, int imageH, int imageW)
        {

                setFilePath(filePath);
                fileName = new File(filePath).getName();
                setPic(loadBitmap(filePath, imageH, imageW));
        }


       private static Bitmap loadBitmap(String filePath, int targetH, int targetW) {

           // Get the dimensions of the bitmap
           BitmapFactory.Options bmOptions = new BitmapFactory.Options();
           bmOptions.inJustDecodeBounds = true;
           BitmapFactory.decodeFile(filePath, bmOptions);
           int photoW = bmOptions.outWidth;
           int photoH = bmOptions.outHeight;

           // Determine how much to scale down the image
           int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

           // Decode the image file into a Bitmap sized to fill the View
           bmOptions.inJustDecodeBounds = false;
           bmOptions.inSampleSize = scaleFactor;
           bmOptions.inPurgeable = true;

           return BitmapFactory.decodeFile(filePath, bmOptions);
}

}
