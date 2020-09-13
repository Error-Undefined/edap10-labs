package train.simulation;

import train.view.TrainView;
import train.threads.UnsafeRouteThread;

public class MultipleTrainSimulation {
  public static void main(String[] args) {

    TrainView view = new TrainView();

    Thread t1 = new UnsafeRouteThread(view);
    Thread t2 = new UnsafeRouteThread(view);
    Thread t3 = new UnsafeRouteThread(view);

    t1.start();
    t2.start();
    t3.start();
  }

}
