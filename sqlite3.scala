var db = Array(0L)
Sqlite3C.open(":memory:", db)
var stmt = Array(0L)
Sqlite3C.prepare_v2(db(0), "create table foo (i integer, f double, t text);", stmt)
Sqlite3C.step(stmt(0))
Sqlite3C.finalize(stmt(0))
Sqlite3C.prepare_v2(db(0), "insert into foo (i, f, t) values (1, 2.0, 'foo');", stmt)
Sqlite3C.step(stmt(0))
Sqlite3C.finalize(stmt(0))
Sqlite3C.prepare_v2(db(0), "insert into foo (i, f, t) values (3, NULL, 'bar');", stmt)
Sqlite3C.step(stmt(0))
Sqlite3C.finalize(stmt(0))
Sqlite3C.prepare_v2(db(0), "select * from foo;", stmt)
var done = false
while (!done) {
    val r = Sqlite3C.step(stmt(0))
    if (r == Sqlite3C.SQLITE_DONE) {
        done = true;
    }
    else {
        var i = 0;
        while (i < Sqlite3C.column_count(stmt(0))) {
            if (i != 0)
                System.out.print(" ")
            val s = Sqlite3C.column_type(stmt(0), i) match {
                case Sqlite3C.SQLITE_INTEGER => Sqlite3C.column_int(stmt(0), i).toString
                case Sqlite3C.SQLITE_FLOAT => Sqlite3C.column_double(stmt(0), i).toString
                case Sqlite3C.SQLITE_TEXT => Sqlite3C.column_text(stmt(0), i)
                case Sqlite3C.SQLITE_NULL => "NULL"
                case _ => "???"
            }
            System.out.print(s)
            i = i + 1
        }
        System.out.println()
    }
}
Sqlite3C.finalize(stmt(0))
Sqlite3C.close(db(0))
