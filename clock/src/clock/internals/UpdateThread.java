package clock.internals;

import java.sql.Time;

import clock.data.ClockState;
import clock.data.TimeStruct;

public class UpdateThread extends Thread {
  private ClockState state;

  public UpdateThread(ClockState state) {
    this.state = state;
  }

  @Override
  public void run() {

    int shouldPling = 0;

    long t0 = System.currentTimeMillis();
    long sleepTo = t0 + 1000;
    for (;;) {

      TimeStruct alarmTime = state.getAlarmTime();
      TimeStruct clockTime = state.getClockTime();

      if (alarmTime.equals(clockTime) && state.isAlarmArmed()) {
        shouldPling = 20;
      }

      if (shouldPling > 0) {
        shouldPling--;
        state.plingAlarm();
      }

      // Tick time forward 1 second
      state.tickForward();

      long now = System.currentTimeMillis();

      try {
        // Sleep with compensated delay
        Thread.sleep(sleepTo - now);
        sleepTo += 1000;
      } catch (InterruptedException e) {
        System.err.println("Update thread died unexpectedly");
        e.printStackTrace();
        throw new Error(e);
      }
    }
  }
}