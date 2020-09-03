package clock.internals;

import clock.data.ClockState;

public class UpdateThread extends Thread {
  private ClockState state;

  public UpdateThread(ClockState state) {
    this.state = state;
  }

  @Override
  public void run() {

    long t0 = System.currentTimeMillis();
    long sleepTo = t0 + 1000;
    for (;;) {
      try {
        state.tickForward();
        state.updateClock();
        long now = System.currentTimeMillis();
        Thread.sleep(sleepTo - now);
        sleepTo += 1000;
      } catch (InterruptedException e) {
        System.err.println("Update thread died unexpectedly");
        e.printStackTrace();
      }
    }
  }
}