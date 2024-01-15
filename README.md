# Quick

An interpreted, dynamic-typed scripting language that was made in a few days.

## About

This is a personal project made just to practice the creation of a programming language, with a simple tree-walk interpreter.

The project was made in Java, following the proposed model inside the [Crafting Interpreters](https://craftinginterpreters.com/) book, with some modifications.

You need at least Java 21 to run the interpreter.

## Compiling

If you want to compile the interpreter yourself, you need to install:

- Java 21 or greater
- Make _(optional)_
- Jar _(optional)_

_The interpreter doesn't have any external dependencies._

There's a _Makefile_ in the root directory. To compile, run the `make compile` (or `cd src && javac -d ../bin Main.java`) command in the command line.

You can create a Jar file as well. To do it, just run `make jar` (or `cd bin && jar cfm ../build/quick.jar ../MANIFEST.MF *`)

## CLI Arguments

There are some options to run the interpreter.
```
Usage: quick <file> [args] | (-v | --version)
```
If you run the interpreter without arguments, the REPL will be activated;

You can provide a Quick source file and the arguments for that program (the arguments are optional);

You can also check the intepreter's version, with the `-v` and `--version` flags.

## REPL

Quick has a _Read-Evaluate-Print-Loop_ feature, which can be activated by not providing any arguments to the executable.

This is how it looks:
```
Quick REPL - v1.1

Type 'exit' to exit
Type 'help' for help

>
```
The REPL can be used to write Quick code without creating any files.

It has some exclusive rules:

- You can redeclare variables;
- Any expression statement evaluated value is printed in the stdout (i.e. if you type `1 + 1`, the interpreter will evaluate the expression and then print `2`).

It has some commands too, here's the list of them:
```
'exit'        - Exits the REPL
'cls' / clear - Clears the screen
'reset'       - Resets the current environment, clearing all declared variables
```
You can also insert a `\` before a command to interpret it as Quick code (e.g. `\exit`).

## Syntax

The language is very simple and straightforward, so it's not very hard to learn.

Here's the basic syntax:

### Hello World

```js
println("Hello, World!")
```
`print` can be used as well, but this function doesn't insert a newline character (`\n`) at the end, like `println` does.

### Declaring Variables

The variable declaration is made with the `let` keyword:
```js
let x = 10
let y = "Hello!"
```

You can lazily evaluate them as well, by not specifying any value:
```js
let x
```
In this case, the value of the variable will be `nil`. <br>
So, these statements are semantically equivalent:
```js
let x
let x = nil
```

All variables in Quick are mutable.

### Data Types

- `num` (supports both integers and floats)
- `str`
- `bool`
- `array`
- `range`
- `ref` (pointers)
- `fn` (functions)
- `nil`

### Pointers

Quick has a feature that very few scripting languages have: pointers. <br>
Every argument is passed by value by default, and pointers allow them to be passed by reference.

```js
let x = 10
let ref = &x

*ref = 20

println(*ref) // 20
println(x)    // 20
```

```rs
fn increment(x) {
  *x = *x + 1
}

let x = 10
increment(&x)
println(x) // 11
```

### Conditions

Conditions can be expressed by the `if` and `else` keywords.

```rs
let x = 10

if x == 10 {
  println("x is 10!")
}

else {
  println("x is not 10")
}
```

#### Ternary Operator

Statements don't produce values, but expressions do. `if` is a keyword that represents both a statement and an expression. <br>
So you can do this:

```rs
let x = 10
let y = if x == 10: "hello" else: "bye"
```

You can also split the expression into two lines if you prefer:

```rs
let x = 10
let y = if x == 10: "hello"
        else: "bye"
```

#### Code Blocks

Quick uses the `{}` syntax to express code blocks:
```rs
if true {
  println("true!")
}
```

When a block is created, a new scope is also created, and all variables created inside that scope are deleted when it ends.

With this in mind, you can create standalone blocks, to manage the deletion of variables:
```rs
{
  let x = 10
  println(x) // 10
} // 'x' is deleted here

println(x) // Error | Variable 'x' doesn't exist in this or a parent scope
```

Quick has an unique and clean way to express a code block that contains only one statement, using the arrow (`->`) operator.

This operator is handy when you want to put only one statement inside a code block, but without the verbosity of the curly braces.

Example:
```rs
let x = 10

if x == 10 -> println("x is 10!")
else -> println("x is not 10")
```

The statement can be split into two lines:

```rs
let x = 10

if x == 10
  -> println("x is 10!")
else
  -> println("x is not 10")
```

This way is also valid:

```rs
let x = 10

if x == 10 ->
  println("x is 10!")
else ->
  println("x is not 10")
```

Remind that everywhere the syntax requires a code block, both ways can be used.

### Arrays

Arrays in Quick are very simple. <br>
They are declared using brackets (`[]`).

```rs
let array = [1, 2, 3, 4]
```

They can hold values of different types also.

```rs
let array = [1, true, "hello"]
```

Arrays can be indexed (more on Indexing) using numbers and ranges:
```rs
let array = [1, 2, 3, 4]
println(array[0]) // 1
println(array[0..2]) // [1, 2]
```

### Strings

Strings are pieces of text.

All string literals in Quick are raw strings (this means that `\n` isn't the newline character, it's literally the string `"\n"`).

They can be indexed by numbers and ranges too:

```rs
let string = "hello"
println(string[0]) // "h"
println(string[0..2]) // "he"
```

#### Indexing

In Quick, only strings and arrays support the indexing syntax. <br>
Both support indexing by numbers and ranges.

```rs
let array = [1, 2, 3, 4]
println(array[0]) // 1
println(array[0..2]) // [1, 2]
```

```rs
let string = "hello"
println(string[0]) // "h"
println(string[0..2]) // "he"
```

Ranges has the step feature, and you can use this while indexing too:

```rs
let array = [1, 2, 3, 4, 5, 6]
println(array[0..5:2]) // [1, 3, 5]
```

```rs
let string = "hello, brave new world!"
println(string[0..20:2]) // "hlo rv e o"
```

### Loops

Quick has two keywords for loops: `loop` and `while`. <br>
Both loops can be manipulated by the `break` and `continue` keywords.

#### `loop`
`loop` is used to create an infinite loop and to iterate over arrays and ranges.

```rs
loop {
  println("infinite!")
}
```

```rs
let array = [1, 2, 3, 4]

loop i in array {
  println(i)
}
```

```rs
loop i in 1..10 {
  println(i)
}
```

```rs
loop i in 0..20:2 {
  println(i)
}
```

#### `while`
`while` is used to keep repeating a block of code while a given condition is true.

```rs
let i = 0

while i < 10 {
  println(i)
  i = i + 1
}
```

#### Range

Range is a built-in type in Quick that allows you to iterate over a sequence of numbers, and to index arrays and strings.

This is the syntax:
```js
start..end:step
```
The `:step` part can be omitted and if so, the default value is `1`.

### Functions

A function in Quick can be defined by both a statement (that automatically binds it to a name), and by an expression. <br>
So, functions in Quick are first-class.

```rs
fn add(x, y) {
  return x + y
}

let add = fn (x, y) {
  return x + y
}
```

If your function has only a return statement, you can simplify the writing by using this syntax, which creates a return statement automatically:

```rs
fn add(x, y): x + y
```
```rs
let add = fn(x, y): x + y
```

The expression can also be written in another line:
```rs
fn add(x, y):
  x + y
```
```rs
let add = fn(x, y):
  x + y
```
