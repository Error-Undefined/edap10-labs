package train.monitor;

import java.util.Set;
import java.util.HashSet;

import train.model.Segment;

public class SegmentMonitor {
  private Set<Segment> busySegments;

  public SegmentMonitor() {
    busySegments = new HashSet<>();
  }

  public synchronized void getSegmentPermit(Segment s) {
    while (busySegments.contains(s)) {
      try {
        wait();
      } catch (InterruptedException e) {
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
