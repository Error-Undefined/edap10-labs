package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class SpinController extends ActorThread<WashingMessage> {

  private WashingIO io;

  private int previousIO;
  private WashingMessage currentMessage;

  public SpinController(WashingIO io) {
    this.io = io;
    previousIO = 0;
    currentMessage = null;
  }

  @Override
  public void run() {
    try {
      while (true) {
        // wait for up to a (simulated) minute for a WashingMessage
        WashingMessage m = receiveWithTimeout(60000 / Settings.SPEEDUP);

        // if m is null, it means a minute passed and no message was received
        if (m != null) {
          System.out.println("got " + m);
          currentMessage = m;
        }

        if (currentMessage != null) {
          int spinModeVar = 0;

          if (currentMessage.getCommand() == WashingMessage.SPIN_FAST) {
            spinModeVar = WashingIO.SPIN_FAST;
          } else if (currentMessage.getCommand() == WashingMessage.SPIN_OFF) {
            spinModeVar = WashingIO.SPIN_IDLE;
          } else if (currentMessage.getCommand() == WashingMessage.SPIN_SLOW) {
            spinModeVar = (previousIO == WashingIO.SPIN_LEFT) ? WashingIO.SPIN_RIGHT : WashingIO.SPIN_LEFT;
          }
          io.setSpinMode(spinModeVar);
          previousIO = spinModeVar;

          if (m != null) {
            WashingMessage ack = new WashingMessage(this, WashingMessage.ACKNOWLEDGMENT);
            currentMessage.getSender().send(ack);
          }

        }

      }
    } catch (InterruptedException unexpected) {
      // we don't expect this thread to be interrupted,
      // so throw an error if it happens anyway
      throw new Error(unexpected);
    }
  }
}
