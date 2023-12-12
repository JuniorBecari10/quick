public class Ref {
  public final Token name;
  public final Environment env;

  public Ref(Token name, Environment env) {
    this.name = name;
    this.env = env;
  }

  public Object getReferenced() throws Exception {
    return this.env.get(this.name);
  }

  @Override
  public String toString() {
    return "&" + this.name;
  }
}
