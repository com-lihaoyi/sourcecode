`sourcecode`
============

```scala
"com.lihaoyi" %% "sourcecode" % "0.1.0" // Scala-JVM
"com.lihaoyi" %%% "sourcecode" % "0.1.0" // Scala.js
```

`sourcecode` is a small Scala library for that provides common "source code"
context to your program at runtime. For example, you can ask for the file-name
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

All these are available both via `()` and as implicits, e.g. `sourcecode.File`
can be summoned via `sourcecode.File()` or `implicitly[sourcecode.File].value`.
This also means you can define functions that pull in this information
automatically:

```scala
def foo(arg: String)(implicit file: sourcecode.File) = {
  ... do something with arg ...
  ... do somethign with file.value ...
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

This is very handy, and this sort of behavior is used in a number of libraries
such as [FastParse](http://lihaoyi.github.io/fastparse/) and
[Scalatags](http://lihaoyi.github.io/scalatags/#CSSStylesheets) to provide
a boilerplate-free experience while still providing good debuggability
and convenience.

Debug Prints
------------

How many times have you written tedious code like
```scala
object Bar{
  def foo(arg: String) = {
    println("Foo.bar: " + arg)
  }
}
```

Where you have to prefix every print statement with the name of the enclosing
classes, objects or functions to make sure you can find your print output
2-3 minutes later? With `source.Enclosing`, you can get this for free:

```scala
def debug(msg: String)(implicit enclosing: sourcecode.Enclosing) = {
  println(enclosing.value + ": " + msg)
}

debug("Hello!") // sourcecode.Tests.debugRun: Hello!
class Foo(arg: Int){
  def bar(param: String) = {
    debug(arg + " " + param)
  }
}
new Foo(123).bar("lol")  // sourcecode.Tests.debugRun Foo#bar: 123 lol
```


