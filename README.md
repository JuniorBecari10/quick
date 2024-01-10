# Quick

An interpreted, dynamic-typed scripting language that was made in a few days.

## About

This is a personal project made just to practice the creation of a programming language and (still) shouldn't be used in production, due to the bugs that still exist.

The project was made in Java, following the proposed model inside the [Crafting Interpreters](https://craftinginterpreters.com/) book, with some modifications.

## REPL

Quick has a Read-Evaluate-Print-Loop feature, which can be activated by not providing any arguments to the executable.

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
'exit'  - Exits the REPL
'cls'   - Clears the screen
'reset' - Resets the current environment and clears all declared variables
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

Remind that everywhere the syntax requires a code block, both ways can be used.

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

Range is a built-in type in Quick that allows you to iterate over a sequence of numbers. <br>
This is the syntax:
```js
start..end:step
```
The `:step` part can be omitted and if so, the default value is `1`.

### Functions

A function in Quick can be defined by both a statement (that automatically binds it to a name), and by an expression.

```rs
fn add(x, y) {
  return x + y
}
```

```rs
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
