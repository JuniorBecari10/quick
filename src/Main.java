import java.util.List;

public class Main {
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: quick <file>");
      return;
    }

    try {
      List<Stmt> stmts = Modules.readFile(args[0]);
      Modules.execute(stmts);
    }
    catch (Exception e) {
      return;
    }
  }
}
