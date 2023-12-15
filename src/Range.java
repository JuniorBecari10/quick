public class Range implements Iterable {
  public double start;
  public double end;
  public double step;

  private double counter;

  public Range(double start, double end, double step) {
    this.start = start;
    this.end = end;
    this.step = step;

    this.counter = start - step;
  }

  @Override
  public String toString() {
    return Util.stringify(this.start) + ":" + Util.stringify(this.end) + "," + Util.stringify(this.step);
  }

  @Override
  public boolean hasNext() {
    return this.counter + this.step <= this.end;
  }

  @Override
  public Object next() {
    double res = this.counter + this.step;
    this.counter += this.step;

    return res;
  }
}
