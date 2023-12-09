import java.util.ArrayList;
import java.util.List;

public class Parser {
  private final List<Token> tokens;
  private int current;

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
    this.current = 0;
  }

  public List<Stmt> parse() throws Exception {
    List<Stmt> statements = new ArrayList<>();

    while (!this.isAtEnd(0)) {
      statements.add(this.statement());
    }

    return statements;
  }

  // ---

  private Stmt statement() throws Exception {
    try {
      Token t = this.peek(0);

      if (this.match(TokenType.BreakKw)) return this.breakStmt(t);
      if (this.match(TokenType.ContinueKw)) return this.continueStmt(t);
      if (this.match(TokenType.FnKw)) return fnStmt(t);
      if (this.match(TokenType.IfKw)) return ifStmt(t);
      if (this.match(TokenType.LetKw)) return letStmt(t);
      if (this.match(TokenType.LoopKw)) return loopStmt(t);
      if (this.match(TokenType.ReturnKw)) return returnStmt(t);
      if (this.match(TokenType.WhileKw)) return whileStmt(t);

      return exprStmt(t);
    }
    catch (Exception e) {
      this.synchronize();
      return null;
    }
  }

  // ---

  private Stmt breakStmt(Token t) throws Exception {
    Stmt.BreakStmt stmt = new Stmt.BreakStmt(t.pos());

    this.consumeNewLine();
    return stmt;
  }

  private Stmt continueStmt(Token t) throws Exception {
    Stmt.ContinueStmt stmt = new Stmt.ContinueStmt(t.pos());

    this.consumeNewLine();
    return stmt;
  }

  private Stmt fnStmt(Token t) throws Exception {
    Token name = this.consume(TokenType.Identifier, "Expected function name");
    this.consume(TokenType.LParen, "Expected '(' after function name");

    List<Token> parameters = new ArrayList<>();

    if (!this.check(TokenType.RParen)) {
      do {
        parameters.add(this.consume(TokenType.Identifier, "Expected parameter name"));
      }
      while (this.match(TokenType.Comma));
    }

    this.consume(TokenType.RParen, "Expected ')' after parameter list");
    List<Stmt> body = this.block();

    return new Stmt.FnStmt(t.pos(), name, parameters, body);
  }

  // if condition { thenBranch } else/(if condition ...) { elseBranch } 
  private Stmt ifStmt(Token t) throws Exception {
    Expr condition = this.expr();

    Stmt.BlockStmt thenBranch = new Stmt.BlockStmt(this.peek(0).pos(), this.block());
    Stmt.BlockStmt elseBranch = null;

    if (this.match(TokenType.ElseKw)) {
      if (this.match(TokenType.IfKw)) {
        Stmt elseIf = this.ifStmt(this.peek(-1));

        List<Stmt> list = new ArrayList<>();
        list.add(elseIf);
        return new Stmt.IfStmt(t.pos(), condition, thenBranch, new Stmt.BlockStmt(elseIf.pos, list));
      }
      
      elseBranch = new Stmt.BlockStmt(this.peek(0).pos(), this.block());
    }

    return new Stmt.IfStmt(t.pos(), condition, thenBranch, elseBranch);
  }

  // let name = value
  private Stmt letStmt(Token t) throws Exception {
    Token name = this.consume(TokenType.Identifier, "Expected variable name after 'let', got '" + this.peek(0).lexeme() + "'");
    Expr value = null;

    if (this.match(TokenType.Equal))
      value = this.expr();
    else
      Util.printError("Expected '=' after variable name, got '" + this.peek(0).lexeme() + "'", this.peek(0).pos());
    
    this.consumeNewLine();
    return new Stmt.LetStmt(t.pos(), name, value);
  }

  private Stmt loopStmt(Token t) throws Exception {
    Stmt.BlockStmt block = new Stmt.BlockStmt(this.peek(0).pos(), this.block());

    return new Stmt.LoopStmt(t.pos(), block);
  }

  private Stmt returnStmt(Token t) throws Exception {
    Expr value = null;

    if (!this.check(TokenType.NewLine))
      value = this.expr();

    this.consumeNewLine();
    return new Stmt.ReturnStmt(t.pos(), value);
  }

  private Stmt whileStmt(Token t) throws Exception {
    Expr condition = this.expr();
    Stmt.BlockStmt block = new Stmt.BlockStmt(this.peek(0).pos(), this.block());

    return new Stmt.WhileStmt(t.pos(), condition, block);
  }

  private Stmt exprStmt(Token t) throws Exception {
    Expr expr = this.expr();

    this.consumeNewLine();
    return new Stmt.ExprStmt(t.pos(), expr);
  }

  // ---

  private List<Stmt> block() throws Exception {
    List<Stmt> statements = new ArrayList<>();

    if (this.match(TokenType.Arrow)) {
      statements.add(this.statement());
      return statements;
    }

    this.skipNewLines();
    consume(TokenType.LBrace, "Expected '{' before block, got '" + this.peek(0).type() + "'");
    this.skipNewLines();

    while (!this.check(TokenType.RBrace) && !this.isAtEnd(0))
      statements.add(this.statement());
    
    this.skipNewLines();
    this.consume(TokenType.RBrace, "Expected '}' after block, got '" + this.peek(0).type() + "'");
    this.skipNewLines();

    return statements;
  }

  // ---

  private Expr expr() throws Exception {
    return this.parseExpr(0);
  }


  /*
   * Precedence Order:
   * 
   * - Lowest
   * 
   * 0 - Assignment
   * 1 - Or
   * 2 - And
   * 3 - Equality
   * 4 - Comparison
   * 5 - Add, Sub
   * 6 - Mul, Div
   * 7 - Unary
   * 8 - Call
   * 9 - Primary
   * 
   * - Highest
   */
  private Expr parseExpr(int precedence) throws Exception {
    switch (precedence) {
      case 0: return this.assignment(precedence);
      case 1: return this.binary(precedence, TokenType.VerticalBar);
      case 2: return this.binary(precedence, TokenType.Ampersand);
      case 3: return this.binary(precedence, TokenType.DoubleEqual, TokenType.BangEqual);
      case 4: return this.binary(precedence, TokenType.Greater, TokenType.GreaterEqual, TokenType.Less, TokenType.LessEqual);
      case 5: return this.binary(precedence, TokenType.Plus, TokenType.Minus);
      case 6: return this.binary(precedence, TokenType.Star, TokenType.Slash);
      case 7: return this.unary(precedence, TokenType.Bang, TokenType.Minus, TokenType.Ampersand, TokenType.Star);
      case 8: return this.call(precedence);
      case 9: return this.primary();
    }
    // TODO! add index

    Util.printError("Invalid precedence: '" + precedence + "'", null);
    return null;
  }

  private Expr assignment(int precedence) throws Exception {
    Expr expr = this.parseExpr(precedence + 1);

    if (this.match(TokenType.Equal)) {
      Expr right = this.assignment(precedence); // this is because assignment is right-associative

      if (expr instanceof Expr.VariableExpr) {
        Token name = ((Expr.VariableExpr) expr).name;
        return new Expr.AssignExpr(expr.pos, name, right);
      }
      
      Util.printError("Invalid assignment target", expr.pos);
    }

    return expr;
  }

  private Expr binary(int precedence, TokenType... operators) throws Exception {
    Expr expr = this.parseExpr(precedence + 1);

    while (this.match(operators)) {
      Token operator = this.peek(-1);
      Expr right = this.parseExpr(precedence + 1);
      
      expr = new Expr.BinaryExpr(expr.pos, expr, operator, right);
    }

    return expr;
  }

  private Expr unary(int precedence, TokenType... operators) throws Exception {
    if (this.match(operators)) {
      Token operator = this.peek(-1);
      Expr right = this.unary(precedence, operators); // always will be unary, since the precedence is the same
      
      return new Expr.UnaryExpr(operator.pos(), operator, right);
    }

    return this.parseExpr(precedence + 1);
  }

  private Expr call(int precedence) throws Exception {
    Expr expr = this.parseExpr(precedence + 1);

    while (true) {
      if (this.match(TokenType.LParen)) {
        List<Expr> args = new ArrayList<>();
        
        if (!this.check(TokenType.RParen)) {
          do {
            args.add(this.expr());
          }
          while (this.match(TokenType.Comma));
        }

        this.consume(TokenType.RParen, "Expected ')' to finish function call");
        expr = new Expr.CallExpr(expr.pos, expr, args);
      }
      else
        break;
    }

    return expr;
  }

  private Expr primary() throws Exception {
    if (this.match(TokenType.FalseKw)) return new Expr.LiteralExpr(this.peek(-1).pos(), false);
    if (this.match(TokenType.TrueKw)) return new Expr.LiteralExpr(this.peek(-1).pos(), true);
    if (this.match(TokenType.NilKw)) return new Expr.LiteralExpr(this.peek(-1).pos(), null);

    if (this.match(TokenType.Number, TokenType.String)) return new Expr.LiteralExpr(this.peek(-1).pos(), this.peek(-1).literal());
    if (this.match(TokenType.Identifier))
      return new Expr.VariableExpr(this.peek(-1).pos(), this.peek(-1));

    if (this.match(TokenType.LParen)) {
      Position pos = this.peek(-1).pos();
      Expr expr = this.expr();
      
      this.consume(TokenType.RParen, "Expected ')' after expression");
      return new Expr.GroupingExpr(pos, expr);
    }

    Util.printError("Invalid expression: '" + this.peek(0).lexeme() + "'", this.peek(0).pos());
    return null;
  }

  // ---

  private boolean isAtEnd(int offset) {
    return this.current + offset >= this.tokens.size();
  }

  private Token peek(int offset) {
    if (this.isAtEnd(offset)) return null;
    
    return this.tokens.get(this.current + offset);
  }

  private Token advance() {
    Token t = this.peek(0);
    this.current++;

    return t;
  }

  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (this.check(type)) {
        this.advance();
        return true;
      }
    }

    return false;
  }

  private boolean check(TokenType t) {
    if (this.isAtEnd(0)) return false;
    return this.peek(0).type() == t;
  }

  private Token consume(TokenType t, String message) throws Exception {
    if (this.check(t)) return this.advance();

    Util.printError(message, this.peek(0).pos());
    return null;
  }

  private Token consumeNewLine() throws Exception {
    if (this.isAtEnd(0)) return null;
    if (this.check(TokenType.RBrace)) return this.peek(0);
    return this.consume(TokenType.NewLine, "Expected new line after statement, got '" + this.peek(0).lexeme() + "'");
  }

  private void skipNewLines() {
    while (!this.isAtEnd(0) && this.check(TokenType.NewLine))
      this.advance();
  }

  // ---

  private void synchronize() {
    this.advance();

    while (!this.isAtEnd(0)) {
      if (this.peek(-1).type() == TokenType.NewLine) return;

      switch (this.peek(0).type()) {
        case BreakKw,
             ContinueKw,
             IfKw,
             LetKw,
             LoopKw,
             ReturnKw,
             WhileKw:
          return;
        
        default:
          this.advance();
          break;
      }
    }
  }
}
