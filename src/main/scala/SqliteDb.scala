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
  def toBlob: Seq[Byte] = throw new SqlException(toString + " is not a blob")
  def isNull = false
  def bindValue(stmt: Long, col: Int): Int
}
case class SqlNull() extends SqlValue {
  override def toString = "NULL"
  override def isNull = true
  override def bindValue(stmt: Long, col: Int) = Sqlite3C.bind_null(stmt, col)
}
case class SqlInt(i: Int) extends SqlValue {
  override def toString = i.toString
  override def toDouble = i
  override def toInt = i
  override def toLong = i
  override def bindValue(stmt: Long, col: Int) = Sqlite3C.bind_int64(stmt, col, i.toLong)
}
case class SqlLong(i: Long) extends SqlValue {
  override def toString = i.toString
  override def toDouble = i
  override def toInt =
    if (i <= Integer.MAX_VALUE && i >= Integer.MIN_VALUE) i.toInt else super.toInt
  override def toLong = i
  override def bindValue(stmt: Long, col: Int) = Sqlite3C.bind_int64(stmt, col, i)
}
case class SqlDouble(d: Double) extends SqlValue {
  override def toString = d.toString
  override def toDouble = d
  override def toInt = if (d.round == d) d.toInt else super.toInt
  override def toLong = if (d.round == d) d.toLong else super.toLong
  override def bindValue(stmt: Long, col: Int) = Sqlite3C.bind_double(stmt, col, d)
}
case class SqlBlob(bytes: Seq[Byte]) extends SqlValue {
    override def toBlob = bytes
    override def toString = new String(bytes.toArray)
    override def bindValue(stmt: Long, col: Int) = Sqlite3C.bind_blob(stmt, col, bytes.toArray)
}
case class SqlText(s: String) extends SqlValue {
    override def toString = s
    override def bindValue(stmt: Long, col: Int) = Sqlite3C.bind_text(stmt, col, s)
}

class SqliteStatement(db: SqliteDb, private val stmt: Array[Long]) {
    def query[R](params: SqlValue*)(f: Iterator[IndexedSeq[SqlValue]] => R): R = {
        params.foldLeft(1) { (i, param) => param.bindValue(stmt(0), i); i + 1 }
        try f(new SqliteResultIterator(db, stmt(0))) finally Sqlite3C.reset(stmt(0))
    }
    def foreachRow(params: SqlValue*)(f: IndexedSeq[SqlValue] => Unit) {
      query(params:_*) { i => i.foreach { row => f(row) } }
    }
    def execute(params: SqlValue*) { query(params:_*) { i => i.foreach { row => Unit } } }
    def close = if (stmt(0) != 0) stmt(0) = Sqlite3C.finalize(stmt(0))
}

class SqliteResultIterator(db: SqliteDb, private val stmt: Long)
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
                        case Sqlite3C.TEXT => SqlText(new String(Sqlite3C.column_blob(stmt, i)))
                        case Sqlite3C.BLOB => SqlBlob(Sqlite3C.column_blob(stmt, i))
                        case Sqlite3C.NULL => SqlNull()
                        case _ => sys.error("unsupported type")
                    }
                }
            case Sqlite3C.DONE =>
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
    def prepare[R](sql: String)(f: SqliteStatement => R): R = {
        assert(db(0) != 0, "db is closed")
        val stmtPointer = Array(0L)
        if (Sqlite3C.prepare_v2(db(0), sql, stmtPointer) != Sqlite3C.OK)
          throw new Exception(errmsg)
        val stmt = new SqliteStatement(this, stmtPointer)
        try f(stmt) finally stmt.close
    }
    def query[R](sql: String, params: SqlValue*)(f: Iterator[IndexedSeq[SqlValue]] => R): R =
      prepare(sql)(_.query(params:_*)(f))
    def foreachRow(sql: String, params: SqlValue*)(f: IndexedSeq[SqlValue] => Unit) =
      prepare(sql)( _.foreachRow(params:_*)(f))
    def execute(sql: String, params: SqlValue*) { prepare(sql)(_.execute(params:_*)) }
    def enableLoadExtension(on: Boolean) {
        Sqlite3C.enable_load_extension(db(0), if (on) 1 else 0)
    }
    def errmsg: String = if (db(0) == 0) "db not open" else Sqlite3C.errmsg(db(0))
}
