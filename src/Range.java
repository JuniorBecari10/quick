public class Range {
  public double start;
  public double end;
  public double step;

  public Range(double start, double end, double step) {
    this.start = start;
    this.end = end;
    this.step = step;
  }

  @Override
  public String toString() {
    return Util.stringify(this.start) + ":" + Util.stringify(this.end) + "," + Util.stringify(this.step);
  }
}
