var path = "test.db"
var f = new java.io.File(path)
if (f.exists) f.delete
var db = new SqliteDb(path)
db.execute("CREATE TABLE foo (i INTEGER, f DOUBLE, t TEXT);")
var start = java.lang.System.currentTimeMillis
for (i <- 0 until 10000)
  db.execute("INSERT INTO foo (i, f, t) VALUES (1, 2.0, 'foo');")
db.close()
println("inserts took " + (java.lang.System.currentTimeMillis - start) + " ms")
db = new SqliteDb("test.db")
var count = 0
var sum = 0
start = java.lang.System.currentTimeMillis
for (row <- db.query("SELECT * FROM foo;")) {
    count = count + 1
    row(0) match {
        case SqlInt(i) => sum += i
        case _ => throw new Exception("not an int")
    }
}
val lat = java.lang.System.currentTimeMillis - start
println("query took " + lat + " ms")
db.execute("DROP TABLE foo;")
db.close
println("count=" + count + ", sum=" + sum)
println((count / (lat / 1000.0)) + " rows/s")
