// Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
// All rights reserved.
//
// See the file LICENSE included in this distribution for details.

// This file is the "meat" of scalaqlite.  I'm sure I should be doing something
// more sophisticated about packages and file names, but I haven't quite sorted
// out how they work in Scala yet.  Hopefully they're nicer than in Java.

abstract class SqlValue
case class SqlNull() extends SqlValue { override def toString: String = "NULL" }
case class SqlInt(i: Int) extends SqlValue { override def toString: String = i.toString }
case class SqlDouble(d: Double) extends SqlValue { override def toString: String = d.toString }
case class SqlText(s: String) extends SqlValue { override def toString: String = s }

class SqliteResultSet(dbc: SqliteDb, stmtc: Long) {
    private val db: SqliteDb = dbc
    private var stmt: Long = stmtc
    def done: Boolean = stmt == 0
    override def finalize: Unit = if (!done) Sqlite3C.finalize(stmt)
    def next: Unit = {
        assert(!done)
        val r = Sqlite3C.step(stmt)
        if (r == Sqlite3C.DONE) {
            Sqlite3C.finalize(stmt)
            stmt = 0
        }
        else if (r != Sqlite3C.ROW) {
            throw new Exception("unexpected result: " + r)
        }
    }
    def row: IndexedSeq[SqlValue] = {
        assert(!done)
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

class SqliteDb(path: String) {
    private val db = Array(0L)
    Sqlite3C.open(path, db) ensuring (_ == Sqlite3C.OK)
    def close(): Unit = {
        assert(db(0) != 0)
        Sqlite3C.close(db(0)) ensuring (_ == Sqlite3C.OK)
        db(0) = 0
    }
    override def finalize(): Unit = if (db(0) != 0) Sqlite3C.close(db(0))
    def query(sql: String): SqliteResultSet = {
        assert(db(0) != 0)
        val stmt = Array(0L)
        Sqlite3C.prepare_v2(db(0), sql, stmt) ensuring (_ == Sqlite3C.OK)
        val res = new SqliteResultSet(this, stmt(0))
        res.next
        res
    }
    def execute(sql: String): Unit = for (row <- query(sql)) ()
    def enableLoadExtension(on: Boolean): Unit =
        Sqlite3C.enable_load_extension(db(0), if (on) 1 else 0)
}
