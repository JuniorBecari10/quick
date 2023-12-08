public class Position {
  public int line;
  public int col;

  public Position(int line, int col) {
    this.line = line;
    this.col = col;
  }

  public Position(Position other) {
    this(other.line, other.col);
  }

  @Override
  public String toString() {
    return this.line + ":" + this.col;
  }
}
