package monitor;

import java.util.Arrays;

import lift.LiftView;

public class LiftMonitor {
  private int floor;
  private boolean inMotion;
  private int direction;
  private int passengersInside;

  private int totalWaiting;

  private int canEnterAtFloor;
  private int willEnterAtFloor;
  private int hasEnteredAtFloor;

  private static final int MAX_PASSENGERS = 4;

  private int[] inQueue;
  private int[] outQueue;

  private LiftView view;

  public LiftMonitor(LiftView view) {
    this.view = view;
    this.floor = 0;
    this.inMotion = false;
    this.direction = 1;
    this.passengersInside = 0;
    this.inQueue = new int[7];
    this.outQueue = new int[7];
    this.totalWaiting = 0;
    this.canEnterAtFloor = 4;

    Arrays.fill(inQueue, 0);
    Arrays.fill(outQueue, 0);
  }

  public LiftView getView() {
    return view;
  }

  public synchronized void getEnterPermit(int waitFloor, int endFloor) throws InterruptedException {
    inQueue[waitFloor]++;
    totalWaiting++;
    notifyAll();

    int passengerDirection = Math.abs(waitFloor - endFloor) / (waitFloor - endFloor);

    while (passengersInside == MAX_PASSENGERS || floor != waitFloor || inMotion
        || hasEnteredAtFloor == willEnterAtFloor) {
      wait();
    }

    // When we reach here we have successfully gotten an enter permit
    passengersInside++;
    totalWaiting--;

  }

  public synchronized void updateAfterEnter(int waitFloor, int endFloor) {
    hasEnteredAtFloor++;
    inQueue[waitFloor]--;
    outQueue[endFloor]++;
    notifyAll();
  }

  public synchronized void getExitPermit(int endFloor) throws InterruptedException {
    while (floor != endFloor || inMotion) {
      wait();
    }
  }

  public synchronized void updateAfterExit(int endFloor) {
    passengersInside--;
    outQueue[endFloor]--;
    notifyAll();
  }

  /**
   * Acquires a permit to move the lift; blocks passengers from trying to enter
   */
  public synchronized void getMovePermit() throws InterruptedException {
    while (totalWaiting == 0 && passengersInside == 0) {
      wait();
    }
    inMotion = true;
  }

  /**
   * Releases the permit to move the lift, allows passengers to enter.
   */
  public synchronized void releaseMovePermit() {
    inMotion = false;
  }

  public synchronized Floors getFloors() throws InterruptedException {
    int nextFloor = floor + direction;
    Floors f = new Floors(floor, nextFloor);
    floor = nextFloor;
    if (floor == 0 || floor == 6) {
      direction = -direction;
    }
    return f;
  }

  public synchronized void allowPassengerTransfer() throws InterruptedException {
    if ((outQueue[floor] == 0 && passengersInside == MAX_PASSENGERS) || (inQueue[floor] == 0 && outQueue[floor] == 0)) {
      return;
    }

    hasEnteredAtFloor = 0;

    canEnterAtFloor = MAX_PASSENGERS - passengersInside + outQueue[floor];
    willEnterAtFloor = Math.min(canEnterAtFloor, inQueue[floor]);

    // passengersInside -= willEnterAtFloor;

    view.openDoors(floor);
    notifyAll();

    while (outQueue[floor] != 0 || (inQueue[floor] != 0 && passengersInside < MAX_PASSENGERS)
        || hasEnteredAtFloor < willEnterAtFloor) {
      wait();
    }

    view.closeDoors();
  }
}
