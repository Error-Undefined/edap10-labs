package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

public class WaterController extends ActorThread<WashingMessage> {

  private final int waterDt = 1;

  private boolean shouldReply = false;

  private WashingIO io;

  private WashingMessage currentMessage;

  public WaterController(WashingIO io) {
    this.io = io;
    currentMessage = null;
  }

  @Override
  public void run() {
    try {

      for (;;) {
        WashingMessage m = receiveWithTimeout(waterDt * 1000 / Settings.SPEEDUP);

        if (m != null) {
          System.out.println("got: " + m);
          currentMessage = m;
          shouldReply = true;
        }

        if (currentMessage != null) {
          if (currentMessage.getCommand() == WashingMessage.WATER_FILL) {
            if (io.getWaterLevel() < currentMessage.getValue()) {
              io.fill(true);
            } else {
              if (shouldReply) {
                currentMessage.getSender().send(new WashingMessage(this, WashingMessage.ACKNOWLEDGMENT));
                shouldReply = false;
              }
              io.fill(false);

            }

          } else if (currentMessage.getCommand() == WashingMessage.WATER_DRAIN) {
            io.drain(true);
            if (io.getWaterLevel() == 0) {
              // io.drain(false);
              if (shouldReply) {
                currentMessage.getSender().send(new WashingMessage(this, WashingMessage.ACKNOWLEDGMENT));
                shouldReply = false;
              }
            } else {
              io.drain(true);
            }

          } else if (currentMessage.getCommand() == WashingMessage.WATER_IDLE) {
            io.fill(false);
            io.drain(false);
            currentMessage = null;
          }
        }
      }

    } catch (InterruptedException e) {
      throw new Error(e); // This should not happen
    }
  }
}
