package train.threads;

import train.model.Route;
import train.model.Segment;
import train.monitor.SegmentMonitor;
import train.monitor.TamperedSegmentMonitor;
import train.view.TrainView;

import java.util.LinkedList;
import java.util.Queue;

public class SafeRouteThread extends Thread {
  private SegmentMonitor monitor;
  private TrainView view;

  public SafeRouteThread(SegmentMonitor monitor, TrainView view) {
    this.monitor = monitor;
    this.view = view;
  }

  public void run() {
    Route route = view.loadRoute();

    Queue<Segment> bq = new LinkedList<>();

    for (int i = 0; i < 3; i++) {
      Segment s = route.next();
      bq.add(s);
      monitor.getSegmentPermit(s);
      s.enter();
    }

    for (;;) {
      Segment head = route.next();
      monitor.getSegmentPermit(head);
      head.enter();
      bq.add(head);
      Segment tail = bq.poll();
      tail.exit();
      monitor.releaseSegment(tail);
    }
  }
}
