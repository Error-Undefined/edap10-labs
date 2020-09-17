package monitor;

import java.util.LinkedList;
import java.util.Queue;

import lift.LiftView;

public class LiftMonitor {
  private int floor;
  private boolean inMotion;
  private int direction;
  private int passengersInside;

  private Queue<Integer> inQueue;
  private Queue<Integer> outQueue;

  private LiftView view;

  public LiftMonitor(LiftView view) {
    this.view = view;
    this.floor = 0;
    this.inMotion = false;
    this.direction = 1;
    this.passengersInside = 0;
    this.inQueue = new LinkedList<>();
    this.outQueue = new LinkedList<>();
  }

}
