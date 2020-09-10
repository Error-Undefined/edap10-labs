package clock.data;

public class TimeStruct {
  public int hours, minutes, seconds;

  public TimeStruct(int hours, int minutes, int seconds) {
    this.hours = hours;
    this.minutes = minutes;
    this.seconds = seconds;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof TimeStruct)) {
      return false;
    }
    TimeStruct t = (TimeStruct) other;
    return hours == t.hours && minutes == t.minutes && seconds == t.seconds;
  }
}
