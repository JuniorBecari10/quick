import java.util.List;

public class Function implements Callable {
  private final FnDecl declaration;
  private final Environment closure;

  public Function(FnDecl declaration, Environment closure) {
    this.declaration = declaration;
    this.closure = closure;
  }

  @Override
  public int arity() {
    return this.declaration.params().size();
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> args) throws Exception {
    Environment env = new Environment(this.closure);

    for (int i = 0; i < declaration.params().size(); i++) {
      env.define(declaration.params().get(i).lexeme(), args.get(i));
    }

    try {
      interpreter.executeBlock(declaration.body(), env);
    }
    catch (Util.Return r) {
      return r.value;
    }
    
    return null;
  }

  public String toString() {
    return "<fn>";
  }
}
