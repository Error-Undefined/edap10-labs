package train.threads;

import java.util.LinkedList;
import java.util.Queue;

import train.model.Route;
import train.model.Segment;
import train.view.TrainView;

public class UnsafeRouteThread extends Thread {
  private TrainView view;

  public UnsafeRouteThread(TrainView view) {
    this.view = view;
  }

  // This is unsafe as we might share segments between threads.
  public void run() {
    Route route = view.loadRoute();

    Queue<Segment> bq = new LinkedList<>();

    for (int i = 0; i < 3; i++) {
      Segment s = route.next();
      bq.add(s);
      s.enter();
    }

    for (;;) {
      Segment head = route.next();
      head.enter();
      bq.add(head);
      Segment tail = bq.poll();
      tail.exit();
    }
  }
}
