// Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
// All rights reserved.
//
// See the file LICENSE included in this distribution for details.

// This file is the "meat" of scalaqlite.  I'm sure I should be doing something
// more sophisticated about packages and file names, but I haven't quite sorted
// out how they work in Scala yet.  Hopefully they're nicer than in Java.

abstract class SqlValue
case class SqlNull() extends SqlValue { override def toString = "NULL" }
case class SqlInt(i: Int) extends SqlValue { override def toString = i.toString }
case class SqlDouble(d: Double) extends SqlValue { override def toString = d.toString }
case class SqlText(s: String) extends SqlValue { override def toString = s }

class SqliteResultSet(db: SqliteDb, private var stmt: Long) {
    def done = stmt == 0
    override def finalize() { if (!done) Sqlite3C.finalize(stmt) }
    def next() {
        assert(!done, "already done")
        val r = Sqlite3C.step(stmt)
        if (r == Sqlite3C.DONE) {
            Sqlite3C.finalize(stmt)
            stmt = 0
        }
        else if (r != Sqlite3C.ROW) {
            error("unexpected result: " + r)
        }
    }
    def row: IndexedSeq[SqlValue] = {
        assert(!done, "already done")
        for (i <- 0 until Sqlite3C.column_count(stmt))
            yield Sqlite3C.column_type(stmt, i) match {
                case Sqlite3C.INTEGER => SqlInt(Sqlite3C.column_int(stmt, i))
                case Sqlite3C.FLOAT => SqlDouble(Sqlite3C.column_double(stmt, i))
                case Sqlite3C.TEXT => SqlText(Sqlite3C.column_text(stmt, i))
                case Sqlite3C.NULL => SqlNull()
                case _ => error("unsupported type")
            }
    }
    def foreach(f: IndexedSeq[SqlValue] => Unit) { while (!done) { f(row); next() } }
}

class SqliteDb(path: String) {
    private val db = Array(0L)
    Sqlite3C.open(path, db) ensuring (_ == Sqlite3C.OK, Sqlite3C.errmsg(db(0)))
    def close() {
        assert(db(0) != 0, "already closed")
        Sqlite3C.close(db(0)) ensuring (_ == Sqlite3C.OK, Sqlite3C.errmsg(db(0)))
        db(0) = 0
    }
    override def finalize() { if (db(0) != 0) Sqlite3C.close(db(0)) }
    def query(sql: String): SqliteResultSet = {
        assert(db(0) != 0, "db is closed")
        val stmt = Array(0L)
        Sqlite3C.prepare_v2(db(0), sql, stmt) ensuring (_ == Sqlite3C.OK, Sqlite3C.errmsg(db(0)))
        val res = new SqliteResultSet(this, stmt(0))
        res.next()
        res
    }
    def execute(sql: String) { for (row <- query(sql)) () }
    def enableLoadExtension(on: Boolean) {
        Sqlite3C.enable_load_extension(db(0), if (on) 1 else 0)
    }
}
