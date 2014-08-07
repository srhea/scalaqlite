Scalaqlite - A native Scala library for Sqlite3
===============================================

# Installation

Execute `./build.sh` then copy

```
target/scala-2.10/scalaqlite-assembly-0.5-SNAPSHOT.jar
target/native/libscalaqlite.so
```

To your project 'lib' directory


# Description

This library is intended to give a very Scala-ish interface to Sqlite.  For
example, you can use the follwing code to print all the rows in table "foo"
with the columns separated by spaces:

    for (row <- db.query("SELECT * FROM foo;"))
        println(row.map(_.toString).mkString(" "))

Or if you want to add up the values of the first column, you can do this:

    for (row <- db.query("SELECT * FROM foo;")) {
        row(0) match {
            case SqlInt(i) => sum += i
            case _ => throw new Exception("expected an int")
        }
    }


Note that sbt doesn't handle setting java.library.path yet, so you have to run
the tests like this:

  LD_LIBRARY_PATH=./target/native sbt test

If you want to avoid compiling the native code and setting LD_LIBRARY_PATH, at
the cost of some performance, check out the jna branch.

Sean
