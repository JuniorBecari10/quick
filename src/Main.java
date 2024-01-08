import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class Main {
  public static final String VERSION = "1.0";

  public static void main(String[] args) {
    if (args.length < 1) {
      System.out.println("Usage: quick <file> [args] | (-v | --version)");
      return;
    }

    if (args[0].equals("-v") || args[0].equals("--version")) {
      System.out.println("Quick v" + VERSION);
      System.out.println("Made by JuniorBecari10.");

      return;
    }

    Util.args = Arrays.copyOfRange(args, 1, args.length);

    try {
      File f = new File(args[0]);

      List<Token> tokens = new Lexer(Files.readString(f.toPath())).lex();
      List<Stmt> stmts = new Parser(tokens).parse();

      new Interpreter(stmts).interpret();
    }
    catch (IOException ee) {
      System.out.println("File ");
    }
    catch (Exception e) {
      return;
    }
  }
}
