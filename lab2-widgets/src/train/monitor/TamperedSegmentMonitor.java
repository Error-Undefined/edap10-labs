package train.monitor;

import java.util.Set;
import java.util.HashSet;

import train.model.Segment;

public class TamperedSegmentMonitor {
  private Set<Segment> busySegments = new HashSet<>();

  public synchronized void getSegmentPermit(Segment s) {
    while (busySegments.contains(s)) {
      try {
        wait();
      } catch (Exception e) {
        e.printStackTrace();
        throw new Error(e);
      }
    }

    busySegments.add(s);
  }

  public synchronized void releaseSegment(Segment s) {
    busySegments.remove(s);
    notifyAll();
  }
}
