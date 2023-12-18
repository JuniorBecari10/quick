import java.util.Arrays;
import java.util.List;

public class Main {
  public static final String VERSION = "1.0";

  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Usage: quick <file> [args] (-v | --version)");
      return;
    }

    if (args[0].equals("-v") || args[0].equals("--version")) {
      System.out.println("Quick v" + VERSION);
      System.out.println("Made by JuniorBecari10.");

      return;
    }

    if (!args[0].endsWith(Modules.FILE_EXT)) {
      System.out.println("File name should end with '" + Modules.FILE_EXT + "'");
      return;
    }

    Util.args = Arrays.copyOfRange(args, 1, args.length);

    try {
      List<Stmt> stmts = Modules.readFile(args[0]);
      Modules.execute(stmts);
    }
    catch (Exception e) {
      return;
    }
  }
}
