import java.util.ArrayList;
import java.util.List;

public class Interpreter implements Stmt.StmtVisitor<Void>, Expr.ExprVisitor<Object> {
  public final Environment globals = new Environment();
  private Environment environment = globals;
  
  private List<Stmt> statements;

  public Interpreter(List<Stmt> statements) {
    this.statements = statements;

    globals.define("clock", new Callable() {
      public int arity() { return 0; }

      public Object call(Interpreter interpreter, List<Object> args) {
        return (double) System.currentTimeMillis() / 1000.0;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("println", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        System.out.println(args.get(0));
        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("print", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        System.out.print(args.get(0));
        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("input", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        System.out.print(args.get(0));

        return System.console().readLine();
      }

      public String toString() { return "<native fn>"; }
    });
  }

  public void interpret() throws Exception {
    for (Stmt stmt : this.statements) {
      this.execute(stmt);
    }
  }

  // ---

  private void execute(Stmt stmt) throws Exception {
    try {
      stmt.accept(this);
    }
    catch (Util.Break b) {
      Util.printError("Cannot use 'break' outside a loop", stmt.pos);
    }
    catch (Util.Continue c) {
      Util.printError("Cannot use 'continue' outside a loop", stmt.pos);
    }
    catch (Util.Return r) {
      Util.printError("Cannot use 'return' outside a function", stmt.pos);
    }
  }

  public void executeBlock(List<Stmt> statements, Environment environment) throws Exception {
    Environment previous = this.environment;

    try {
      this.environment = environment;

      for (Stmt stmt : statements)
        this.execute(stmt);
    }
    finally {
      this.environment = previous;
    }
  }

  // ---
  
  private boolean isTruthy(Object obj) {
    if (obj == null) return false;
    if (obj instanceof Boolean) return (boolean) obj;

    return true;
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);
  }

  /*
  private void checkNumberOperand(Token operator, Object operand) throws Exception {
    if (operand instanceof Double) return;
    Util.printError("Operand must be a number", operator.pos());
  }
  */

  private void checkNumberOperands(Token operator, Object left, Object right) throws Exception {
    if (left instanceof Double && right instanceof Double) return;
    Util.printError("Operands must be numbers", operator.pos());
  }

  /*
  private Object lookUpVariable(Token name, Expr expr) throws Exception {
    return this.environment.get(name);
  }

  private String stringify(Object object) {
    if (object == null) return "nil";

    if (object instanceof Double) {
        String text = object.toString();
        
        if (text.endsWith(".0")) {
            text = text.substring(0, text.length() - 2);
        }
        
        return text;
    }

    return object.toString();
  }
  */

  // ---

  @Override
  public Void visitBlockStmt(Stmt.BlockStmt stmt) throws Exception {
    this.executeBlock(stmt.statements, this.environment);
    return null;
  }

  @Override
  public Void visitBreakStmt(Stmt.BreakStmt stmt) throws Exception {
    throw new Util.Break();
  }

  @Override
  public Void visitContinueStmt(Stmt.ContinueStmt stmt) throws Exception {
    throw new Util.Continue();
  }

  @Override
  public Void visitExprStmt(Stmt.ExprStmt stmt) throws Exception {
    this.evaluate(stmt.expr);
    return null;
  }

  @Override
  public Void visitFnStmt(Stmt.FnStmt stmt) throws Exception {
    Fn fn = new Fn(stmt);
    this.environment.define(stmt.name.lexeme(), fn);

    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.IfStmt stmt) throws Exception {
    if (this.isTruthy(this.evaluate(stmt.condition)))
      this.execute(stmt.thenBranch);
    else if (stmt.elseBranch != null)
      this.execute(stmt.elseBranch);
    
    return null;
  }

  @Override
  public Void visitLetStmt(Stmt.LetStmt stmt) throws Exception {
    Object value = this.evaluate(stmt.value);
    this.environment.define(stmt.name.lexeme(), value);

    return null;
  }

  @Override
  public Void visitLoopStmt(Stmt.LoopStmt stmt) throws Exception {
    while (true) {
      try {
        this.execute(stmt.block);
      }
      catch (Util.Break b) {
        break;
      }
      catch (Util.Continue c) {
        continue;
      }
    }

    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.ReturnStmt stmt) throws Exception {
    Object value = null;

    if (stmt.value != null)
      value = this.evaluate(stmt.value);

    throw new Util.Return(value);
  }

  @Override
  public Void visitWhileStmt(Stmt.WhileStmt stmt) throws Exception {
    while (this.isTruthy(this.evaluate(stmt.condition))){
      try {
        this.execute(stmt.block);
      }
      catch (Util.Break b) {
        break;
      }
      catch (Util.Continue c) {
        continue;
      }
    }

    return null;
  }

  // ---

  private Object evaluate(Expr expr) throws Exception {
    return expr.accept(this);
  }

  @Override
  public Object visitAssignExpr(Expr.AssignExpr expr) throws Exception {
    Object value = this.evaluate(expr.value);
    this.environment.assign(expr.name, value);

    return value;
  }

  @Override
  public Object visitBinaryExpr(Expr.BinaryExpr expr) throws Exception {
    Object left = this.evaluate(expr.left);
    Object right = this.evaluate(expr.right);

    switch (expr.operator.type()) {
      case DoubleEqual:
        return this.isEqual(left, right);
    
      case BangEqual:
        return !this.isEqual(left, right);

      // ---

      case Ampersand:
        return this.isTruthy(left) && this.isTruthy(right);

      case VerticalBar:
        return this.isTruthy(left) || this.isTruthy(right);

      // ---

      case Greater:
        this.checkNumberOperands(expr.operator, left, right);
        return (double) left > (double) right;

      case GreaterEqual:
        this.checkNumberOperands(expr.operator, left, right);
        return (double) left >= (double) right;

      case Less:
        this.checkNumberOperands(expr.operator, left, right);
        return (double) left < (double) right;

      case LessEqual:
        this.checkNumberOperands(expr.operator, left, right);
        return (double) left <= (double) right;

      // ---

      case Plus:
        if (left instanceof Double && right instanceof Double) {
          return (double)left + (double)right;
        }

        if (left instanceof String && right instanceof String) {
            return (String)left + (String)right;
        }

        Util.printError("Operand must be two numbers or two strings", expr.left.pos);
      
      case Minus:
        this.checkNumberOperands(expr.operator, left, right);
        return (double) left - (double) right;

      case Star:
        this.checkNumberOperands(expr.operator, left, right);
        return (double) left * (double) right;

      case Slash:
        this.checkNumberOperands(expr.operator, left, right);

        if ((double) right == 0.0)
          Util.printError("Cannot divide by zero", expr.left.pos);

        return (double) left <= (double) right;

      default:
        Util.printError("Invalid binary operator: '" + expr.operator.lexeme() + "'", expr.left.pos);
        return null;
    }
  }

  @Override
  public Object visitCallExpr(Expr.CallExpr expr) throws Exception {
    Object callee = this.evaluate(expr.callee);

    List<Object> args = new ArrayList<>();
    
    for (Expr arg : expr.args)
      args.add(this.evaluate(arg));
    
    if (!(callee instanceof Callable))
      Util.printError("Can only call functions", expr.callee.pos);

    Callable function = (Callable) callee;

    if (args.size() != function.arity())
      Util.printError("Expected " + function.arity() + " arguments, but got " + args.size() + " instead", expr.callee.pos);

    return function.call(this, args);
  }

  @Override
  public Object visitGroupingExpr(Expr.GroupingExpr expr) throws Exception {
    return this.evaluate(expr.expr);
  }

  @Override
  public Object visitLiteralExpr(Expr.LiteralExpr expr) throws Exception {
    return expr.value;
  }

  @Override
  public Object visitUnaryExpr(Expr.UnaryExpr expr) throws Exception {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'visitUnaryExpr'");
  }

  @Override
  public Object visitVariableExpr(Expr.VariableExpr expr) throws Exception {
    return this.environment.get(expr.name);
  }
}
