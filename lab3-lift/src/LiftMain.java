import lift.LiftView;
import lift.Passenger;
import monitor.LiftMonitor;
import thread.LiftThread;

public class LiftMain {

  public static void main(String[] args) {
    LiftView view = new LiftView();
    LiftMonitor monitor = new LiftMonitor(view);

    Thread liftThread = new LiftThread(monitor);
  }
}
