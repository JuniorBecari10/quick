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
      execute("*x = 10");
    }
    catch (Exception e) {
      System.out.println("File '" + args[0] + "' doesn't exist");
    }
  }

  public static void execute(String content) {
    try {
      List<Token> tokens = new Lexer(content).lex();
      List<Stmt> stmts = new Parser(tokens).parse();
      //System.out.println(((Expr.AssignExpr) ((Stmt.ExprStmt) stmts.get(0)).expr).isRef);
      new Interpreter(stmts).interpret();
    }
    catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }
}
