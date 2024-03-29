import java.text.DecimalFormat;
import java.util.Optional;
import java.util.function.Supplier;

public class Util {
  public static class Break extends Exception {}
  public static class Continue extends Exception {}
  
  public static class Return extends Exception {
    public Object value;
    
    public Return(Object value) {
      this.value = value;
    }
  }
  
  public static String[] args;
  
  public static <T> Optional<T> supressException(Supplier<T> supplier) {
    try {
      T value = supplier.get();
      return Optional.ofNullable(value);
    }
    catch (Exception e) {
      return Optional.empty();
    }
  }
  
  public static String stringify(Object obj) {
    if (obj == null) return "nil";
    
    if (obj instanceof Double) {
      try {
        return numToStr((double) obj);
      }
      catch (Exception e) {
        return obj.toString();
      }
    }
    
    return obj.toString();
  }
  
  public static String numToStr(double num) {
    DecimalFormat f = new DecimalFormat("#.##");
    f.setDecimalSeparatorAlwaysShown(false);
    
    return f.format(num).replace(",", ".");
  }
  
  public static void printError(String message, Position pos) throws Exception {
    System.out.printf("Error in %d:%d | %s\n", pos.line + 1, pos.col + 1, message);
    throw new Exception();
  }
  
  public static void clearScreen() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
  }
}
