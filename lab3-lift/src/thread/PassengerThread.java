package thread;

import lift.Passenger;
import monitor.LiftMonitor;

public class PassengerThread extends Thread {
  private Passenger passenger;
  private LiftMonitor monitor;

  public PassengerThread(Passenger passenger, LiftMonitor monitor) {
    this.passenger = passenger;
    this.monitor = monitor;
  }

  public void run() {
    passenger.begin();
    try {
      monitor.getEnterPermit(passenger.getStartFloor(), passenger.getDestinationFloor());
      passenger.enterLift();
      monitor.updateAfterEnter(passenger.getStartFloor(), passenger.getDestinationFloor());

      monitor.getExitPermit(passenger.getDestinationFloor());
      passenger.exitLift();
      monitor.updateAfterExit(passenger.getDestinationFloor());
    } catch (InterruptedException e) {
      throw new Error(e);
    }
    passenger.end();
  }
}
