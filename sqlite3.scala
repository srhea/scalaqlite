class Sqlite {
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
    class Result(dbc: Sqlite, stmtc: Long) {
        private val db: Sqlite = dbc
        private var stmt: Long = stmtc
        private var r = 0
        def done(): Boolean = {
            stmt == 0 || r == Sqlite3C.DONE
        }
        private def assertNotDone(): Unit = {
            if (done())
                throw new Exception("already done")
        }
        def next(): Unit = {
            assertNotDone()
            r = Sqlite3C.step(stmt)
            if (r != Sqlite3C.DONE && r != Sqlite3C.ROW)
                throw new Exception("unexpected result: " + r)
        }
        def close(): Unit = {
            if (stmt == 0)
                throw new Exception("already closed")
            Sqlite3C.finalize(stmt)
            stmt = 0
        }
        override def finalize(): Unit = {
            if (stmt != 0)
                close()
        }
        def columnCount(): Int = {
            assertNotDone()
            Sqlite3C.column_count(stmt)
        }
        def columnType(i: Int): Int = {
            assertNotDone()
            Sqlite3C.column_type(stmt, i)
        }
        def asInt(i: Int): Int = {
            assertNotDone()
            Sqlite3C.column_int(stmt, i)
        }
        def asDouble(i: Int): Double = {
            assertNotDone()
            Sqlite3C.column_double(stmt, i)
        }
        def asText(i: Int): String = {
            assertNotDone()
            Sqlite3C.column_text(stmt, i)
        }
    }
    def query(sql: String): Result = {
        var stmt = Array(0L)
        val rc = Sqlite3C.prepare_v2(db(0), sql, stmt)
        var res = new Result(this, stmt(0))
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
res.close()
db.close()

