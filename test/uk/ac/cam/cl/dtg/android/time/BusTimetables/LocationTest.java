package uk.ac.cam.cl.dtg.android.time.BusTimetables;

import static org.junit.Assert.assertEquals;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import android.location.Location;

public class LocationTest {

  @Test
  public void selectBestLocation() {
    long currentTime = System.currentTimeMillis();
    List<Location> locations = new ArrayList<Location>();

    Location recentBad = createMock(Location.class);
    expect(recentBad.hasAccuracy()).andReturn(true);
    expect(recentBad.getAccuracy()).andReturn(30f);
    expect(recentBad.getTime()).andReturn(currentTime);
    replay(recentBad);

    Location realBest = createMock(Location.class);
    expect(realBest.hasAccuracy()).andReturn(true);
    expect(realBest.getAccuracy()).andReturn(3f);
    expect(realBest.getTime()).andReturn(currentTime - 5L);
    replay(realBest);

    Location oldGood = createMock(Location.class);
    expect(oldGood.hasAccuracy()).andReturn(true);
    expect(oldGood.getAccuracy()).andReturn(0.1f);
    expect(oldGood.getTime()).andReturn(currentTime - 1000L * 60L * 60L * 24L);
    replay(oldGood);

    locations.add(recentBad);
    locations.add(realBest);
    locations.add(oldGood);

    Location best = LocationHelper.selectBestLocation(locations);
    assertEquals("Best location not selected", realBest, best);
  }
}
