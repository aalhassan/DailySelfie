 package com.example.student.dailyselfie;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.util.*;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

	private final  ArrayList<Image> images = new ArrayList<Image>();
	private final Context mContext;
	private static final String TAG = "Lab-DailySelfie";

	public ImageAdapter(Context context) {
		mContext = context;
}

	// Add an Image to the adapter
	// Notify observers that the data set has changed

	public void add(Image image) {

		images.add(image);
		notifyDataSetChanged();

	}

	// Clears the list adapter of all items.

	public void clear() {


		images.clear();
		notifyDataSetChanged();

	}
		
   public boolean picLoaded(String filePath){
	   if (getCount() > 0)
	    for (Image im: this.images) {
		    if (im.getFilePath().equals(filePath))
			return true;
		   }
		   return false;   
   }
   
	// Returns the number of Images
	@Override
	public int getCount() {

		return images.size();

	}

	// Retrieve the number of Images
	@Override
	public Object getItem(int pos) {

		return images.get(pos);

	}

	// Get the ID for the ToDoItem
	// In this case it's just the position

	@Override
	public long getItemId(int pos) {

		return pos;

	}

	// Create a View for the ToDoItem at specified position
	// Remember to check whether convertView holds an already allocated View
	// before created a new View.
	// Consider using the ViewHolder pattern to make scrolling more efficient
	// See: http://developer.android.com/training/improving-layouts/smooth-scrolling.html

    public static class ViewHolder {
         ImageView imageView  ;
         TextView dateTaken ;
 

    };

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// TODO - Get the current Image
		final Image image = (Image) getItem(position);


             // TODO - Inflate the View for this ToDoItem
             // from todo_item.xml
             RelativeLayout imageItem = (RelativeLayout) convertView;
            if (imageItem == null) {

				imageItem = (RelativeLayout) View.inflate(mContext, R.layout.image_item, null);

                ViewHolder holder = new ViewHolder() ;
                holder.dateTaken = (TextView) imageItem.findViewById(R.id.date_taken);
                holder.imageView = (ImageView) imageItem.findViewById(R.id.image_view);
                imageItem.setTag(holder);
            }
            
            ViewHolder holder = (ViewHolder) imageItem.getTag();
			 
        // Remember that the data that goes in this View
        // corresponds to the user interface elements defined
        // in the layout file

				// Set Pic-
               holder.imageView.setImageBitmap(image.getPic());
			   
			   //Set FileName/DateTaken
               holder.dateTaken.setText(image.getFileName());

		// Return the View you just created
		return imageItem;

	}
}
