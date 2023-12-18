import java.util.List;

public class Main {
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: quick <file>");
      return;
    }

    if (!args[0].endsWith(Modules.FILE_EXT)) {
      System.out.println("File name should end with '" + Modules.FILE_EXT + "'");
      return;
    }

    try {
      List<Stmt> stmts = Modules.readFile(args[0]);
      Modules.execute(stmts);
    }
    catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }
}
