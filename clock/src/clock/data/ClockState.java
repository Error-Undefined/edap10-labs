package clock.data;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import clock.io.ClockInput;
import clock.io.ClockOutput;

public class ClockState {
  private final Lock stateMutex;

  private final TimeData clockTime;
  private final TimeData alarmTime;

  private ClockInput input;
  private ClockOutput output;

  public ClockState(ClockInput in, ClockOutput out) {
    stateMutex = new ReentrantLock();

    clockTime = new TimeData();
    alarmTime = new TimeData();

    this.input = in;
    this.output = out;
  }

  public void tickForward() {
    stateMutex.lock();
    clockTime.tickForward();
    stateMutex.unlock();
  }

  public void updateClock() {
    stateMutex.lock();
    output.displayTime(clockTime.getHours(), clockTime.getMinutes(), clockTime.getSeconds());
    stateMutex.unlock();
  }

}