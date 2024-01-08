public enum TokenType {
  Number,
  Identifier,
  String,

  NewLine,
  EOF,

  Plus,
  Minus,
  Star,
  Slash,

  PlusEqual,
  MinusEqual,
  StarEqual,
  SlashEqual,

  Modulo,

  LParen,
  RParen,

  LShift,
  RShift,

  LBracket,
  RBracket,

  LBrace,
  RBrace,

  Arrow,

  Colon,
  Comma,
  
  Dot,
  DoubleDot,

  Equal,
  DoubleEqual,

  Bang,
  BangEqual,

  Ampersand,
  VerticalBar,

  Less,
  LessEqual,

  Greater,
  GreaterEqual,

  LetKw,
  FnKw,
  LoopKw,
  WhileKw,
  InKw,
  IfKw,
  ElseKw,
  ReturnKw,
  BreakKw,
  ContinueKw,

  TrueKw,
  FalseKw,
  NilKw
}
