// Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
// All rights reserved.
//
// See the file LICENSE included in this distribution for details.

// This file is the "meat" of scalaqlite.  I'm sure I should be doing something
// more sophisticated about packages and file names, but I haven't quite sorted
// out how they work in Scala yet.  Hopefully they're nicer than in Java.

package org.srhea.scalaqlite
import scala.collection.mutable.ListBuffer

class SqlException(msg: String) extends Exception(msg)

abstract class SqlValue {
  def toDouble: Double = throw new SqlException(toString + " is not a double")
  def toInt: Int = throw new SqlException(toString + " is not an int")
  def isNull = false
}
case class SqlNull() extends SqlValue {
  override def toString = "NULL"
  override def isNull = true
}
case class SqlInt(i: Int) extends SqlValue {
  override def toString = i.toString
  override def toDouble = i
  override def toInt = i
}
case class SqlDouble(d: Double) extends SqlValue {
  override def toString = d.toString
  override def toDouble = d
  override def toInt = if (d.round == d) d.toInt else super.toInt
}
case class SqlText(s: String) extends SqlValue { override def toString = s }

class SqliteResultIterator(db: SqliteDb, private var stmt: Long)
    extends Iterator[IndexedSeq[SqlValue]]
{
    private var cachedRow: IndexedSeq[SqlValue] = null

    assert(stmt != 0)
    advance()

    private def advance() {
        cachedRow = Sqlite3C.step(stmt) match {
            case Sqlite3C.ROW =>
                (0 until Sqlite3C.column_count(stmt)).map { i =>
                    Sqlite3C.column_type(stmt, i) match {
                        case Sqlite3C.INTEGER => SqlInt(Sqlite3C.column_int(stmt, i))
                        case Sqlite3C.FLOAT => SqlDouble(Sqlite3C.column_double(stmt, i))
                        case Sqlite3C.TEXT => SqlText(Sqlite3C.column_text(stmt, i))
                        case Sqlite3C.NULL => SqlNull()
                        case _ => error("unsupported type")
                    }
                }
            case Sqlite3C.DONE =>
                Sqlite3C.finalize(stmt)
                null
            case Sqlite3C.ERROR =>
                error("sqlite error: " + db.errmsg)
            case other =>
                error("unexpected result: " + other)
        }
    }

    def hasNext = cachedRow != null

    def next: IndexedSeq[SqlValue] = {
        assert(hasNext)
        val result = cachedRow
        advance()
        result
    }

    override def finalize() {
        if (hasNext)
            Sqlite3C.finalize(stmt)
    }
}

class SqliteDb(path: String) {
    private val db = Array(0L)
    Sqlite3C.open(path, db) ensuring (_ == Sqlite3C.OK, errmsg)
    def close() {
        assert(db(0) != 0, "already closed")
        Sqlite3C.close(db(0)) ensuring (_ == Sqlite3C.OK, errmsg)
        db(0) = 0
    }
    override def finalize() { if (db(0) != 0) Sqlite3C.close(db(0)) }
    def query(sql: String): Iterator[IndexedSeq[SqlValue]] = {
        assert(db(0) != 0, "db is closed")
        val stmt = Array(0L)
        Sqlite3C.prepare_v2(db(0), sql, stmt) ensuring (_ == Sqlite3C.OK, errmsg)
        new SqliteResultIterator(this, stmt(0))
    }
    def execute(sql: String) { for (row <- query(sql)) () }
    def enableLoadExtension(on: Boolean) {
        Sqlite3C.enable_load_extension(db(0), if (on) 1 else 0)
    }
    def errmsg: String = if (db(0) == 0) "db not open" else Sqlite3C.errmsg(db(0))
}
