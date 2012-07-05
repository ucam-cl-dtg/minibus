package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * Provides helper methods for dealing with location information
 * 
 * @author drt24
 * 
 */
public class LocationHelper {

  public static Location selectBestLocation(List<Location> locations) {
    long currentTime = System.currentTimeMillis();
    Location best = null;
    double accuracy = -1;
    for (Location location : locations) {
      if (null == best) {
        best = location;
        accuracy =
            location.getAccuracy()
                * ((currentTime - location.getTime()) / LocationHelper.TIMEFACTOR);
      } else if (location.hasAccuracy()) {
        double newAccuracy =
            location.getAccuracy()
                * ((currentTime - location.getTime()) / LocationHelper.TIMEFACTOR);
        if (newAccuracy > 0 && newAccuracy < accuracy) {
          best = location;
          accuracy = newAccuracy;
        }
      }
    }
    return best;
  }

  /**
   * Get the last known location of the device, using the list of providers given
   * 
   * @param providers
   * @param locationManager
   * @return
   */
  public static Location getLastKnownLocation(LocationManager locationManager) {
    List<String> providers = locationManager.getProviders(true);
    Log.i("Nearby", "Providers: " + providers);
    List<Location> lastLocations = new ArrayList<Location>(providers.size());
    for (String provider : providers) {
      Location last = locationManager.getLastKnownLocation(provider);
      if (last != null) {
        lastLocations.add(last);
      }
    }
    return selectBestLocation(lastLocations);
  }

  /**
   * According to Wolfram Alpha mean walking speed is 1.1m/s and so every second of age is 1.1
   * meters of additional inaccuracy
   */
  private static final double TIMEFACTOR = 1100;

}
