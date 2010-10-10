var db = new Sqlite
db.open(":memory:")
db.execute("create table foo (i integer, f double, t text);")
db.execute("insert into foo (i, f, t) values (1, 2.0, 'foo');")
db.execute("insert into foo (i, f, t) values (3, NULL, 'bar');")
var res = db.query("select * from foo")
for (row <- res) {
    var first = true;
    for (elt <- row) {
        if (first)
            first = false
        else
            System.out.print(" ")
        val s = elt.typ() match {
            case Sqlite3C.INTEGER => elt.asInt.toString
            case Sqlite3C.FLOAT => elt.asDouble.toString
            case Sqlite3C.TEXT => elt.asText
            case Sqlite3C.NULL => "NULL"
            case _ => "???"
        }
        System.out.print(s)
    }
    System.out.println()
}
db.close()
