import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Modules {
  public static final String FILE_EXT = ".qk";
  public static final List<String> included = new ArrayList<>();

  public static List<Stmt> readFile(String name) {
    return readFile(new File(name));
  }

  public static List<Stmt> readFile(File file) {
    try {
      List<Token> tokens = new Lexer(Files.readString(file.toPath())).lex();
      List<Stmt> stmts = new Parser(tokens).parse();

      String[] split = file.getName().split(".");
      included.add(split.length > 0 ? split[0] : file.getName());
      return stmts;
    }
    catch (Exception e) {
      e.printStackTrace();
      System.out.println("File '" + file.getName() + "' doesn't exist");
      return null;
    }
  }

  public static void execute(List<Stmt> stmts) {
    try {
      new Interpreter(stmts).interpret();
    }
    catch (Exception e) {
      return;
    }
  }
}
