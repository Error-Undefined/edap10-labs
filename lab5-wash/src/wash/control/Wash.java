package wash.control;

import actor.ActorThread;
import sun.misc.Unsafe;
import wash.io.WashingIO;
import wash.simulation.WashingSimulator;

public class Wash {

  public static void main(String[] args) throws InterruptedException {
    WashingSimulator sim = new WashingSimulator(Settings.SPEEDUP);

    WashingIO io = sim.startSimulation();

    TemperatureController temp = new TemperatureController(io);
    WaterController water = new WaterController(io);
    SpinController spin = new SpinController(io);

    temp.setName("Temp thread");
    water.setName("Water thread");
    spin.setName("Spin thread");

    temp.start();
    water.start();
    spin.start();
    ActorThread<WashingMessage> currentProgram = new WashingProgram3(io, temp, water, spin);

    while (true) {
      int n = io.awaitButton();
      System.out.println("user selected program " + n);

      if (n == 1) {
        currentProgram = new WashingProgram1(io, temp, water, spin);
        currentProgram.setName("Program thread");
        currentProgram.start();
      } else if (n == 2) {
        currentProgram = new WashingProgram2(io, temp, water, spin);
        currentProgram.setName("Program thread");
        currentProgram.start();
      } else if (n == 3) {
        currentProgram = new WashingProgram3(io, temp, water, spin);
        currentProgram.setName("Program thread");
        currentProgram.start();
      } else if (n == 0) {
        currentProgram.interrupt();
      }
    }
  }
};
