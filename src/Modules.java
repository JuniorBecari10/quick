import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Modules {
  public static final String FILE_EXT = ".qk";
  public static final List<String> included = new ArrayList<>();

  public static String baseFolder = "";
  public static String currentFileName;

  public static List<Stmt> readFile(String name) throws Exception {
    return readFile(new File(baseFolder + name));
  }

  public static List<Stmt> readFile(File file) throws Exception {
    included.add(file.getName());
    currentFileName = file.getName();
    
    if (baseFolder.equals("")) {
      String[] split = file.getAbsolutePath().split("\\" + File.separator);
      List<String> pathSplit = new ArrayList<>();

      for (String s : split)
        pathSplit.add(s);

      pathSplit.remove(pathSplit.size() - 1);
      baseFolder = String.join(File.separator, pathSplit);
    }

    try {
      List<Token> tokens = new Lexer(Files.readString(file.toPath())).lex();
      List<Stmt> stmts = new Parser(tokens).parse();
      return stmts;
    }
    catch (IOException e) {
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
