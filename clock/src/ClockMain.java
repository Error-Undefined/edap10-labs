import java.util.concurrent.Semaphore;

import clock.AlarmClockEmulator;
import clock.data.ClockState;
import clock.internals.UpdateThread;
import clock.io.ClockInput;
import clock.io.ClockInput.UserInput;
import clock.io.ClockOutput;

public class ClockMain {

  static final int CHOICE_SET_TIME = 1; // user set new clock time
  static final int CHOICE_SET_ALARM = 2; // user set new alarm time
  static final int CHOICE_TOGGLE_ALARM = 3; // user pressed both buttons simultaneously

  public static void main(String[] args) throws InterruptedException {
    AlarmClockEmulator emulator = new AlarmClockEmulator();

    ClockInput in = emulator.getInput();
    ClockOutput out = emulator.getOutput();

    ClockState state = new ClockState(in, out);

    Thread update = new UpdateThread(state);
    update.start();

    // out.displayTime(23, 59, 51); // arbitrary time: just an example

    Semaphore hardwareInterrupt = in.getSemaphore();

    while (true) {

      hardwareInterrupt.acquire();
      UserInput userInput = in.getUserInput();

      int choice = userInput.getChoice();
      int h = userInput.getHours();
      int m = userInput.getMinutes();
      int s = userInput.getSeconds();

      switch (choice) {
        case CHOICE_SET_TIME:
          state.setClockTime(h, m, s);
          state.unplingAlarm();
          break;
        case CHOICE_SET_ALARM:
          state.setAlarmTime(h, m, s);
          state.unplingAlarm();
          break;
        case CHOICE_TOGGLE_ALARM:
          state.toggleAlarm();
          break;
        default:
          break;
      }

      // System.out.println("choice=" + choice + " h=" + h + " m=" + m + " s=" + s);
    }
  }
}
