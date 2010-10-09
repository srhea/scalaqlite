var db = new Sqlite
db.open(":memory:")
db.execute("create table foo (i integer, f double, t text);")
db.execute("insert into foo (i, f, t) values (1, 2.0, 'foo');")
db.execute("insert into foo (i, f, t) values (3, NULL, 'bar');")
var res = db.query("select * from foo")
while (!res.done()) {
    var i = 0;
    while (i < res.columnCount()) {
        if (i != 0)
            System.out.print(" ")
        val s = res.columnType(i) match {
            case Sqlite3C.INTEGER => res.asInt(i).toString
            case Sqlite3C.FLOAT => res.asDouble(i).toString
            case Sqlite3C.TEXT => res.asText(i)
            case Sqlite3C.NULL => "NULL"
            case _ => "???"
        }
        System.out.print(s)
        i = i + 1
    }
    System.out.println()
    res.next()
}
db.close()
