package train.simulation;

import train.view.TrainView;
import train.monitor.SegmentMonitor;
import train.monitor.TamperedSegmentMonitor;
import train.threads.SafeRouteThread;

public class SafeMultipleTrainSimulation {
  public static void main(String[] args) {

    TrainView view = new TrainView();
    SegmentMonitor monitor = new SegmentMonitor();
    // TamperedSegmentMonitor monitor = new TamperedSegmentMonitor();

    for (int i = 0; i < 20; i++) {
      Thread t = new SafeRouteThread(monitor, view);
      t.start();
    }
  }

}
