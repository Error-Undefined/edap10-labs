package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class TemperatureController extends ActorThread<WashingMessage> {

  private final int dt = 10;

  private final double mu = dt * 0.0478;
  private final double ml = dt * 9.52 / (10000);

  private double heatTo = 0;

  private boolean shouldHeat = false;
  private boolean isHeating = false;

  private WashingIO io;

  private WashingMessage currentMessage;
  private WashingMessage notifyBack;

  public TemperatureController(WashingIO io) {
    this.io = io;
    currentMessage = null;
    notifyBack = null;
  }

  @Override
  public void run() {
    try {

      for (;;) {
        // Wait for a period of simulated time. This also acts as periodicity
        WashingMessage m = receiveWithTimeout(dt * 1000 / Settings.SPEEDUP);

        // Not null if a message was recieved
        if (m != null) {
          System.out.println("got: " + m);
          currentMessage = m;
          notifyBack = m;
        }

        if (currentMessage != null) {
          if (currentMessage.getCommand() == WashingMessage.TEMP_SET) {
            heatTo = currentMessage.getValue();
            shouldHeat = true;
            isHeating = true;
            io.heat(true);
            currentMessage = null;
          } else if (currentMessage.getCommand() == WashingMessage.TEMP_IDLE) {
            io.heat(false);
            shouldHeat = false;
            isHeating = false;
            currentMessage.getSender().send(new WashingMessage(this, WashingMessage.ACKNOWLEDGMENT));
            currentMessage = null;
          }

        }
        if (shouldHeat) {
          if (heatTo - io.getTemperature() < mu && isHeating) {
            System.out.println("Stop heat");
            if (notifyBack != null) {
              notifyBack.getSender().send(new WashingMessage(this, WashingMessage.ACKNOWLEDGMENT));
              notifyBack = null;
            }
            io.heat(false);
            isHeating = false;
          } else if (io.getTemperature() - (heatTo - 1.9) < ml && !isHeating) {
            io.heat(true);
            isHeating = true;
          }
        }

      }
    } catch (InterruptedException e) {
      // This thread should not be interrupted
      throw new Error(e);
    }
  }
}