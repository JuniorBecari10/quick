import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Interpreter implements Stmt.StmtVisitor<Void>, Expr.ExprVisitor<Object> {
  public final Environment globals = new Environment();
  private Environment environment = new Environment(globals);

  private boolean isRepl = false;

  public Interpreter() {
    // -- Prelude --

    globals.define("timeMs", new Callable() {
      public int arity() { return 0; }

      public Object call(Interpreter interpreter, List<Object> args) {
        return (double) System.currentTimeMillis();
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("timeSec", new Callable() {
      public int arity() { return 0; }

      public Object call(Interpreter interpreter, List<Object> args) {
        return (double) System.currentTimeMillis() / 1000.0;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("println", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        System.out.println(Util.stringify(args.get(0)));
        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("print", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        System.out.print(Util.stringify(args.get(0)));
        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("printlnBlank", new Callable() {
      public int arity() { return 0; }

      public Object call(Interpreter interpreter, List<Object> args) {
        System.out.println();
        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("clearScreen", new Callable() {
      public int arity() { return 0; }

      public Object call(Interpreter interpreter, List<Object> args) {
        Util.clearScreen();
        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("execute", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("win");
        ProcessBuilder builder;

        try {
          if (isWindows)
            builder = new ProcessBuilder("cmd", "/c", Util.stringify(args.get(0)));
          else
            builder = new ProcessBuilder("sh", "-c", Util.stringify(args.get(0)));

          builder.inheritIO();
          Process process = builder.start();
          
          return process.waitFor();
        } catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("input", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        System.out.print(Util.stringify(args.get(0)));
        return System.console().readLine();
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("inputNum", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        System.out.print(Util.stringify(args.get(0)));
        String s = System.console().readLine();

        try {
          Double res = Double.valueOf(s);
          return res;
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("inputNumPersistent", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        while (true) {
          try {
            System.out.print(Util.stringify(args.get(0)));

            String s = System.console().readLine();
            Double res = Double.valueOf(s);

            return res;
          }
          catch (Exception e) {
            continue;
          }
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("inputInt", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        System.out.print(Util.stringify(args.get(0)));
        String s = System.console().readLine();

        try {
          Integer res = Integer.valueOf(s);
          return (double) res;
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("inputIntPersistent", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        while (true) {
          try {
            System.out.print(Util.stringify(args.get(0)));

            String s = System.console().readLine();
            Integer res = Integer.valueOf(s);

            return (double) res;
          }
          catch (Exception e) {
            continue;
          }
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("panic", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        System.out.println("Panic: " + Util.stringify(args.get(0)));
        throw new Exception();
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("args", new Callable() {
      public int arity() { return 0; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        return new Array(Arrays.asList((Object[]) Util.args));
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("exit", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double d = (Double) args.get(0);

          if (d.intValue() != d)
            return null;
          
          System.exit(d.intValue());
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("len", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        final Object obj = args.get(0);
        
        if (obj instanceof String) {
          return (double) ((String) obj).length();
        }
        else if (obj instanceof Array) {
          return (double) ((Array) obj).array.size();
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("workingDir", new Callable() {
      public int arity() { return 0; }

      public Object call(Interpreter interpreter, List<Object> args) {
        return System.getProperty("user.dir");
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("getProperty", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        if (!(args.get(0) instanceof String))
          return null;

        return System.getProperty((String) args.get(0));
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("setProperty", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) {
        if (!(args.get(0) instanceof String && args.get(1) instanceof String))
          return null;

        return System.setProperty((String) args.get(0), (String) args.get(1));
      }

      public String toString() { return "<native fn>"; }
    });

    // -- Types --

    globals.define("asStr", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        return Util.stringify(args.get(0));
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("asNum", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        try {
          if (!(args.get(0) instanceof String)) return null;

          String s = (String) args.get(0);
          return Double.valueOf(s);
        } catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("toInt", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        if (args.get(0) instanceof String) {
          String s = (String) args.get(0);
          return (double) Double.valueOf(s).intValue();
        }

        if (args.get(0) instanceof Double) {
          Double d = (Double) args.get(0);
          return (double) d.intValue();
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("isInt", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        if (!(args.get(0) instanceof Double)) return false;

        Double d = (Double) args.get(0);
        return d.intValue() == d;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("isFloat", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        if (!(args.get(0) instanceof Double)) return false;

        Double d = (Double) args.get(0);
        return d.intValue() != d;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("isNum", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        if (args.get(0) instanceof Double) return true;
        return false;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("isStr", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        if (args.get(0) instanceof String) return true;
        return false;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("isBool", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        if (args.get(0) instanceof Boolean) return true;
        return false;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("isArray", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        if (args.get(0) instanceof Array) return true;
        return false;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("isRange", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        if (args.get(0) instanceof Range) return true;
        return false;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("isRef", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        if (args.get(0) instanceof Ref) return true;
        return false;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("isFn", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        if (args.get(0) instanceof Function) return true;
        return false;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("typeOf", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) {
        if (args.get(0) instanceof Double) return "num";
        if (args.get(0) instanceof String) return "str";
        if (args.get(0) instanceof Boolean) return "bool";
        if (args.get(0) instanceof Array) return "array";
        if (args.get(0) instanceof Range) return "range";
        if (args.get(0) instanceof Ref) return "ref";
        if (args.get(0) instanceof Function) return "fn";
        if (args.get(0) == null) return "nil";

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    // -- Math --

    globals.define("ceil", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double d = (Double) args.get(0);
          return Math.ceil(d);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("floor", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double d = (Double) args.get(0);
          return Math.floor(d);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("min", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double && args.get(1) instanceof Double) {
          Double a = (Double) args.get(0);
          Double b = (Double) args.get(1);

          return Math.min(a, b);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("max", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double && args.get(1) instanceof Double) {
          Double a = (Double) args.get(0);
          Double b = (Double) args.get(1);
          
          return Math.max(a, b);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("factorial", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double d = (Double) args.get(0);
          Double res = d;

          while (d > 1) {
            res = res * (d - 1);
            d--;
          }

          return res;
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("fibonacci", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double n = (Double) args.get(0);
          Double current = 1.0;
          Double prev = 0.0;

          for (int i = 2; i <= n; i++) {
            current += prev;
            prev = current - prev;
          }

          return current;
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("sqrt", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double d = (Double) args.get(0);

          if (d < 0)
            return null;

          return Math.sqrt(d);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("cbrt", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double d = (Double) args.get(0);

          if (d < 0)
            return null;

          return Math.cbrt(d);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("lcm", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double && args.get(1) instanceof Double) {
          Double a = (Double) args.get(0);
          Double b = (Double) args.get(1);

          if (a == 0 || b == 0)
            return 0;

          double absNumber1 = Math.abs(a);
          double absNumber2 = Math.abs(b);

          double absHigherNumber = Math.max(absNumber1, absNumber2);
          double absLowerNumber = Math.min(absNumber1, absNumber2);

          double lcm = absHigherNumber;
          
          while (lcm % absLowerNumber != 0) {
            lcm += absHigherNumber;
          }
            
          return lcm;
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("gcd", new Callable() {
      public int arity() { return 2; }

    public Object call(Interpreter interpreter, List<Object> args) throws Exception {
      if (args.get(0) instanceof Double && args.get(1) instanceof Double) {
        Double a = (Double) args.get(0);
        Double b = (Double) args.get(1);
        
        double i;
        if (a < b)
          i = a;
        else
          i = b;
        
        for (; i > 1; i--) {
          if (a % i == 0 && b % i == 0)
            return i;
        }
        
        return 1;
      }
      
      return null;
    }

      public String toString() { return "<native fn>"; }
    });

    globals.define("sin", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double d = (Double) args.get(0);

          return Math.sin(d);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("cos", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double d = (Double) args.get(0);

          return Math.cos(d);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("tan", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double d = (Double) args.get(0);

          return Math.tan(d);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("asin", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double d = (Double) args.get(0);

          return Math.asin(d);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("acos", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double d = (Double) args.get(0);

          return Math.acos(d);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("atan", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double d = (Double) args.get(0);

          return Math.atan(d);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("sinh", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double d = (Double) args.get(0);

          return Math.sinh(d);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("cosh", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double d = (Double) args.get(0);

          return Math.cosh(d);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("tanh", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double d = (Double) args.get(0);

          return Math.tanh(d);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("atan2", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double && args.get(1) instanceof Double) {
          Double a = (Double) args.get(0);
          Double b = (Double) args.get(1);

          return Math.atan2(a, b);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("power", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double && args.get(1) instanceof Double) {
          Double a = (Double) args.get(0);
          Double b = (Double) args.get(1);

          return Math.pow(a, b);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("log", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double a = (Double) args.get(0);

          return Math.log(a);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("log10", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double a = (Double) args.get(0);

          return Math.log10(a);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("log1p", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double a = (Double) args.get(0);

          return Math.log1p(a);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("toDegrees", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double a = (Double) args.get(0);

          return Math.toDegrees(a);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("toRadians", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double a = (Double) args.get(0);

          return Math.toRadians(a);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("pi", new Callable() {
      public int arity() { return 0; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        return Math.PI;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("e", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        return Math.E;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("abs", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Double) {
          Double a = (Double) args.get(0);

          return Math.abs(a);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("arrayStrNoBracket", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          Array a = (Array) args.get(0);
          String res = Util.stringify(a);

          return res.substring(1, res.length() - 1);
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("arrayStrNoBracketNoComma", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          Array a = (Array) args.get(0);
          String res = Util.stringify(a);

          return res.substring(1, res.length() - 1).replace(", ", "");
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("arrayStrNoComma", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          Array a = (Array) args.get(0);
          String res = Util.stringify(a);

          return res.replace(", ", "");
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    // -- Arrays --

    globals.define("sort", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Array) {
          List<Double> copy = new ArrayList<>();

          for (Object o : ((Array) args.get(0)).array) {
            if (o instanceof Double) {
              copy.add((Double) o);
            }
            else {
              return null;
            }
          }

          try {
            Collections.sort(copy);
            List<Object> out = new ArrayList<>();

            for (Double d : copy) {
              out.add(d);
            }

            return new Array(out);
          } catch (Exception e) {
            return null;
          }
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("append", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Array) {
          Array a = (Array) args.get(0);

          List<Object> list = new ArrayList<>(a.array);
          list.add(args.get(1));

          return new Array(list);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("push", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Array) {
          Array a = (Array) args.get(0);
          a.array.add(args.get(1));
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("remove", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Array && args.get(1) instanceof Double) {
          Array a = (Array) args.get(0);
          Double d = (Double) args.get(1);

          if (d.intValue() != d)
            return null;
          
          a.array.remove(d.intValue());
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("insert", new Callable() {
      public int arity() { return 3; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Array && args.get(2) instanceof Double) {
          Array a = (Array) args.get(0);
          Object element = args.get(1);
          Double d = (Double) args.get(2);

          if (d.intValue() != d)
            return null;
          
          a.array.add(d.intValue(), element);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("split", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof String && args.get(1) instanceof String) {
          String s = (String) args.get(0);
          String sep = (String) args.get(1);

          String[] spl = s.split(sep);
          List<Object> list = new ArrayList<>();

          for (String st : spl)
            list.add(st);


          return new Array(list);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

     globals.define("join", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        if (args.get(0) instanceof Array && args.get(1) instanceof String) {
          Array a = (Array) args.get(0);
          String sep = (String) args.get(1);

          List<String> strs = new ArrayList<>();

          for (Object o : a.array)
            strs.add(Util.stringify(o));

          return String.join(sep, strs);
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    // -- Strings --

    globals.define("collectStr", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          StringBuilder b = new StringBuilder();
          Array a = (Array) args.get(0);

          for (Object o : a.array) {
            b.append((String) o);
          }

          return b.toString();
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("startsWith", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          String s = (String) args.get(0);
          String start = (String) args.get(1);

          return s.startsWith(start);
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("endsWith", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          String s = (String) args.get(0);
          String end = (String) args.get(1);

          return s.endsWith(end);
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("contains", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          String s = (String) args.get(0);
          String substr = (String) args.get(1);

          return s.contains(substr);
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("indexOf", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          String s = (String) args.get(0);
          String substr = (String) args.get(1);

          return s.indexOf(substr);
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("lastIndexOf", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          String s = (String) args.get(0);
          String substr = (String) args.get(1);

          return s.lastIndexOf(substr);
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("replace", new Callable() {
      public int arity() { return 3; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          String s = (String) args.get(0);

          String replaced = (String) args.get(1);
          String replacer = (String) args.get(2);

          return s.replace(replaced, replacer);
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("repeat", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          String s = (String) args.get(0);
          Double times = (Double) args.get(1);

          if (times.intValue() != times)
            throw new Exception();

          return s.repeat(times.intValue());
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("toLowerCase", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          String s = (String) args.get(0);
          return s.toLowerCase();
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("toUpperCase", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          String s = (String) args.get(0);
          return s.toUpperCase();
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("trim", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          String s = (String) args.get(0);
          return s.trim();
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("trimStart", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          String s = (String) args.get(0);
          return s.stripLeading();
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("trimEnd", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          String s = (String) args.get(0);
          return s.stripTrailing();
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("isBlank", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          String s = (String) args.get(0);
          return s.isBlank();
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("isEmpty", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          String s = (String) args.get(0);
          return s.isEmpty();
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    // -- Files --

    globals.define("readFile", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          return Files.readString(new File((String) args.get(0)).toPath());
        }
        catch (Exception e) {
          return null;
        }
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("writeFile", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          try (BufferedWriter wr = new BufferedWriter(new FileWriter((String) args.get(0)));) {
            wr.write((String) args.get(1));
          }
        }
        catch (Exception e) {
          return null;
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("appendFile", new Callable() {
      public int arity() { return 2; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          try (BufferedWriter wr = new BufferedWriter(new FileWriter((String) args.get(0)));) {
            wr.append((String) args.get(1));
          }
        }
        catch (Exception e) {
          return null;
        }

        return null;
      }

      public String toString() { return "<native fn>"; }
    });

    globals.define("existsFile", new Callable() {
      public int arity() { return 1; }

      public Object call(Interpreter interpreter, List<Object> args) throws Exception {
        try {
          return new File((String) args.get(0)).exists();
        }
        catch (Exception e) {
          return false;
        }
      }

      public String toString() { return "<native fn>"; }
    });
  }

  public void interpret(List<Stmt> statements, boolean isRepl) throws Exception {
    this.isRepl = isRepl;

    for (Stmt stmt : statements) {
      try {
        this.execute(stmt);
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
  }

  // ---

  private void execute(Stmt stmt) throws Exception {
    stmt.accept(this);
  }

  public void executeBlock(List<Stmt> statements, Environment environment) throws Exception {
    Environment previous = this.environment;

    try {
      this.environment = new Environment(environment);

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

  private Object greater(Token operator, Object left, Object right) throws Exception {
    checkNumberOperands(operator, left, right);
    return (double) left > (double) right;
  }

  private Object greaterEqual(Token operator, Object left, Object right) throws Exception {
    checkNumberOperands(operator, left, right);
    return (double) left >= (double) right;
  }

  private Object less(Token operator, Object left, Object right) throws Exception {
    checkNumberOperands(operator, left, right);
    return (double) left < (double) right;
  }

  private Object lessEqual(Token operator, Object left, Object right) throws Exception {
    checkNumberOperands(operator, left, right);
    return (double) left <= (double) right;
  }

  private Object plus(Token operator, Object left, Object right) throws Exception {
    if (left instanceof Double && right instanceof Double)
      return (double) left + (double) right;
      
    return Util.stringify(left) + Util.stringify(right);
  }

  private Object minus(Token operator, Object left, Object right) throws Exception {
    checkNumberOperands(operator, left, right);
    return (double) left - (double) right;
  }

  private Object times(Token operator, Object left, Object right) throws Exception {
    checkNumberOperands(operator, left, right);
    return (double) left * (double) right;
  }

  private Object divide(Token operator, Object left, Object right, Expr leftExpr) throws Exception {
    checkNumberOperands(operator, left, right);

    if ((double) right == 0.0)
      Util.printError("Cannot divide by zero | values: left: " + Util.stringify(left) + ", right: " + Util.stringify(right), leftExpr.pos);

    return (double) left / (double) right;
  }

  private Object modulo(Token operator, Object left, Object right) throws Exception {
    checkNumberOperands(operator, left, right);
    return (double) left % (double) right;
  }

  private Object lShift(Token operator, Object left, Object right, Expr leftExpr) throws Exception {
    checkNumberOperands(operator, left, right);

    int leftInt = ((Double) left).intValue();
    int rightInt = ((Double) right).intValue();

    if ((double) leftInt != (double) left || (double) rightInt != (double) right) {
      Util.printError("Can only bit shift integers | values: left: " + Util.stringify(left) + ", right: " + Util.stringify(right), leftExpr.pos);
    }

    return (double) (leftInt << rightInt);
  }

  private Object rShift(Token operator, Object left, Object right, Expr leftExpr) throws Exception {
    checkNumberOperands(operator, left, right);

    int leftInt = ((Double) left).intValue();
    int rightInt = ((Double) right).intValue();

    if ((double) leftInt != (double) left || (double) rightInt != (double) right) {
      Util.printError("Can only bit shift integers | values: left: " + Util.stringify(left) + ", right: " + Util.stringify(right), leftExpr.pos);
    }

    return (double) (leftInt >> rightInt);
  }

  private Object and(Object left, Object right) throws Exception {
    return this.isTruthy(left) && this.isTruthy(right);
  }

  private Object or(Object left, Object right) throws Exception {
    return this.isTruthy(left) || this.isTruthy(right);
  }

  private Object notEqual(Object left, Object right) throws Exception {
    return !this.isEqual(left, right);
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);
  }

  private static void checkNumberOperand(Token operator, Object operand) throws Exception {
    if (operand instanceof Double) return;
    Util.printError("Operand must be a number | value: " + Util.stringify(operand), operator.pos());
  }

  private static void checkNumberOperands(Token operator, Object left, Object right) throws Exception {
    if (left instanceof Double && right instanceof Double) return;
    Util.printError("Operands must be numbers | values: left: " + Util.stringify(left) + ", right: " + Util.stringify(right), operator.pos());
  }

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
    if (this.isRepl) {
      System.out.println("< " + Util.stringify(this.evaluate(stmt.expr)));
      return null;
    }

    this.evaluate(stmt.expr);
    return null;
  }

  @Override
  public Void visitFnStmt(Stmt.FnStmt stmt) throws Exception {
    Function fn = new Function(stmt, this.environment);
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

    if (this.environment.containsVariable(stmt.name.lexeme()) && !this.isRepl)
      Util.printError("Cannot redeclare '" + stmt.name.lexeme() + "' in the same scope", stmt.name.pos());

    this.environment.define(stmt.name.lexeme(), value);
    return null;
  }

  @Override
  public Void visitLoopStmt(Stmt.LoopStmt stmt) throws Exception {
    if (stmt.variable == null || stmt.iterable == null) {
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

    Object iterable = this.evaluate(stmt.iterable);

    if (!(iterable instanceof Iterable))
      Util.printError("Can only iterate over iterable objects (e.g. arrays and ranges), got '" + Util.stringify(iterable) + "'", stmt.pos);

    Iterable it = (Iterable) iterable;

    Environment previous = this.environment;
    this.environment = new Environment(previous);

    while (it.hasNext()) {
      try {
        this.environment.define(stmt.variable.lexeme(), it.next());
        this.execute(stmt.block);
      }
      catch (Util.Break b) {
        break;
      }
      catch (Util.Continue c) {
        continue;
      }
    }

    this.environment = previous;
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
  public Object visitArrayExpr(Expr.ArrayExpr expr) throws Exception {
    List<Object> array = new ArrayList<>();

    for (Expr e : expr.items)
      array.add(this.evaluate(e));

    return new Array(array);
  }

  @Override
  public Object visitAssignExpr(Expr.AssignExpr expr) throws Exception {
    Object value = this.evaluate(expr.value);

    if (this.globals.containsVariable(expr.name.lexeme())) {
      Util.printError("Cannot reassign native function '" + expr.name.lexeme() + "'", expr.name.pos());
    }

    switch (expr.operator.type()) {
      case PlusEqual, DoublePlus -> value = this.plus(expr.operator, this.evaluate(expr.lValue), value);
      case MinusEqual, DoubleMinus -> value = this.minus(expr.operator, this.evaluate(expr.lValue), value);
      case StarEqual -> value = this.times(expr.operator, this.evaluate(expr.lValue), value);
      case SlashEqual -> value = this.divide(expr.operator, this.evaluate(expr.lValue), value, expr.lValue);
      case ModuloEqual -> value = this.modulo(expr.operator, this.evaluate(expr.lValue), value);

      case LShiftEqual -> value = this.lShift(expr.operator, this.evaluate(expr.lValue), value, expr.lValue);
      case RShiftEqual -> value = this.rShift(expr.operator, this.evaluate(expr.lValue), value, expr.lValue);

      default -> {}
    }

    if (expr.isRef) {
      Object v = this.environment.get(expr.name);

      if (!(v instanceof Ref))
        Util.printError("Can only dereference assign reference objects, got '" + Util.stringify(v) + "'", expr.name.pos());
      
      Ref r = (Ref) v;
      r.env.assign(r.name, value);
    }
    else
      this.environment.assign(expr.name, value);

    return value;
  }

  @Override
  public Object visitAssignIndexExpr(Expr.AssignIndexExpr expr) throws Exception {
    Object value = this.evaluate(expr.value);
    Object index = this.evaluate(expr.index);

    if (!(index instanceof Double))
      Util.printError("Arrays can only be indexed by integers, got '" + Util.stringify(index) + "'", expr.pos);
    
    Double ind = (Double) index;
    
    if (ind.intValue() != ind)
      Util.printError("Arrays can only be indexed by integers, got '" + Util.stringify(index) + "'", expr.pos);
    
    this.environment.assignArray(expr.name, ind.intValue(), value);
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
        return this.notEqual(left, right);

      // ---

      case Ampersand:
        return this.and(left, right);

      case VerticalBar:
        return this.or(left, right);

      // ---

      case Greater:
        return this.greater(expr.operator, left, right);

      case GreaterEqual:
        return this.greaterEqual(expr.operator, left, right);

      case Less:
        return this.less(expr.operator, left, right);

      case LessEqual:
        return this.lessEqual(expr.operator, left, right);

      // ---

      case Plus:
        return this.plus(expr.operator, left, right);

      case Minus:
        return this.minus(expr.operator, left, right);

      case Star:
        return this.times(expr.operator, left, right);

      case Slash:
        return this.divide(expr.operator, left, right, expr.left);

      case Modulo:
        return this.modulo(expr.operator, left, right);

      case LShift:
        return this.lShift(expr.operator, left, right, expr.left);

      case RShift:
        return this.rShift(expr.operator, left, right, expr.left);

      // ---

      case InKw:
        if (!(right instanceof Array))
          Util.printError("Right side of an 'in' expression must be an array, got '" + Util.stringify(right) + "'", expr.left.pos);
        
        Array a = (Array) right;

        for (Object o : a.array)
          if (left.equals(o))
            return true;

        return false;

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
      Util.printError("Can only call functions, got '" + Util.stringify(callee) + "'", expr.callee.pos);

    Callable function = (Callable) callee;

    if (args.size() != function.arity())
      Util.printError("Expected " + function.arity() + " arguments, got " + args.size(), expr.callee.pos);

    return function.call(this, args);
  }

  @Override
  public Object visitFnExpr(Expr.FnExpr expr) throws Exception {
    return new Function(expr, this.environment);
  }

  @Override
  public Object visitGroupingExpr(Expr.GroupingExpr expr) throws Exception {
    return this.evaluate(expr.expr);
  }

  @Override
  public Object visitIndexExpr(Expr.ArrayIndexExpr expr) throws Exception {
    Object array = this.evaluate(expr.array);
    Object index = this.evaluate(expr.index);

    if (!(array instanceof Array || array instanceof String))
      Util.printError("Can only index arrays and strings, got '" + Util.stringify(array) + "'", expr.pos);
    
    if (!(index instanceof Double || index instanceof Range))
      Util.printError("Arrays and strings can only be indexed by integers and ranges, got '" + Util.stringify(index) + "'", expr.pos);

    if (array instanceof Array) {
      Array a = (Array) array;

      if (index instanceof Double) {
        Double ind = (Double) index;

        if (ind.intValue() != ind)
          Util.printError("Arrays can only be indexed by integers and ranges, got '" + Util.stringify(index) + "'", expr.pos);

        if (ind < 0 || ind >= a.array.size())
          Util.printError("Index out of bounds: index " + ind.intValue() + " is outside the bounds for an array of length " + a.array.size(), expr.pos);
        
        return a.array.get(ind.intValue());
      }

      if (index instanceof Range) {
        Range range = (Range) index;

        if (((Double) (range.start)).intValue() != range.start || ((Double) (range.end)).intValue() != range.end || ((Double) (range.step)).intValue() != range.step)
          Util.printError("Range bounds and step must be integers, got '" + Util.stringify(index) + "'", expr.pos);

        if (range.start < 0 || range.start >= a.array.size() || range.end < 0 || range.end >= a.array.size())
          Util.printError("Index out of bounds: range " + Util.stringify(range) + " bound is outside the bounds for an array of length " + a.array.size(), expr.pos);

        List<Object> res = new ArrayList<>();
        while (range.hasNext()) {
          res.add(a.array.get(((Double) range.next()).intValue()));
        }

        return new Array(res);
      }
    }
    else if (array instanceof String) {
      String s = (String) array;

      if (index instanceof Double) {
        Double ind = (Double) index;

        if (ind.intValue() != ind)
          Util.printError("Arrays can only be indexed by integers and ranges, got '" + Util.stringify(index) + "'", expr.pos);

        if (ind < 0 || ind >= s.length())
          Util.printError("Index out of bounds: index " + ind.intValue() + " is outside the bounds for an array of length " + s.length(), expr.pos);
        
        return new String(new char[] { s.charAt(ind.intValue()) });
      }

      if (index instanceof Range) {
        Range range = (Range) index;

        if (((Double) (range.start)).intValue() != range.start || ((Double) (range.end)).intValue() != range.end || ((Double) (range.step)).intValue() != range.step)
          Util.printError("Range bounds and step must be integers, got '" + Util.stringify(index) + "'", expr.pos);

        if (range.start < 0 || range.start >= s.length() || range.end < 0 || range.end >= s.length())
          Util.printError("Index out of bounds: range " + Util.stringify(range) + " bound is outside the bounds for an array of length " + s.length(), expr.pos);

        List<Object> res = new ArrayList<>();
        while (range.hasNext()) {
          res.add(new String(new char[] { s.charAt(((Double) range.next()).intValue()) }));
        }

        StringBuilder b = new StringBuilder();

        for (Object o : res) {
          b.append((String) o);
        }

        return b.toString();
      }
    }

    return null;
   }

  @Override
  public Object visitLiteralExpr(Expr.LiteralExpr expr) throws Exception {
    return expr.value;
  }

  @Override
  public Object visitRangeExpr(Expr.RangeExpr expr) throws Exception {
    Object start = this.evaluate(expr.start);
    Object end = this.evaluate(expr.end);
    Object step = expr.step == null
                    ? 1.0
                    : this.evaluate(expr.step);

    if (!(start instanceof Double))
      Util.printError("The start of the range must be a number, got '" + Util.stringify(start) + "'", expr.pos);
    
    if (!(end instanceof Double))
      Util.printError("The end of the range must be a number, got '" + Util.stringify(end) + "'", expr.pos);
    
    if (!(step instanceof Double))
      Util.printError("The step of the range must be a number, got '" + Util.stringify(step) + "'", expr.pos);
    
    return new Range((Double) start, (Double) end, (Double) step);
  }

  @Override
  public Object visitTernaryExpr(Expr.TernaryExpr expr) throws Exception {
    if (this.isTruthy(this.evaluate(expr.condition)))
      return this.evaluate(expr.thenBranch);
    
    return this.evaluate(expr.elseBranch);
  }

  @Override
  public Object visitUnaryExpr(Expr.UnaryExpr expr) throws Exception {
    Object operand = this.evaluate(expr.operand);

    switch (expr.operator.type()) {
      case Bang:
        return !this.isTruthy(operand);
      
      case Minus:
        checkNumberOperand(expr.operator, operand);
        return -(double) operand;
      
      case Ampersand:
        return new Ref(((Expr.IdentifierExpr) expr.operand).name, this.environment);
      
      case Star:
        if (!(operand instanceof Ref))
          Util.printError("Can only dereference reference objects, got '" + Util.stringify(operand) + "'", expr.operator.pos());
        
        Ref r = (Ref) operand;
        return r.env.get(r.name);

      default:
        Util.printError("Invalid unary operator: '" + expr.operator.lexeme() + "'", expr.operator.pos());
        return null;
    }
  }

  @Override
  public Object visitVariableExpr(Expr.IdentifierExpr expr) throws Exception {
    return this.environment.get(expr.name);
  }
}
