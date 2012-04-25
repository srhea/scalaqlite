// Copyright (c) 2010-2012 Sean C. Rhea <sean.c.rhea@gmail.com>
// All rights reserved.
//
// See the file LICENSE included in this distribution for details.

package org.srhea.scalaqlite

import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference
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

class SqliteResultIterator(db: SqliteDb, private var stmt: Pointer)
    extends Iterator[IndexedSeq[SqlValue]]
{
    private var cachedRow: IndexedSeq[SqlValue] = null

    assert(stmt != null)
    advance()

    private def advance() {
        cachedRow = Sqlite3C.sqlite3_step(stmt) match {
            case Sqlite3C.ROW =>
                (0 until Sqlite3C.sqlite3_column_count(stmt)).map { i =>
                    Sqlite3C.sqlite3_column_type(stmt, i) match {
                        case Sqlite3C.INTEGER => SqlInt(Sqlite3C.sqlite3_column_int(stmt, i))
                        case Sqlite3C.FLOAT => SqlDouble(Sqlite3C.sqlite3_column_double(stmt, i))
                        case Sqlite3C.TEXT => SqlText(Sqlite3C.sqlite3_column_text(stmt, i))
                        case Sqlite3C.NULL => SqlNull()
                        case _ => sys.error("unsupported type")
                    }
                }
            case Sqlite3C.DONE =>
                Sqlite3C.sqlite3_finalize(stmt)
                null
            case Sqlite3C.ERROR =>
                sys.error("sqlite error: " + db.errmsg)
            case other =>
                sys.error("unexpected result: " + other)
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
            Sqlite3C.sqlite3_finalize(stmt)
    }
}

class SqliteDb(path: String) {
    private val db = new PointerByReference
    Sqlite3C.sqlite3_open(path, db) ensuring (_ == Sqlite3C.OK, errmsg)
    def close() {
        assert(db.getValue != null, "already closed")
        Sqlite3C.sqlite3_close(db.getValue) ensuring (_ == Sqlite3C.OK, errmsg)
        db.setValue(null)
    }
    override def finalize() { if (db.getValue != null) Sqlite3C.sqlite3_close(db.getValue) }
    def query(sql: String): Iterator[IndexedSeq[SqlValue]] = {
        assert(db.getValue != null, "db is closed")
        val stmt = new PointerByReference
        val tail = new PointerByReference
        Sqlite3C.sqlite3_prepare_v2(db.getValue, sql, sql.length, stmt, tail) ensuring (_ == Sqlite3C.OK, errmsg)
        new SqliteResultIterator(this, stmt.getValue)
    }
    def execute(sql: String) { for (row <- query(sql)) () }
    def enableLoadExtension(on: Boolean) {
        Sqlite3C.sqlite3_enable_load_extension(db.getValue, if (on) 1 else 0)
    }
    def errmsg: String = if (db.getValue == null) "db not open" else Sqlite3C.sqlite3_errmsg(db.getValue)
}
