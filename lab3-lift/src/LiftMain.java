import lift.LiftView;
import monitor.LiftMonitor;
import thread.LiftThread;
import thread.PassengerThread;

public class LiftMain {

  public static void main(String[] args) throws InterruptedException {
    LiftView view = new LiftView();
    LiftMonitor monitor = new LiftMonitor(view);

    Thread liftThread = new LiftThread(monitor);

    liftThread.start();

    for (int i = 0; i < 60; i++) {
      Thread passengerThread = new PassengerThread(view.createPassenger(), monitor);
      passengerThread.start();
    }
  }
}
