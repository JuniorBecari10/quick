# Quick

An interpreted scripting language that was made in a few days.

## About

This is a personal project just to practice the creation of programming and (still) shouldn't be used in production, due to the bugs that still exist.

The project was made in Java, following the proposed model inside the [Crafting Interpreters](https://craftinginterpreters.com/) book, with some modifications.

## How to Use

The language is very simple and straightforward as the book's one, so it's not very hard to learn.

### Hello World

```js
println("Hello, World!")
```
`print` can be used as well; this function doesn't insert a newline character (`\n`) at the end, like `println` does.

### Declaring Variables

The variable declaration is made with the `let` keyword:
```js
let x = 10
let y = "Hello!"
```

### Data Types

- `num` (supports both integers and floats)
- `str`
- `bool`
- ~~`array`~~ not added yet
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

### Conditions

Conditions can be expressed by the `if` and `else` keywords.

```rs
let x = 10

if x == 10 -> println("x is 10!")
else -> println("x is not 10")
```

#### Code Blocks

Quick has an unique and clean way to express a code block that contains only one statement, using the arrow (`->`) operator.

This operator is handy when you want to put only one statement inside a code block, but without the verbosity of the curly braces.

But remind that everywhere the syntax requires a code block, both ways can be used.

### Loops

Quick has two keywords for loops: `loop` and `while`. <br>
Both loops can be manipulated by the `break` and `continue` keywords.

#### `loop`
`loop` is used to create an infinite loop, and, in the future, will be used to iterate over arrays.

```rs
loop {
  println("infinite!")
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

### Including Files

Quick has the C-way to include files: just paste the other file's AST into the main one's.

The importing is made with the `incl` keyword, followed by the file's name, without the extension.

main.qk
```js
incl math

let res = add(2, 3)
println(res) // 5
```

math.qk
```rs
fn add(x, y) {
  return x + y
}
```
