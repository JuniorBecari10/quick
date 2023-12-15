import java.util.List;

public class Array implements Iterable {
  public List<Object> array;
  private int counter = 0;

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

  @Override
  public boolean hasNext() {
    return this.counter < this.array.size();
  }

  @Override
  public Object next() {
    return this.array.get(this.counter++);
  }
}
