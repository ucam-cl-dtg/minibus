package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import uk.ac.cam.cl.dtg.android.time.buses.*;

public class BusStopMarker extends OverlayItem {
	
	public BusStop busStop;

	public BusStopMarker(BusStop stop, int late6, int longe6) {
		super(new GeoPoint(late6, longe6), stop.getName(), stop.getStopRef());
		
		busStop = stop;
	}
	
	public BusStopMarker(BusStop stop) {
		super(getPoint(stop.getLatitude(),stop.getLongitude()), stop.getName(), stop.getStopRef());
		
		busStop = stop;
	}
	
    private static GeoPoint getPoint(double lat, double lon)
    {
    	return new GeoPoint((int)((double)lat * 1E6), (int)((double)lon * 1E6));
    }


}
