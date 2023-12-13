import java.util.List;

public abstract class Stmt {
  public interface StmtVisitor<R> {
    R visitBlockStmt(BlockStmt stmt) throws Exception;
    R visitBreakStmt(BreakStmt stmt) throws Exception;
    R visitContinueStmt(ContinueStmt stmt) throws Exception;
    R visitExprStmt(ExprStmt stmt) throws Exception;
    R visitFnStmt(FnStmt stmt) throws Exception;
    R visitIfStmt(IfStmt stmt) throws Exception;
    R visitLetStmt(LetStmt stmt) throws Exception;
    R visitLoopStmt(LoopStmt stmt) throws Exception;
    R visitReturnStmt(ReturnStmt stmt) throws Exception;
    R visitWhileStmt(WhileStmt stmt) throws Exception;
  }

  public final Position pos;

  public Stmt(Position pos) {
    this.pos = pos;
  }

  public abstract <R> R accept(StmtVisitor<R> visitor) throws Exception;

  public static class BlockStmt extends Stmt {
    final List<Stmt> statements;

    public BlockStmt(Position pos, List<Stmt> statements) {
      super(pos);
      this.statements = statements;
    }

    public <R> R accept(StmtVisitor<R> visitor) throws Exception {
      return visitor.visitBlockStmt(this);
    }
  }

  public static class BreakStmt extends Stmt {
    public BreakStmt(Position pos) {
      super(pos);
    }

    public <R> R accept(StmtVisitor<R> visitor) throws Exception {
      return visitor.visitBreakStmt(this);
    }
  }

  public static class ContinueStmt extends Stmt {
    public ContinueStmt(Position pos) {
      super(pos);
    }

    public <R> R accept(StmtVisitor<R> visitor) throws Exception {
      return visitor.visitContinueStmt(this);
    }
  }

  public static class FnStmt extends Stmt implements FnDecl {
    final Token name;
    final List<Token> params;
    final List<Stmt> body;

    public FnStmt(Position pos, Token name, List<Token> params, List<Stmt> body) {
      super(pos);

      this.name = name;
      this.params = params;
      this.body = body;
    }

    public <R> R accept(StmtVisitor<R> visitor) throws Exception {
      return visitor.visitFnStmt(this);
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

  public static class ExprStmt extends Stmt {
    final Expr expr;

    public ExprStmt(Position pos, Expr expr) {
      super(pos);
      this.expr = expr;
    }

    public <R> R accept(StmtVisitor<R> visitor) throws Exception {
      return visitor.visitExprStmt(this);
    }
  }

  public static class IfStmt extends Stmt {
    final Expr condition;
    final BlockStmt thenBranch;
    final BlockStmt elseBranch;

    public IfStmt(Position pos, Expr condition, BlockStmt thenBranch, BlockStmt elseBranch) {
      super(pos);

      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseBranch = elseBranch;
    }

    public <R> R accept(StmtVisitor<R> visitor) throws Exception {
      return visitor.visitIfStmt(this);
    }
  }

  public static class InclStmt extends Stmt {
    final Token mod;

    public InclStmt(Position pos, Token mod) {
      super(pos);
      this.mod = mod;
    }

    public <R> R accept(StmtVisitor<R> visitor) throws Exception {
      return null;
    }
  }

  public static class LetStmt extends Stmt {
    final Token name;
    final Expr value;

    public LetStmt(Position pos, Token name, Expr value) {
      super(pos);

      this.name = name;
      this.value = value;
    }

    public <R> R accept(StmtVisitor<R> visitor) throws Exception {
      return visitor.visitLetStmt(this);
    }
  }

  public static class LoopStmt extends Stmt {
    final BlockStmt block;

    public LoopStmt(Position pos, BlockStmt block) {
      super(pos);
      this.block = block;
    }

    public <R> R accept(StmtVisitor<R> visitor) throws Exception {
      return visitor.visitLoopStmt(this);
    }
  }

  public static class ReturnStmt extends Stmt {
    final Expr value;

    public ReturnStmt(Position pos, Expr value) {
      super(pos);
      this.value = value;
    }

    public <R> R accept(StmtVisitor<R> visitor) throws Exception {
      return visitor.visitReturnStmt(this);
    }
  }

  public static class WhileStmt extends Stmt {
    final Expr condition;
    final BlockStmt block;

    public WhileStmt(Position pos, Expr condition, BlockStmt block) {
      super(pos);

      this.condition = condition;
      this.block = block;
    }

    public <R> R accept(StmtVisitor<R> visitor) throws Exception {
      return visitor.visitWhileStmt(this);
    }
  }
}
