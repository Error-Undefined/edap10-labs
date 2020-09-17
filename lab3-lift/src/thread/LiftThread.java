package thread;

import lift.LiftView;

public class LiftThread extends Thread {
  private LiftView view;

  public LiftThread(LiftView view) {
    this.view = view;
  }
}
