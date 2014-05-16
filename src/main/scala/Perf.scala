// Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
// All rights reserved.
//
// See the file LICENSE included in this distribution for details.

// Use an in-memory db, as we're only trying to test the performance of the
// Scala code, not the database itself.  Otherwise, the inserts are limited by
// disk write speed.
package org.srhea.scalaqlite
object PerfTest extends App {
  val rowCount = 100000
  def test: Double = {
    val db = new SqliteDb(":memory:")

    db.execute("CREATE TABLE foo (i INTEGER, f DOUBLE, t TEXT);")
    val insertStart = java.lang.System.currentTimeMillis
    db.execute("BEGIN;")
    db.prepare("INSERT INTO foo (f, i, t) VALUES (?, ?, ?);") { stmt =>
        var i = 0
        while (i < rowCount) {
            stmt.execute(SqlDouble(1.0), SqlLong(1), SqlText("foo"))
            i += 1
        }
    }
    db.execute("COMMIT;")
    val insertElapsed = java.lang.System.currentTimeMillis - insertStart
    println("prepared inserts took " + insertElapsed + " ms: " + (rowCount / (insertElapsed / 1000.0)) + " rows/s")
    val queryStart = java.lang.System.currentTimeMillis
    var sum = 0L
    db.foreachRow("SELECT * FROM foo") { case Seq(SqlLong(i), _*) => sum += i }
    // SELECT * makes the query about 3x slower since we greedily get all the rows,
    // but that is what I want here.
    if (rowCount != sum) throw new Exception("row count and sum don't match")

    val queryElapsed = java.lang.System.currentTimeMillis - queryStart
    println("query took " + queryElapsed + " ms: " + (rowCount / (queryElapsed / 1000.0)) + " rows/s")
    db.close()
    queryElapsed + insertElapsed
  }

  def main = {
    val numRuns = 20
    test // warm up the jvm
    val averageMs = (0 until numRuns).foldLeft(0.0) { (s, i) => s + test } / numRuns
    println("Combined average insert / query throughput of " + ((2 * rowCount) / averageMs * 1000) + " / s")
  }
  main
}
