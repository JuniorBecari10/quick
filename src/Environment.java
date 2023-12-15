import java.util.HashMap;
import java.util.Map;

public class Environment {
  private final Map<String, Object> values = new HashMap<>();
  private Environment enclosing;

  public Environment() {
    this.enclosing = null;
  }

  public Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  public void define(String name, Object value) {
    this.values.put(name, value);
  }

  public boolean containsVariable(String name) {
    return this.values.containsKey(name);
  }

  public Object get(Token name) throws Exception {
    if (this.values.containsKey(name.lexeme())) {
      return this.values.get(name.lexeme());
    }

    if (this.enclosing != null)
      return this.enclosing.get(name);

    Util.printError("Variable '" + name.lexeme() + "' doesn't exist", name.pos());
    return null;
  }

  public void assign(Token name, Object value) throws Exception {
    if (this.values.containsKey(name.lexeme())) {
      values.put(name.lexeme(), value);
      return;
    }

    if (this.enclosing != null) {
      this.enclosing.assign(name, value);
      return;
    }

    Util.printError("Variable '" + name.lexeme() + "' doesn't exist", name.pos());
  }

  public void assignArray(Token name, int index, Object value) throws Exception {
    if (this.values.containsKey(name.lexeme())) {
      Object obj = this.get(name);

      if (!(obj instanceof Array))
        Util.printError("Can only index arrays", name.pos());
      
      Array a = (Array) obj;
      a.array.set(index, value);

      values.put(name.lexeme(), a);
      return;
    }

    if (this.enclosing != null) {
      this.enclosing.assign(name, value);
      return;
    }

    Util.printError("Variable '" + name.lexeme() + "' doesn't exist", name.pos());
  }
}
