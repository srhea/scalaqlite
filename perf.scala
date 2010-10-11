// Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
// All rights reserved.
//
// See the file LICENSE included in this distribution for details.

// Use an in-memory db, as we're only trying to test the performance of the
// Scala code, not the database itself.  Otherwise, the inserts are limited by
// disk write speed.
var db = new SqliteDb(":memory:")
db.execute("CREATE TABLE foo (i INTEGER, f DOUBLE, t TEXT);")
var start = java.lang.System.currentTimeMillis
val count = 100000
for (i <- 0 until count)
  db.execute("INSERT INTO foo (i, f, t) VALUES (1, 2.0, 'foo');")
var lat = java.lang.System.currentTimeMillis - start
println("inserts took " + lat + " ms: " + (count / (lat / 1000.0)) + " rows/s")
var sum = 0
start = java.lang.System.currentTimeMillis
for (row <- db.query("SELECT * FROM foo;")) {
    row(0) match {
        case SqlInt(i) => sum += i
        case _ => throw new Exception("not an int")
    }
}
if (count != sum) throw new Exception("count and sum don't match")
lat = java.lang.System.currentTimeMillis - start
println("query took " + lat + " ms: " + (count / (lat / 1000.0)) + " rows/s")
db.close()
