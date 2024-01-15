import java.util.List;

public class Repl {
  public static void repl() {
    Interpreter interpreter = new Interpreter();

    System.out.println("Quick REPL - v" + Main.VERSION + "\n");
    System.out.println("Type 'exit' to exit");
    System.out.println("Type 'help' for help\n");

    while (true) {
      try {
        System.out.print("> ");
        String input = System.console().readLine();

        switch (input) {
          case "exit" -> { return; }
          case "help" -> {
            System.out.println("Quick REPL | Help\n");

            System.out.println(" 'exit'          - Exits the REPL");
            System.out.println(" 'cls' / 'clear' - Clears the screen");
            System.out.println(" 'reset'         - Resets the current environment, clearing all declared variables");

            System.out.println("\nUse '\\' before a command to interpret it as Quick code");
            continue;
          }

          case "cls", "clear" -> {
            Util.clearScreen();
            continue;
          }

          case "reset" -> {
            interpreter = new Interpreter();
            System.out.println("Environment reset.");
            continue;
          }
        }

        if (input.startsWith("\\")) input = input.substring(1);
        
        List<Token> tokens = new Lexer(input).lex();
        List<Stmt> stmts = new Parser(tokens).parse();
        
        interpreter.interpret(stmts, true);
      } catch (Exception e) {
        continue;
      }
    }
  }
}
