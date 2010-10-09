class Sqlite3Db {
    private var db = Array(0L)
    def open(path: String): Unit = {
        Sqlite3C.open(":memory:", db)
    }
    def close(): Unit = {
        Sqlite3C.close(db(0))
        db(0) = 0
    }
    override def finalize(): Unit = {
        if (db(0) != 0)
            close()
    }
    class Result(dbc: Sqlite3Db) {
        var db: Sqlite3Db = dbc
        var stmt = Array(0L)
        var r = Sqlite3C.SQLITE_DONE
        def done(): Boolean = {
            r == Sqlite3C.SQLITE_DONE
        }
        def next(): Unit = {
            r = Sqlite3C.step(stmt(0))
            if (r != Sqlite3C.SQLITE_DONE && r != Sqlite3C.SQLITE_ROW)
                throw new Exception("unexpected result: " + r)
        }
        def close(): Unit = {
            Sqlite3C.finalize(stmt(0))
            stmt(0) = 0
        }
        override def finalize(): Unit = {
            if (stmt(0) != 0)
                close()
        }
        def columnCount(): Int = {
            Sqlite3C.column_count(stmt(0))
        }
        def columnType(i: Int): Int = {
            Sqlite3C.column_type(stmt(0), i)
        }
        def asInt(i: Int): Int = {
            Sqlite3C.column_int(stmt(0), i)
        }
        def asDouble(i: Int): Double = {
            Sqlite3C.column_double(stmt(0), i)
        }
        def asText(i: Int): String = {
            Sqlite3C.column_text(stmt(0), i)
        }
    }
    def query(sql: String): Result = {
        var res = new Result(this)
        val rc = Sqlite3C.prepare_v2(db(0), sql, res.stmt)
        res.next()
        res
    }
    def execute(sql: String): Unit = {
        var stmt = query(sql)
        while (!stmt.done()) {
            stmt.next()
        }
        stmt.close()
    }
}

var db = new Sqlite3Db
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
            case Sqlite3C.SQLITE_INTEGER => res.asInt(i).toString
            case Sqlite3C.SQLITE_FLOAT => res.asDouble(i).toString
            case Sqlite3C.SQLITE_TEXT => res.asText(i)
            case Sqlite3C.SQLITE_NULL => "NULL"
            case _ => "???"
        }
        System.out.print(s)
        i = i + 1
    }
    System.out.println()
    res.next()
}
res.close()
db.close()

