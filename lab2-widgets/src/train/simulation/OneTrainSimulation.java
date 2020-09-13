package train.simulation;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import train.model.Route;
import train.model.Segment;
import train.view.TrainView;

public class OneTrainSimulation {

  public static void main(String[] args) {

    TrainView view = new TrainView();

    Route route = view.loadRoute();

    BlockingQueue<Segment> bq = new LinkedBlockingQueue<>();

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
