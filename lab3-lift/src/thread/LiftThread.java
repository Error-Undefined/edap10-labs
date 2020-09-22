package thread;

import lift.LiftView;
import lift.Passenger;
import monitor.Floors;
import monitor.LiftMonitor;

public class LiftThread extends Thread {
  private LiftMonitor monitor;

  private LiftView view;

  public LiftThread(LiftMonitor monitor) {
    this.monitor = monitor;
    view = monitor.getView();
  }

  @Override
  public void run() {
    for (;;) {
      try {
        monitor.getMovePermit();
        Floors f = monitor.getFloors();
        view.moveLift(f.floor, f.nextFloor);
        monitor.releaseMovePermit();
        monitor.allowPassengerTransfer();
      } catch (InterruptedException e) {
        throw new Error(e);
      }

    }
  }
}
