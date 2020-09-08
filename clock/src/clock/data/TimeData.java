package clock.data;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A class to keep track of a time. Thread safe.
 */
public class TimeData {
  private Lock mutex = new ReentrantLock();

  private int hours;
  private int minutes;
  private int seconds;

  private static final int MAX_SECONDS = 59;
  private static final int MAX_MINUTES = 59;
  private static final int MAX_HOURS = 23;

  public TimeData() {
    this(0, 0, 0);
  }

  public TimeData(int hours, int minutes, int seconds) {
    this.hours = hours;
    this.minutes = minutes;
    this.seconds = seconds;
  }

  /**
   * Ticks the clock one second forwards.
   */
  public void tickForward() {
    mutex.lock();
    if (seconds == MAX_SECONDS) {
      if (minutes == MAX_MINUTES) {
        minutes = 0;
        if (hours == MAX_HOURS) {
          hours = 0;
        } else {
          ++hours;
        }
      } else {
        ++minutes;
      }
      seconds = 0;
    } else {
      ++seconds;
    }
    mutex.unlock();
  }

  public void setTime(int hours, int minutes, int seconds) {
    mutex.lock();
    this.hours = hours;
    this.minutes = minutes;
    this.seconds = seconds;
    mutex.unlock();
  }

  public int getHours() {
    mutex.lock();
    int hoursReturn = hours;
    mutex.unlock();
    return hoursReturn;
  }

  public int getMinutes() {
    mutex.lock();
    int minutesReturn = minutes;
    mutex.unlock();
    return minutesReturn;
  }

  public int getSeconds() {
    mutex.lock();
    int secondsReturn = seconds;
    mutex.unlock();
    return secondsReturn;
  }

}