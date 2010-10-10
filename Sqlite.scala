abstract class SqlValue
case class SqlNull() extends SqlValue { override def toString: String = "NULL" }
case class SqlInt(i: Int) extends SqlValue { override def toString: String = i.toString }
case class SqlDouble(d: Double) extends SqlValue { override def toString: String = d.toString }
case class SqlText(s: String) extends SqlValue { override def toString: String = s }

class Sqlite(path: String) {
    private var db = Array(0L)
    assertSuccess("open", Sqlite3C.open(path, db))
    private def assertSuccess(method: String, rc: Int): Unit =
        if (rc != Sqlite3C.OK)
            throw new Exception(method + " failed: " + Sqlite3C.errmsg(db(0)))
    private def assertOpen: Unit = if (db(0) == 0) throw new Exception("db is not open")
    def close(): Unit = {
        assertOpen
        assertSuccess("close", Sqlite3C.close(db(0)))
        db(0) = 0
    }
    override def finalize(): Unit = if (db(0) != 0) Sqlite3C.close(db(0))

    class Result(dbc: Sqlite, stmtc: Long) {
        private val db: Sqlite = dbc
        private var stmt: Long = stmtc
        def done: Boolean = stmt == 0
        override def finalize: Unit = if (!done) Sqlite3C.finalize(stmt)
        private def assertNotDone: Unit = if (done) throw new Exception("already done")
        def next: Unit = {
            assertNotDone
            var r = Sqlite3C.step(stmt)
            if (r == Sqlite3C.DONE) {
                Sqlite3C.finalize(stmt)
                stmt = 0
            }
            else if (r != Sqlite3C.ROW) {
                throw new Exception("unexpected result: " + r)
            }
        }
        def row: IndexedSeq[SqlValue] = {
            assertNotDone
            for (i <- 0 until Sqlite3C.column_count(stmt))
                yield Sqlite3C.column_type(stmt, i) match {
                    case Sqlite3C.INTEGER => SqlInt(Sqlite3C.column_int(stmt, i))
                    case Sqlite3C.FLOAT => SqlDouble(Sqlite3C.column_double(stmt, i))
                    case Sqlite3C.TEXT => SqlText(Sqlite3C.column_text(stmt, i))
                    case Sqlite3C.NULL => SqlNull()
                    case _ => throw new Exception("unsupported type")
                }
        }
        def foreach(f: IndexedSeq[SqlValue] => Unit): Unit = {
            while (!done) {
                f(row)
                next
            }
        }
    }
    def query(sql: String): Result = {
        assertOpen
        var stmt = Array(0L)
        val rc = Sqlite3C.prepare_v2(db(0), sql, stmt)
        assertSuccess("prepare", rc)
        var res = new Result(this, stmt(0))
        res.next
        res
    }
    def execute(sql: String): Unit = {
        var stmt = query(sql)
        while (!stmt.done)
            stmt.next
    }
}
