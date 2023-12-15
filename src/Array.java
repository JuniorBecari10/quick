import java.util.List;

public class Array {
  public List<Object> array;

  public Array(List<Object> array) {
    this.array = array;
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder("[");

    int i = 0;
    for (Object o : this.array) {
      b.append(Util.stringify(o));

      if (i < this.array.size() - 1)
        b.append(", ");

      i++;
    }

    b.append("]");

    return b.toString();
  }
}
