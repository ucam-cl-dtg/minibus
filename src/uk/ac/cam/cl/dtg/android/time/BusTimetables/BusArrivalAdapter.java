package uk.ac.cam.cl.dtg.android.time.BusTimetables;


import uk.ac.cam.cl.dtg.android.time.buses.*;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.*;

public class BusArrivalAdapter extends ArrayAdapter<BusArrival> { 
         Activity context; 
         View empty;
         LayoutInflater inflater;
         public int rowView = R.layout.busstoparrivalshort;
         
         BusArrivalAdapter(Activity context, BusArrivalData d ) { 
        	 
        	 
            super(context, R.layout.busstoparrivalshort, d.NextBuses); 
            inflater=context.getLayoutInflater();
            
           
            this.context=context; 
        } 
 
        public View getView(int position, View convertView, ViewGroup parent) { 
        	
             
            View row=inflater.inflate(rowView, null); 
            
            TextView service=(TextView)row.findViewById(R.id.col_service); 
            TextView dest=(TextView)row.findViewById(R.id.col_destination); 
            TextView due=(TextView)row.findViewById(R.id.col_due);
            ImageView imgdata=(ImageView)row.findViewById(R.id.img_data_indicator);
            
            // Get when we arrive
            BusArrival bus = super.getItem(position);
            ArrivalTime time = bus.getDueTime();
            
            service.setText(bus.getServiceID());
            dest.setText(bus.getDestination());
            due.setText(time.toString());
            
            // Change data indicator
            if(time.isLiveData) {
            	imgdata.setBackgroundResource(R.drawable.transmit_blue);
            } else {
            	imgdata.setBackgroundResource(R.drawable.timetabledata);
            }
            
            if(time.isDue) imgdata.setBackgroundResource(R.drawable.due);
 
            return(row); 
        } 
 }  