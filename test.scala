var db = new Sqlite
db.open(":memory:")
db.execute("create table foo (i integer, f double, t text);")
db.execute("insert into foo (i, f, t) values (1, 2.0, 'foo');")
db.execute("insert into foo (i, f, t) values (3, NULL, 'bar');")
for (row <- db.query("select * from foo;")) {
    var list = for (elt <- row) yield elt.toString
    println(list.reduceLeft(_ + " " + _))
}
db.close()
