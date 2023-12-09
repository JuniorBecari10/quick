import java.util.List;

public class Main {
  public static void main(String[] args) {
    try {
      List<Token> tokens = new Lexer("let name = input(\"name: \")\nprintln(name)").lex();
      List<Stmt> stmts = new Parser(tokens).parse();
      new Interpreter(stmts).interpret();
    }
    catch (Exception e) {
      e.printStackTrace();
      return;
    }
  }
}
