package thread;

import lift.LiftView;
import lift.Passenger;
import monitor.LiftMonitor;

public class LiftThread extends Thread {
  private LiftMonitor monitor;

  public LiftThread(LiftMonitor monitor) {
    this.monitor = monitor;
  }
}
