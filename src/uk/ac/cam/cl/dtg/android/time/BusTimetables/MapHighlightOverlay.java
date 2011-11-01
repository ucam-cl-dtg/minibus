package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapHighlightOverlay extends ItemizedOverlay<OverlayItem> {
	
	public static final String HIGHLIGHT_INTENT = "uk.ac.cam.cl.dtg.android.time.BusStopApp.HIGHLIGHT_BUS_STOP";
	MapView mv;
	
	
	@Override
	public boolean onTap(GeoPoint pt, MapView mv) {
	return super.onTap(pt, mv);
	}


	// Collection of map markers in live usage
	private ArrayList<OverlayItem> Markers = new ArrayList<OverlayItem>();

	/**
	 * Constructor. Just refreshes markers at the moment.
	 * 
	 * @param defaultMarker
	 */
	
	public MapHighlightOverlay(Drawable defaultMarker) {
		super(boundCenter(defaultMarker));	
		populate();
	}
	public MapHighlightOverlay(Drawable defaultMarker, MapView mv) {
		
		this(defaultMarker);
		this.mv = mv;
		

	}
	
	public void doHighlight(int latE6, int longE6, MapView mv) {
		
		highlightLocation(latE6, longE6);
		
		MapController mc = mv.getController();
		
		// TODO: make this despatch a message or runnable
		mc.animateTo(new GeoPoint(latE6, longE6));
			
		mc.setZoom(18);
		
	}
	
	private void highlightLocation(int latE6, int longE6) {
		
		Markers.clear();
		
		GeoPoint point = new GeoPoint(latE6,longE6);
		OverlayItem overlayitem = new OverlayItem(point, "", "");
		Markers.add(overlayitem);
		
		setLastFocusedIndex(-1);
		populate();
		
	}
	
    @Override
    public void draw(Canvas canvas, MapView view, boolean shadow ) {

            super.draw(canvas, view, false);

    } 


	@Override
	protected OverlayItem createItem(int i) {
		return Markers.get(i);
	}


	@Override
	public int size() {
		return Markers.size();
	}

}
