import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Lexer {
  private final char[] input;
  
  private int start;
  private int current;

  private Position startPos;
  private Position currentPos;

  private List<Token> tokens;

  private static Map<String, TokenType> keywords = new HashMap<>();

  static {
    keywords.put("let", TokenType.LetKw);
    keywords.put("loop", TokenType.LoopKw);
    keywords.put("while", TokenType.WhileKw);
    keywords.put("in", TokenType.InKw);
    keywords.put("fn", TokenType.FnKw);
    keywords.put("if", TokenType.IfKw);
    keywords.put("else", TokenType.ElseKw);
    keywords.put("return", TokenType.ReturnKw);
    keywords.put("break", TokenType.BreakKw);
    keywords.put("continue", TokenType.ContinueKw);

    keywords.put("true", TokenType.TrueKw);
    keywords.put("false", TokenType.FalseKw);
    keywords.put("nil", TokenType.NilKw);
  }

  public Lexer(String input) {
    this.input = input.toCharArray();

    this.start = 0;
    this.current = 0;

    this.startPos = new Position(0, 0);
    this.currentPos = new Position(0, 0);

    this.tokens = new ArrayList<>();
  }

  public List<Token> lex() throws Exception {
    while (!this.isAtEnd()) {
      this.start = this.current;
      this.startPos = new Position(this.currentPos);

      try {
        this.token();
      } catch (Exception e) {
        continue;
      }
    }

    this.addToken(TokenType.NewLine);
    return this.tokens;
  }

  private void token() throws Exception {
    char c = this.advance();

    switch (c) {
      case '+':
        if (this.match('='))
          this.addToken(TokenType.PlusEqual);
        else if (this.match('+'))
          this.addToken(TokenType.DoublePlus);
        else
          this.addToken(TokenType.Plus);
        break;
      
      case '-':
        if (this.match('>'))
          this.addToken(TokenType.Arrow);
        else if (this.match('='))
          this.addToken(TokenType.MinusEqual);
        else if (this.match('-'))
          this.addToken(TokenType.DoubleMinus);
        else
          this.addToken(TokenType.Minus);
        break;
      
      case '*':
        if (this.match('='))
          this.addToken(TokenType.StarEqual);
        else
          this.addToken(TokenType.Star);
        break;

      case '/':
        if (this.match('/'))
          while (!this.isAtEnd() && this.peek(0) != '\n')
            this.advance();
        else if (this.match('='))
          this.addToken(TokenType.SlashEqual);
        else
          this.addToken(TokenType.Slash);
        break;

      case '%':
        if (this.match('='))
          this.addToken(TokenType.ModuloEqual);
        else
          this.addToken(TokenType.Modulo);
        break;

      case '(':
        this.addToken(TokenType.LParen);
        break;

      case ')':
        this.addToken(TokenType.RParen);
        break;

      case '[':
        this.addToken(TokenType.LBracket);
        break;

      case ']':
        this.addToken(TokenType.RBracket);
        break;

      case '{':
        this.addToken(TokenType.LBrace);
        break;

      case '}':
        this.addToken(TokenType.RBrace);
        break;

      case ':':
        this.addToken(TokenType.Colon);
        break;

      case ',':
        this.addToken(TokenType.Comma);
        break;

      case '.':
        if (this.match('.'))
          this.addToken(TokenType.DoubleDot);
        else
          this.addToken(TokenType.Dot);
        break;

      case '&':
        this.addToken(TokenType.Ampersand);
        break;

      case '|':
        this.addToken(TokenType.VerticalBar);
        break;

      case '=':
        if (this.match('='))
          this.addToken(TokenType.DoubleEqual);
        else
          this.addToken(TokenType.Equal);
        break;

      case '!':
        if (this.match('='))
          this.addToken(TokenType.BangEqual);
        else
          this.addToken(TokenType.Bang);
        break;
      
      case '<':
        if (this.match('='))
          this.addToken(TokenType.LessEqual);
        else if (this.match('<')) {
          if (this.match('='))
            this.addToken(TokenType.LShiftEqual);
          else
            this.addToken(TokenType.LShift);
        }
        else
          this.addToken(TokenType.Less);
        break;

      case '>':
        if (this.match('='))
          this.addToken(TokenType.GreaterEqual);
        else if (this.match('>')) {
          if (this.match('='))
            this.addToken(TokenType.RShiftEqual);
          else
            this.addToken(TokenType.RShift);
        }
        else
          this.addToken(TokenType.Greater);
        break;

      case '\n':
        this.addToken(TokenType.NewLine);

        this.currentPos.line++;
        this.currentPos.col = 0;
        break;

      case ' ', '\t', '\r':
        break;
      
      case '"':
        this.string();
        break;

      default:
        if (this.isIdentifier(c)) this.identifier();
        else if (this.isNumber(c)) this.number();

        else {
          this.retrocede();
          
          try {
            Util.printError("Unknown token: '" + c + "'", this.currentPos);
          }
          finally {
            this.advance();
          }
        }
        break;
    }
  }

  // ---

  private void identifier() {
    while (this.isIdentifier(this.peek(0)) || this.isNumber(this.peek(0)))
      this.advance();
    
    TokenType type = TokenType.Identifier;
    TokenType value = keywords.get(this.lexeme());

    if (value != null)
      type = value;

    this.addToken(type);
  }

  private void number() throws Exception {
    while (this.isNumber(this.peek(0)))
      this.advance();
    
    if (this.peek(0) == '.' && isNumber(this.peek(1))) {
      this.advance();

      while (this.isNumber(this.peek(0)))
        this.advance();
    }

    Optional<Double> opt = Util.supressException(() -> Double.parseDouble(this.lexeme()));
    
    if (!opt.isPresent())
      Util.printError("Invalid number literal: '" + this.lexeme() + "'", this.startPos);

    double num = opt.get();
    this.addToken(TokenType.Number, this.lexeme(), num, this.startPos);
  }

  private void string() throws Exception {
    while (!this.isAtEnd() && this.peek(0) != '"') {
      if (this.peek(0) == '\n') {
        this.currentPos.line++;
        this.currentPos.col = 0;
      }

      this.advance();
    }

    if (this.isAtEnd() && this.peek(-1) != '"') {
      Util.printError("Unterminated string literal: '" + new String(Arrays.copyOfRange(this.input, this.start + 1, this.current - 1)) + "'s", this.startPos);
    }

    this.advance();

    String str = new String(Arrays.copyOfRange(this.input, this.start + 1, this.current - 1));
    this.addToken(TokenType.String, str, str, this.startPos);
  }

  // ---

  private String lexeme() {
    return new String(Arrays.copyOfRange(this.input, this.start, this.current));
  }

  // ---

  private boolean isAtEnd() {
    return this.current >= this.input.length;
  }

  private char peek(int offset) {
    int index = this.current + offset;

    if (index < 0 || index >= this.input.length)
      return 0;

    return this.input[index];
  }

  private char advance() {
    this.current++;
    this.currentPos.col++;

    return this.peek(-1);
  }

  private void retrocede() {
    this.current--;
    this.currentPos.col--;
  }

  private boolean match(char c) {
    if (this.isAtEnd()) return false;

    if (this.peek(0) == c) {
      this.advance();
      return true;
    }

    return false;
  }

  // ---

  private boolean isIdentifier(char c) {
    return Character.isAlphabetic(c) || c == '_';
  }

  private boolean isNumber(char c) {
    return Character.isDigit(c);
  }

  // ---

  private void addToken(TokenType type) {
    String lexeme = this.lexeme();
    this.tokens.add(new Token(type, lexeme, lexeme, this.startPos));
  }

  private void addToken(TokenType type, String lexeme, Object literal, Position pos) {
    this.tokens.add(new Token(type, lexeme, literal, pos));
  }
}
