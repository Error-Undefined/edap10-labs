package factory.controller;

import java.util.concurrent.Semaphore;

import factory.model.DigitalSignal;
import factory.model.WidgetKind;
import factory.simulation.Painter;
import factory.simulation.Press;
import factory.swingview.Factory;

/**
 * Implementation of the ToolController interface, to be used for the Widget
 * Factory lab.
 * 
 * @see ToolController
 */
public class LabToolController implements ToolController {
  private final DigitalSignal conveyor, press, paint;
  private final long pressingMillis, paintingMillis;

  private Semaphore signal;

  public LabToolController(DigitalSignal conveyor, DigitalSignal press, DigitalSignal paint, long pressingMillis,
      long paintingMillis) {
    this.conveyor = conveyor;
    this.press = press;
    this.paint = paint;
    this.pressingMillis = pressingMillis;
    this.paintingMillis = paintingMillis;

    signal = new Semaphore(2);
  }

  @Override
  public void onPressSensorHigh(WidgetKind widgetKind) throws InterruptedException {
    //
    // : you will need to modify this method.
    //
    // Note that this method can be called concurrently with onPaintSensorHigh
    // (that is, in a separate thread).
    //
    if (widgetKind == WidgetKind.BLUE_RECTANGULAR_WIDGET) {
      stopConveyor();
      press.on();
      Thread.sleep(pressingMillis);
      press.off();
      Thread.sleep(pressingMillis); // press needs this time to retract
      startConveyor();
    }
  }

  @Override
  public void onPaintSensorHigh(WidgetKind widgetKind) throws InterruptedException {
    //
    // you will need to modify this method.
    //
    // Note that this method can be called concurrently with onPressSensorHigh
    // (that is, in a separate thread).
    //
    if (widgetKind == WidgetKind.ORANGE_ROUND_WIDGET) {
      stopConveyor();
      paint.on();
      Thread.sleep(paintingMillis);
      paint.off();
      startConveyor();
    }
  }

  private synchronized void startConveyor() {
    signal.release();
    if (conveyor.isLow() && signal.availablePermits() == 2)
      conveyor.on();

  }

  private synchronized void stopConveyor() throws InterruptedException {
    signal.acquire();
    if (conveyor.isHigh())
      conveyor.off();

  }

  // -----------------------------------------------------------------------

  public static void main(String[] args) {
    Factory factory = new Factory();
    ToolController toolController = new LabToolController(factory.getConveyor(), factory.getPress(), factory.getPaint(),
        Press.PRESSING_MILLIS, Painter.PAINTING_MILLIS);
    factory.startSimulation(toolController);
  }
}
