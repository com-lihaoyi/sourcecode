SourceCode [![Build Status](https://travis-ci.org/lihaoyi/sourcecode.svg?branch=master)](https://travis-ci.org/lihaoyi/sourcecode) [![Join the chat at https://gitter.im/lihaoyi/Ammonite](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/lihaoyi/sourcecode?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
==========

```scala
"com.lihaoyi" %% "sourcecode" % "0.1.0" // Scala-JVM
"com.lihaoyi" %%% "sourcecode" % "0.1.0" // Scala.js
```

`sourcecode` is a small Scala library for that provides common "source code"
context to your program at runtime, similar to Python's `__name__`, C++'s
`__LINE__` or Ruby's `__FILE__`. For example, you can ask for the file-name
and line number of the current file:

```scala
val file = sourcecode.File()
assert(file.endsWith("/sourcecode/shared/src/test/scala/sourcecode/Tests.scala"))

val line = sourcecode.Line()
assert(line == 16)
```

This might not be something you want to use for "business logic", but is very
helpful for debugging, logging and providing automatic diagnostics. This
information is also available via an `implicit`, letting you write functions
that automatically pull it in.

The kinds of compilation-time data that `sourcecode` provides are:

- `sourcecode.File`: full path of the current file where the call occurs
- `sourcecode.Line`: current line number
- `sourcecode.Name`: the name of the nearest enclosing definition: `val`,
  `class`, whatever.
- `sourcecode.FullName`: the name of the nearest enclosing definition: `val`,
  `class`, whatever, prefixed by the names of all enclosing `class`s, `trait`s,
  `object`s or `package`s. Note that this does *not* include other enclosing
  `def`s, `val`s, `var`s or `lazy val`s`
- `sourcecode.Enclosing`: the name of the nearest enclosing definition: `val`,
  `class`, whatever, prefixed by the names of all enclosing `class`s, `trait`s,
  `object`s or `package`s, `def`s, `val`s, `var`s or `lazy val`s`
- `sourcecode.Text[T]`: when you want to take a value of type `T`, but also 
  want to get the "source text" of that particular value. Note that this 
  implicit requires the `-Yrangepos` compiler flag to work, and will fail to
  compile otherwise. Also, if you have multiple statements in a `{}` block, 
  `sourcecode.Text` will only capture the source code for the last expression 
  that gets returned.

All these are available both via `()` and as implicits, e.g. `sourcecode.File`
can be summoned via `sourcecode.File()` or `implicitly[sourcecode.File].value`.
This also means you can define functions that pull in this information
automatically:

```scala
def foo(arg: String)(implicit file: sourcecode.File) = {
  ... do something with arg ...
  ... do something with file.value ...
}

foo("hello") // the implicit sourcecode.File is filled in automatically
```

`sourcecode` does not rely on runtime reflection or stack inspection, and
is done at compile-time using macros. This means that it is both orders of
magnitude faster than e.g. getting file-name and line-numbers using stack
inspection, and also works on Scala.js where reflection and stack inspection
can't be used.

Use Cases
=========

At first it might seem strange to make use of these source-level details in
your program: shouldn't a program's meaning not change under re-formatting and
re-factoring?

It turns out that there are a number of entirely valid use cases for this sort
of information that is both extremely handy, and also would not be surprising
at all to a developer using your API. Here are a few example use cases:

Logging
-------

You can use `sourcecode.File` and `sourcecode.Line` to define `log` functions
that automatically capture their line number and file-name

```scala
def log(foo: String)(implicit line: sourcecode.Line, file: sourcecode.File) = {
  println(s"${file.value}:${line.value} $foo")
}

log("Foooooo") // sourcecode/shared/src/test/scala/sourcecode/Tests.scala:86 Fooooo
```

This can be handy for letting you see where the log lines are coming from,
without tediously tagging every log statement with a unique prefix.
Furthermore, this happens at compile time, and is thus orders of magnitude
faster than getting this information by generating stack traces, and works
on Scala.js where stack-inspection does not.

Enums
-----

You can use `sourcecode.Name` to define an enumeration-value factory function
that automatically assigns names to the enum values based on the name of the
`val` that it is assigned to

```scala
case class EnumValue(name: String){
  override def toString = name
}
class Enum{
  def value(implicit name: sourcecode.Name) = EnumValue(name.value)
}
object MyEnum extends Enum{
  val firstItem = value // No need to pass in "firstItem" as a string!
  val secondItem = value
}
assert(MyEnum.firstItem.toString == "firstItem")
assert(MyEnum.secondItem.toString == "secondItem")
```

This is very handy, and this functionality is used in a number of libraries
such as [FastParse](http://lihaoyi.github.io/fastparse/) and
[Scalatags](http://lihaoyi.github.io/scalatags/#CSSStylesheets) to provide
a boilerplate-free experience while still providing good debuggability
and convenience.

Sometimes you want to make sure that different enum values in differently
named enums (or even an enum of the same name in a different package!) are
given unique names. In that case, you can use `sourcecode.FullName` or
`sourcecode.Enclosing` to capture the full path e.g.
`"com.mypkg.MyEnum.firstItem"` and `"com.mypkg.MyEnum.secondItem"`. You can
also use `sourcecode.Name` in an constructor, in which case it'll be picked
up during inheritance:

```scala
class EnumValue(implicit name: sourcecode.Name){
  override def toString = name.value
}
object Foo extends EnumValue
println(Foo.toString)
assert(Foo.toString == "Foo")
```

Debug Prints
------------

How many times have you written tedious code like
```scala
object Bar{
  def foo(arg: String) = {
    println("Bar.foo: " + arg)
  }
}
```

Where you have to prefix every print statement with the name of the enclosing
classes, objects or functions to make sure you can find your print output
2-3 minutes later? With `source.Enclosing`, you can get this for free:

```scala
def debug[V](value: sourcecode.Text[V])(implicit enclosing: sourcecode.Enclosing) = {
  println(enclosing.value + " [" + value.source + "]: " + value.value)
}

class Foo(arg: Int){
  debug(arg) // sourcecode.DebugRun.main Foo [arg]: 123
  def bar(param: String) = {
    debug(arg -> param)
  }
}
new Foo(123).bar("lol")  // sourcecode.DebugRun.main Foo#bar [arg -> param]: (123,lol)
```

Embedding Domain-Specific Languages
-----------------------------------

The Scala programming is a popular choice to embed domain-specific languages:
that means that you start with some external language, e.g. this 
[MathProg] example

```scala
param m;
param n;
param l;

set I := 1 .. m;
set J := 1 .. n;
set K := 1 .. l;

param c{J};
param d{K};
param a{I, J};

var x{J} integer, >= 0;
var y{K} >= 0;
```

The linked slides has more detail about what exactly this language does (it
describes mathematical optimization problems). For a variety of reasons, you 
may prefer to write this as part of a Scala program instead: for example you
may want Scala's IDE support, or its ability to define functions that help
reduce boilerplate, or maybe you like the way the compiler provides type errors
when you do the wrong thing.

A first attempt at converting this to Scala may look like this:

```scala
val m = param("m")
val n = param("n")
val l = param("l")

val I = set("I") := 1 to m
val J = set("J") := 1 to m
val K = set("K") := 1 to m

val c = param("c", J)
val d = param("d", K)
val a = param("a", I, J)

val x = xvar("x", J).integer >= 0
val y = xvar("y", K) >= 0
```

There's a bunch of duplication around the names of the `val`s: each `val`
has its name repeated in a string that gets passed to the expression on the
right. This is for the program to use the name of the `val` later: for example
when printing error messages, or the results of the computation, you want to
see which `val`s are involved! Thus you end up duplicating the names over and
over and over.

With sourcecode, you can easily define `param` `set` and `xvar` as taking 
`sourcecode.Name`, thus eliminating all the boilerplate involved in duplicating
names:

```scala
val m = param
val n = param
val l = param

val I = set := 1 to m
val J = set := 1 to m
val K = set := 1 to m

val c = param(J)
val d = param(K)
val a = param(I, J)

val x = xvar(J).integer >= 0
val y = xvar(K) >= 0
```



Version History
---------------
0.1.1
=====

- Ignore `<local foo>` and `<init>` symbols when determining `sourcecode.Name`, 
`sourcecode.FullName` or `sourcecode.Enclosing`
- Add `sourcecode.Text` implicit to capture source code of an expression
- Add implicit conversions to `sourcecode.*`, so you can pass in a `String`
  to manually satisfy and implicit wanting a `sourcecode.Name` or 
  `sourcecode.FullName` or `sourcecode.File`, an `Int` to satisfy an implicit 
  asking for `sourcecode.Line` or a `Seq[Chunk]` to satisfy an implicit asking
  for a `sourcecode.Enclosing`

0.1.0
=====

- First release

[MathProg]: http://www.slideshare.net/gerferra/an-embedded-dsl-to-manipulate-mathprog-mixed-integer-programming-models-within-scala