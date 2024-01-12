import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Main {
  public static final String VERSION = "1.1";

  public static void main(String[] args) {
    try {
      List<Token> tokens = new Lexer("""
        fn bubbleSort(array) {
          let numEl = len(array) - 1
          let ordenado = false
        
          while !ordenado {
            ordenado = true
        
            loop i in 0..numEl {
              if array[i] > array[i + 1] {
                let temp = array[i]
                array[i] = array[i + 1]
                array[i + 1] = temp
        
                ordenado = false
              }
            }
          }
        
          return array
        }
        
        fn swap(a, b) {
          let temp = *a
          *a = *b
          *b = temp
        }
        
        let array = [3, 6, 2, 7, 1, 4]
        println(bubbleSort(array))
          """).lex();
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
