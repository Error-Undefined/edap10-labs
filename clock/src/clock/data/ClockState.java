package clock.data;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import clock.io.ClockOutput;

public class ClockState {
  private final Lock stateMutex;

  private final TimeData clockTime;
  private final TimeData alarmTime;

  private int alarmState;

  private ClockOutput output;

  private static final int ALARM_OFF = 0;
  private static final int ALARM_ARMED = 1;

  public ClockState(ClockOutput out) {
    stateMutex = new ReentrantLock();

    clockTime = new TimeData();
    alarmTime = new TimeData();

    this.output = out;

    alarmState = ALARM_OFF;
  }

  public void tickForward() {
    stateMutex.lock();
    clockTime.tickForward();
    output.displayTime(clockTime.getHours(), clockTime.getMinutes(), clockTime.getSeconds());
    stateMutex.unlock();
  }

  public void setClockTime(int hours, int minutes, int seconds) {
    stateMutex.lock();
    clockTime.setTime(hours, minutes, seconds);
    output.displayTime(clockTime.getHours(), clockTime.getMinutes(), clockTime.getSeconds());
    stateMutex.unlock();
  }

  public void setAlarmTime(int hours, int minutes, int seconds) {
    stateMutex.lock();
    alarmTime.setTime(hours, minutes, seconds);
    stateMutex.unlock();
  }

  public TimeStruct getClockTime() {
    stateMutex.lock();
    TimeStruct t = new TimeStruct(clockTime.getHours(), clockTime.getMinutes(), clockTime.getSeconds());
    stateMutex.unlock();
    return t;
  }

  public TimeStruct getAlarmTime() {
    stateMutex.lock();
    TimeStruct t = new TimeStruct(alarmTime.getHours(), alarmTime.getMinutes(), alarmTime.getSeconds());
    stateMutex.unlock();
    return t;
  }

  public void toggleAlarm() {
    stateMutex.lock();
    if (alarmState == ALARM_OFF) {
      alarmState = ALARM_ARMED;
      output.setAlarmIndicator(true);
    } else {
      alarmState = ALARM_OFF;
      output.setAlarmIndicator(false);
    }
    stateMutex.unlock();
  }

  /**
   * Plings the alarm once if it is armed.
   */
  public void plingAlarm() {
    stateMutex.lock();
    if (alarmState != ALARM_ARMED) {
      stateMutex.unlock();
      return;
    }
    output.alarm();
    stateMutex.unlock();
  }

  public boolean isAlarmArmed() {
    stateMutex.lock();
    boolean result = alarmState == ALARM_ARMED;
    stateMutex.unlock();
    return result;
  }
}