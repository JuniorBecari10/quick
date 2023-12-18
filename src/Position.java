public class Position {
  public int line;
  public int col;
  public String file;

  public Position(int line, int col, String file) {
    this.line = line;
    this.col = col;
    this.file = file;
  }

  public Position(Position other) {
    this(other.line, other.col, other.file);
  }

  @Override
  public String toString() {
    return this.line + ":" + this.col;
  }
}
