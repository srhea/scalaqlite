// Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
// All rights reserved.
//
// See the file LICENSE included in this distribution for details.

package org.srhea.scalaqlite
import scala.collection.mutable.ListBuffer

class SqlException(msg: String) extends Exception(msg)

abstract class SqlValue {
  def toDouble: Double = throw new SqlException(toString + " is not a double")
  def toInt: Int = throw new SqlException(toString + " is not an int")
  def toLong: Long = throw new SqlException(toString + " is not an long")
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
  override def toLong = i
}
case class SqlLong(i: Long) extends SqlValue {
  override def toString = i.toString
  override def toDouble = i
  override def toInt =
    if (i <= Integer.MAX_VALUE && i >= Integer.MIN_VALUE) i.toInt else super.toInt
  override def toLong = i
}
case class SqlDouble(d: Double) extends SqlValue {
  override def toString = d.toString
  override def toDouble = d
  override def toInt = if (d.round == d) d.toInt else super.toInt
  override def toLong = if (d.round == d) d.toLong else super.toLong
}
case class SqlBlob(bytes: Array[Byte]) extends SqlValue {
    override def toString = new String(bytes)
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
                        case Sqlite3C.INTEGER =>
                          val j = Sqlite3C.column_int64(stmt, i)
                          if (j <= Integer.MAX_VALUE && j >= Integer.MIN_VALUE)
                            SqlInt(j.toInt)
                          else
                            SqlLong(j)
                        case Sqlite3C.FLOAT => SqlDouble(Sqlite3C.column_double(stmt, i))
                        case Sqlite3C.TEXT => SqlBlob(Sqlite3C.column_blob(stmt, i))
                        case Sqlite3C.BLOB => SqlBlob(Sqlite3C.column_blob(stmt, i))
                        case Sqlite3C.NULL => SqlNull()
                        case _ => sys.error("unsupported type")
                    }
                }
            case Sqlite3C.DONE =>
                Sqlite3C.finalize(stmt)
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
