package com.example.student.dailyselfie;


import android.content.Intent;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.provider.MediaStore;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.net.Uri;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DailySelfie extends AppCompatActivity {
    private  String mCurrentPhotoPath;
    private ListView mListView;
    private final String TARGET_H = "75" ; //Target Image Height
    private final String TARGET_W = "75" ;//Target Image Width
    public  boolean listenerSet; //Checks if listener is already set
    private ImageAdapter mAdapter;
    private static  final  int REQUEST_IMAGE_CAPTURE =1;

    private AlarmManager mAlarmManager;
    private Intent mNotificationReceiverIntent;
    private PendingIntent mNotificationReceiverPendingIntent;
    private static final long INITIAL_ALARM_DELAY = 2 * 60 * 1000L;

    private static final String FILE_NAME = "DailySelfieImages.txt";
    public static final String TAG = "Lab-DailySelfie";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daily_selfie);


        Log.i(TAG, "Entered On Create");

        mAdapter = new ImageAdapter(getApplicationContext());

        mListView = (ListView) findViewById(R.id.list_view);

        mListView.setAdapter(mAdapter);

        if (!getFileStreamPath(FILE_NAME).exists()) {
            saveImages();
        }

       listenerSet = false;

        // Get the AlarmManager Service
        mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        // Create an Intent to broadcast to the AlarmNotificationReceiver
        mNotificationReceiverIntent = new Intent(DailySelfie.this,
                AlarmNotificationReceiver.class);

        // Create an PendingIntent that holds the NotificationReceiverIntent
        mNotificationReceiverPendingIntent = PendingIntent.getBroadcast(
                DailySelfie.this, 0, mNotificationReceiverIntent, 0);

        //Initial Alarm Set.
        //setReminder(INITIAL_ALARM_DELAY);
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "Entered On Resume");
        //For a fresh start, check existing selfies, if any load them from file
        if (mAdapter.getCount() == 0 && mCurrentPhotoPath == null) try  {
            loadImages();
        }
        catch (IOException e) {
            Log.i(TAG,"Load IO Exception");

        }
       //On resume ,add any pic just taken
       else if (mCurrentPhotoPath != null && !mAdapter.picLoaded(mCurrentPhotoPath) && (new File(mCurrentPhotoPath).exists())) {
            //Create a new instance of ImageCreator to load the new Image and add it to Adapter
            new ImageCreator(DailySelfie.this).execute(mCurrentPhotoPath, TARGET_H , TARGET_W);
       }



        super.onResume();
    }

    @Override
    protected void onPause() {
        if(null != mAdapter && mAdapter.getCount() != 0) saveImages();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_daily_selfie, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

         if (id == R.id.takePic) {
             takeAPic();
            return true; //start Activity for result.
        }
        else if(id == R.id.set_reminder_2min){
             setReminder(INITIAL_ALARM_DELAY);
             return true;
         }
         else if(id == R.id.set_reminder_daily){
             setReminder(AlarmManager.INTERVAL_DAY);
             return true;
         }
         else if(id == R.id.cancel_reminder){
             cancelReminder();
             return true;
         }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Do nothing, but something on resume.
    }


    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File image = new File(storageDir, imageFileName+".jpg");

        // Save a file: Path for use with ACTION_VIEW intents
        mCurrentPhotoPath =  image.getAbsolutePath();
        return image;
    }

 
     //Method to take a pic using camera and save result image to External storage
    private void takeAPic(){
        Intent takePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(takePic.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                //
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePic.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePic, REQUEST_IMAGE_CAPTURE);
            }
        }

    }

    public void addImage(Image image){
        Log.i(TAG, "Adding Image");
        mAdapter.add(image);
    }

    //Set up onclickListenter for list items.
    public boolean setItemListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 //On clicking a list item. show image via an intent
                Image imageSelected = (Image) mAdapter.getItem(position);
                Intent showPic = new Intent();
                showPic.setAction(Intent.ACTION_VIEW);
                File file = new File(imageSelected.getFilePath());
                showPic.setDataAndType(Uri.fromFile(file), "image/*");
                startActivity(showPic);
            }
        });
        return true;
    }
    //Start Alarm Management

    private void setReminder(long repeat){
        // Cancel all alarms using mNotificationReceiverPendingIntent
        mAlarmManager.cancel(mNotificationReceiverPendingIntent);

        // Set repeating alarm
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + repeat,
                repeat,
                mNotificationReceiverPendingIntent);

        // Show Toast message
        Toast.makeText(getApplicationContext(),
                "Reminders Set! Cancel from menu.", Toast.LENGTH_LONG).show();
    }

    // Cancel all alarms using mNotificationReceiverPendingIntent
    private void cancelReminder() {
        // Cancel all alarms using mNotificationReceiverPendingIntent
        mAlarmManager.cancel(mNotificationReceiverPendingIntent);

        // Show Toast message
        Toast.makeText(getApplicationContext(),
                "Reminders Cancelled! Set from menu.", Toast.LENGTH_LONG).show();

    }

 // Load stored Images
    private void loadImages() throws IOException {
        Log.i(TAG, "Entered loadImages");
        FileInputStream fis = openFileInput(FILE_NAME);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String filePath = null;

        while (null != (filePath = br.readLine())) {
            //Create a new instance of ImageCreator to create Image and add it to Adapter
            if (new File(filePath).exists())// check for existence of the file first.
            new ImageCreator(DailySelfie.this).execute(filePath, TARGET_H , TARGET_W);
        }

        br.close();
    }

	// Save ImagePaths to file
	private void saveImages() {
		PrintWriter writer = null;
		try {
			FileOutputStream fos = openFileOutput(FILE_NAME, MODE_PRIVATE);
			writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					fos)));
			for (int idx = 0; idx < mAdapter.getCount(); idx++) {

				writer.println(((Image) mAdapter.getItem(idx)).getFilePath());

			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != writer) {
				writer.close();
			}
		}
	}
}