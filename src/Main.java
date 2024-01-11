import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
      List<Token> tokens = new Lexer(Files.readString(Paths.get(args[0]))).lex();
      List<Stmt> stmts = new Parser(tokens).parse();

      new Interpreter().interpret(stmts, false);
    }
    catch (IOException ee) {
      System.out.println("File '" + args[0] + "' doesn't exist in the '" + Paths.get("").toAbsolutePath().normalize().toString() + "' folder");
    }
    catch (Exception e) {
      return;
    }
  }
}
