import java.util.List;

public class Fn implements Callable {
  private final Stmt.FnStmt declaration;

  public Fn(Stmt.FnStmt declaration) {
    this.declaration = declaration;
  }

  @Override
  public int arity() {
    return this.declaration.params.size();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> args) throws Exception {
    Environment env = new Environment(interpreter.globals);

    for (int i = 0; i < declaration.params.size(); i++) {
      env.define(declaration.params.get(i).lexeme(), args.get(i));
    }

    try {
      interpreter.executeBlock(declaration.body, env);
    }
    catch (Util.Return r) {
      return r.value;
    }
    
    return null;
  }

  public String toString() {
    return "<fn " + this.declaration.name.lexeme() + ">";
  }
}
