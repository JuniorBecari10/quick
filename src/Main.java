import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class Main {
  public static void main(String[] args) {
    if (args.length != 1) {
      System.out.println("Usage: quick <file>");
      return;
    }

    try {
      execute("""
        let a = "global"
        {
          fn show() {
            println(a)
          }

          show()
          let a = "block"
          show()
        }
          """);
    }
    catch (Exception e) {
      System.out.println("File '" + args[0] + "' doesn't exist");
    }
  }

  public static void execute(String content) {
    try {
      List<Token> tokens = new Lexer(content).lex();
      List<Stmt> stmts = new Parser(tokens).parse();
      new Interpreter(stmts).interpret();
    }
    catch (Exception e) {
      return;
    }
  }
}
