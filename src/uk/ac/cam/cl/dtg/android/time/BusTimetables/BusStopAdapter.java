package uk.ac.cam.cl.dtg.android.time.BusTimetables;


import java.util.List;

import uk.ac.cam.cl.dtg.android.time.BusTimetables.Preferences.UNITS;
import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BusStopAdapter extends ArrayAdapter<BusStop> { 
	Context context; 
	View empty;
	LayoutInflater inflater;
	Location currentPosition;


	BusStopAdapter(Context context,List<BusStop> stops, Location currentPos) { 


		super(context, R.layout.busstoparrivalshort,stops.toArray(new BusStop[1])); 
		inflater = LayoutInflater.from(context);

		currentPosition = currentPos;

		this.context=context; 
	} 

	@Override
  public View getView(int position, View convertView, ViewGroup parent) { 


		UNITS units = Preferences.getUnits();

		View row=inflater.inflate(R.layout.nearbystoprow, null); 

		TextView stopname=(TextView)row.findViewById(R.id.col_nearby_name); 
		TextView dist=(TextView)row.findViewById(R.id.col_nearby_dist); 
		ImageView icon=(ImageView)row.findViewById(R.id.img_stopicon);

		BusStop stop = super.getItem(position);
		stopname.setText(stop.getName());
		
		Log.i("BusStopAdapter","Processing stop: "+stop.getName());
		
	
		if(stop.getMeta("FavID")!=null) icon.setBackgroundResource(R.drawable.star2);

		// Now calculate distance!
		Location l = new Location("gps");
		l.setLatitude(stop.getLatitude() / 1E6);
		l.setLongitude(stop.getLongitude() / 1E6);
		float d = currentPosition.distanceTo(l);

		// Format the distance
		int finaldist = (int)d;


		String suffix = "m";
		String displayDist;


		if(units.equals(UNITS.Metric)) {
			if(finaldist > 1000) {
				suffix = "km";
				finaldist = (int)(Math.round((double)finaldist/100));
				displayDist = Double.toString(((double)finaldist / 10));

			} else {

				finaldist = (int)(Math.round((double)finaldist/10)) * 10; 
				displayDist = Integer.toString(finaldist);

			}

			// Imperial?
		} else {

			if(finaldist < 1700) { 
				finaldist = (int)((double)finaldist * 1.0936133);
				finaldist = (int)(Math.round((double)finaldist/10)) * 10; 
				displayDist = Integer.toString(finaldist);
				suffix=" yards";
			} else {

				suffix = " miles";
				finaldist = (int)(Math.round((double)finaldist/160.9));
				displayDist = Double.toString(((double)finaldist / 10));

			}
		}

		dist.setText(displayDist+suffix);

		return(row); 
	} 
}  
