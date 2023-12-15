import java.util.List;

public abstract class Expr {
  public interface ExprVisitor<R> {
    R visitArrayExpr(ArrayExpr expr) throws Exception;
    R visitAssignExpr(AssignExpr expr) throws Exception;
    R visitAssignIndexExpr(AssignIndexExpr expr) throws Exception;
    R visitBinaryExpr(BinaryExpr expr) throws Exception;
    R visitCallExpr(CallExpr expr) throws Exception;
    R visitFnExpr(FnExpr expr) throws Exception;
    R visitGroupingExpr(GroupingExpr expr) throws Exception;
    R visitIndexExpr(IndexExpr expr) throws Exception;
    R visitLiteralExpr(LiteralExpr expr) throws Exception;
    R visitRangeExpr(RangeExpr expr) throws Exception;
    R visitTernaryExpr(TernaryExpr expr) throws Exception;
    R visitUnaryExpr(UnaryExpr expr) throws Exception;
    R visitVariableExpr(VariableExpr expr) throws Exception;
  }

  public final Position pos;

  public Expr(Position pos) {
    this.pos = pos;
  }

  public abstract <R> R accept(ExprVisitor<R> visitor) throws Exception;

  // TODO! add arrays and range in parser, and add Iterable interface
  public static class ArrayExpr extends Expr {
    final List<Expr> items;

    public ArrayExpr(Position pos, List<Expr> items) {
      super(pos);
      this.items = items;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitArrayExpr(this);
    }
  }

  public static class AssignExpr extends Expr {
    final Token name;
    final Expr value;
    final boolean isRef;

    public AssignExpr(Position pos, Token name, Expr value, boolean isRef) {
      super(pos);

      this.name = name;
      this.value = value;
      this.isRef = isRef;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitAssignExpr(this);
    }
  }

  public static class AssignIndexExpr extends Expr {
    final Token name;
    final Expr index;
    final Expr value;

    public AssignIndexExpr(Position pos, Token name, Expr index, Expr value) {
      super(pos);

      this.name = name;
      this.index = index;
      this.value = value;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitAssignIndexExpr(this);
    }
  }

  public static class BinaryExpr extends Expr {
    final Expr left;
    final Token operator;
    final Expr right;

    public BinaryExpr(Position pos, Expr left, Token operator, Expr right) {
      super(pos);

      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitBinaryExpr(this);
    }
  }

  public static class CallExpr extends Expr {
    final Expr callee;
    final List<Expr> args;

    public CallExpr(Position pos, Expr callee, List<Expr> args) {
      super(pos);

      this.callee = callee;
      this.args = args;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitCallExpr(this);
    }
  }

  public static class FnExpr extends Expr implements FnDecl {
    final List<Token> params;
    final List<Stmt> body;

    public FnExpr(Position pos, List<Token> params, List<Stmt> body) {
      super(pos);

      this.params = params;
      this.body = body;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitFnExpr(this);
    }

    @Override
    public List<Token> params() {
      return this.params;
    }

    @Override
    public List<Stmt> body() {
      return this.body;
    }
  }

  public static class GroupingExpr extends Expr {
    final Expr expr;

    public GroupingExpr(Position pos, Expr expr) {
      super(pos);

      this.expr = expr;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitGroupingExpr(this);
    }
  }

  public static class IndexExpr extends Expr {
    final Expr array;
    final Expr index;

    public IndexExpr(Position pos, Expr expr, Expr index) {
      super(pos);

      this.array = expr;
      this.index = index;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitIndexExpr(this);
    }
  }

  public static class LiteralExpr extends Expr {
    final Object value;

    public LiteralExpr(Position pos, Object value) {
      super(pos);

      this.value = value;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitLiteralExpr(this);
    }
  }

  public static class RangeExpr extends Expr {
    final Expr start;
    final Expr end;
    final Expr step;

    public RangeExpr(Position pos, Expr start, Expr end, Expr step) {
      super(pos);

      this.start = start;
      this.end = end;
      this.step = step;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitRangeExpr(this);
    }
  }

  public static class TernaryExpr extends Expr {
    final Expr condition;
    final Expr thenBranch;
    final Expr elseBranch;

    public TernaryExpr(Position pos, Expr condition, Expr thenBranch, Expr elseBranch) {
      super(pos);

      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitTernaryExpr(this);
    }
  }

  public static class UnaryExpr extends Expr {
    final Token operator;
    final Expr right;

    public UnaryExpr(Position pos, Token operator, Expr right) {
      super(pos);

      this.operator = operator;
      this.right = right;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitUnaryExpr(this);
    }
  }

  public static class VariableExpr extends Expr {
    final Token name;

    public VariableExpr(Position pos, Token name) {
      super(pos);

      this.name = name;
    }

    public <R> R accept(ExprVisitor<R> visitor) throws Exception {
      return visitor.visitVariableExpr(this);
    }
  }
}
