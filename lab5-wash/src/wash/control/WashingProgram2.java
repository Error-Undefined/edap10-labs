package wash.control;

import actor.ActorThread;
import wash.io.WashingIO;

/**
 * Program 3 for washing machine. This also serves as an example of how washing
 * programs can be structured.
 * 
 * This short program stops all regulation of temperature and water levels,
 * stops the barrel from spinning, and drains the machine of water.
 * 
 * It can be used after an emergency stop (program 0) or a power failure.
 */
public class WashingProgram2 extends ActorThread<WashingMessage> {

  private WashingIO io;
  private ActorThread<WashingMessage> temp;
  private ActorThread<WashingMessage> water;
  private ActorThread<WashingMessage> spin;

  public WashingProgram2(WashingIO io, ActorThread<WashingMessage> temp, ActorThread<WashingMessage> water,
      ActorThread<WashingMessage> spin) {
    this.io = io;
    this.temp = temp;
    this.water = water;
    this.spin = spin;
  }

  @Override
  public void run() {
    try {
      // Lock the hatch
      io.lock(true);

      // Fill with water
      water.send(new WashingMessage(this, WashingMessage.WATER_FILL, 10.0));
      System.out.println("got ack of fill " + receive());
      water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));

      // Prewash : set temperature to 40
      temp.send(new WashingMessage(this, WashingMessage.TEMP_SET, 40));
      System.out.println("got ack of heat " + receive());

      // Spin the barrel
      spin.send(new WashingMessage(this, WashingMessage.SPIN_SLOW));
      receive();
      // Spin for 20 simulated minutes (one minute == 60000 milliseconds)
      Thread.sleep(20 * 60000 / Settings.SPEEDUP);
      // Stop spinning
      spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));
      receive();

      // Turn off heating
      temp.send(new WashingMessage(this, WashingMessage.TEMP_IDLE));
      receive();

      // Empty of water
      water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN));
      System.out.println("got ack of empty " + receive());
      water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));

      // Fill with water
      water.send(new WashingMessage(this, WashingMessage.WATER_FILL, 10.0));
      System.out.println("got ack of fill " + receive());
      water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));

      // Set temperature to 60
      temp.send(new WashingMessage(this, WashingMessage.TEMP_SET, 60));
      System.out.println("got ack of heat " + receive());

      // Instruct SpinController to rotate barrel slowly, back and forth
      // Expect an acknowledgment in response.
      System.out.println("setting SPIN_SLOW...");
      spin.send(new WashingMessage(this, WashingMessage.SPIN_SLOW));
      WashingMessage ack1 = receive();
      System.out.println("washing program 1 got " + ack1);

      // Spin for 30 simulated minutes (one minute == 60000 milliseconds)
      Thread.sleep(30 * 60000 / Settings.SPEEDUP);

      // Turn off heating
      temp.send(new WashingMessage(this, WashingMessage.TEMP_IDLE));
      receive();

      // Instruct SpinController to stop spin barrel spin.
      // Expect an acknowledgment in response.
      System.out.println("setting SPIN_OFF...");
      spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));
      WashingMessage ack2 = receive();
      System.out.println("washing program 1 got " + ack2);

      // Empty water
      water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN));
      System.out.println("got ack of empty " + receive());
      water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));

      // Rinse 5 times in cold water
      for (int i = 0; i < 5; i++) {
        // Fill with water
        water.send(new WashingMessage(this, WashingMessage.WATER_FILL, 10.0));
        System.out.println("got ack of fill " + receive());
        water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));

        // Spin slowly
        spin.send(new WashingMessage(this, WashingMessage.SPIN_SLOW));
        receive();
        // Rinse 2 minutes
        Thread.sleep(2 * 60000 / Settings.SPEEDUP);
        // Stop spin
        spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));
        receive();

        // Empty the water
        water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN));
        System.out.println("got ack of empty " + receive());
        water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));
      }

      // Start centrifuge part
      spin.send(new WashingMessage(this, WashingMessage.SPIN_FAST));
      receive();

      water.send(new WashingMessage(this, WashingMessage.WATER_DRAIN));
      receive();

      Thread.sleep(5 * 60000 / Settings.SPEEDUP);

      spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));
      receive();

      water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));
      receive();

      // Now that the barrel has stopped and the water is empty, it is safe to open
      // the hatch.
      io.lock(false);

      System.out.println("Program complete");

    } catch (InterruptedException e) {

      // If we end up here, it means the program was interrupt()'ed:
      // set all controllers to idle
      water.send(new WashingMessage(this, WashingMessage.WATER_IDLE));
      spin.send(new WashingMessage(this, WashingMessage.SPIN_OFF));
      temp.send(new WashingMessage(this, WashingMessage.TEMP_IDLE));
      System.out.println("washing program interrupted");
    }
  }
}
