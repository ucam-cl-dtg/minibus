package uk.ac.cam.cl.dtg.android.time.BusTimetables;


import java.util.List;

import uk.ac.cam.cl.dtg.android.time.buses.BusStop;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StopSearchResultAdapter extends ArrayAdapter<BusStop> { 
         Activity context; 
         View empty;
         LayoutInflater inflater;
       //  Location currentPosition;
         
        public StopSearchResultAdapter(Activity context,List<BusStop> stops) { 
        	 
        	 
            super(context, R.layout.busstoparrivalshort,stops.toArray(new BusStop[1])); 
            inflater=context.getLayoutInflater();
            
          //  currentPosition = currentPos;
            
            empty = inflater.inflate(R.layout.lookupresultsempty, null);
            
            this.context=context; 
        } 
 
        @Override
        public View getView(int position, View convertView, ViewGroup parent) { 
        	
             
            View row=inflater.inflate(R.layout.lookupstoprow, null); 
            
            TextView stopname=(TextView)row.findViewById(R.id.col_nearby_name); 
           // TextView dist=(TextView)row.findViewById(R.id.col_nearby_dist); 
            
            BusStop stop = super.getItem(position);
            
            if(stop == null) return empty;

            stopname.setText(stop.getName());
            
            /*// Now calculate distance!
            Location l = new Location("gps");
            l.setLatitude(stop.getLatitude() / 1E6);
            l.setLongitude(stop.getLongitude() / 1E6);
            float d = currentPosition.distanceTo(l);
            
            // Format the distance
            int finaldist = (int)d;
            
            String suffix = "m";
            String displayDist;
            
            finaldist = (int)(Math.round((double)finaldist/10)) * 10;            
            
            if(finaldist > 1000) {
            	suffix = "km";
            	finaldist = (int)(Math.round((double)finaldist/100));
            	displayDist = Double.toString(((double)finaldist / 10));
            } else {
            	displayDist = Integer.toString(finaldist);
            }
            
            dist.setText(displayDist+suffix);*/

 
            return(row); 
        } 
 }  
