import java.util.Optional;
import java.util.function.Supplier;

public class Util {
  public static <T> Optional<T> supressException(Supplier<T> supplier) {
    try {
      T value = supplier.get();
      return Optional.ofNullable(value);
    }
    catch (Exception e) {
      return Optional.empty();
    }
  }

  public static void printError(String message, Position pos) throws Exception {
    System.out.printf("Error in %d:%d | %s\n", pos.line + 1, pos.col + 1, message);
    throw new Exception();
  }
}