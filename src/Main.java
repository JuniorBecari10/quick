import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class Main {
  public static final String VERSION = "1.1";

  public static void main(String[] args) {
    if (args.length == 0) {
      Repl.repl();
      return;
    }

    if (args.length > 2) {
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

      new Interpreter().interpret(stmts, false);
    }
    catch (IOException ee) {
      System.out.println("File '" + args[0] + "' doesn't exist");
    }
    catch (Exception e) {
      return;
    }
  }
}
