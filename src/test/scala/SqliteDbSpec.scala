// Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
// All rights reserved.
//
// See the file LICENSE included in this distribution for details.

package org.srhea.scalaqlite
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class SqliteDbSpec extends FlatSpec with ShouldMatchers {
    val db = new SqliteDb(":memory:")

    "CREATE TABLE" should "not throw any exceptions" in {
        db.execute("CREATE TABLE foo (i INTEGER, f DOUBLE, t TEXT);")
    }

    "INSERT" should "add rows to the table" in {
        db.execute("INSERT INTO foo (i, f, t) VALUES (1, 2.0, 'foo');")
        db.execute("INSERT INTO foo (i, f, t) VALUES (3, NULL, 'bar');")
        db.foreachRow("SELECT count(*) FROM foo;") { row => row(0) should equal (SqlLong(2)) }
    }

    "a prepared statement INSERT" should "add rows to the table" in {
        db.prepare("INSERT INTO foo (i, f, t) VALUES (?, ?, ?);") { stmt =>
            stmt.execute(SqlLong(5), SqlDouble(10.0), SqlText("foobar"))
            stmt.execute(SqlLong(5), SqlDouble(11.0), SqlText("notfoobar"))
        }
        db.foreachRow("SELECT count(*) FROM foo WHERE i > 4") { row => row(0) should equal (SqlLong(2)) }
    }

    "SELECT *" should "output all the rows" in {
        var list: List[String] = Nil
        db.foreachRow("SELECT * FROM foo;") { row =>
            list = row.map(_.toString).mkString(" ") :: list
        }
        list.reverse.mkString("\n") should equal ("1 2.0 foo\n3 NULL bar\n5 10.0 foobar\n5 11.0 notfoobar")
    }

    "SqliteResultSet.map" should "work" in {
        val s = db.query("SELECT * FROM foo;") { _.map(_.mkString(" ")).mkString("\n") }
        s should equal ("1 2.0 foo\n3 NULL bar\n5 10.0 foobar\n5 11.0 notfoobar")
    }

    "SqliteResultSet.filter" should "work" in {
        db.foreachRow("SELECT * FROM foo") { row => if (row(2).toString == "bar")
          row.mkString(" ") should equal ("3 NULL bar")
        }
    }

    "doubles" should "have full precision" in {
        db.foreachRow("SELECT 1234567890123.0;") { row => row(0).toDouble should equal (1234567890123.0) }
    }

    "longs" should "have full precision" in {
        val low = Integer.MIN_VALUE - 1L
        val high = Integer.MAX_VALUE + 1L
        db.foreachRow("SELECT " + low + ";") { row =>
          row(0).isInstanceOf[SqlLong] should equal (true)
          row(0).toLong should equal (low)
        }
        db.foreachRow("SELECT " + high + ";") { row =>
          row(0).isInstanceOf[SqlLong] should equal (true)
          row(0).toLong should equal (high)
        }
    }

    "values that fit in an int" should "be returned as an int" in {
      db.foreachRow("SELECT " + Integer.MIN_VALUE + ";") { row => row(0).isInstanceOf[SqlLong] should equal (true) }
      db.foreachRow("SELECT " + Integer.MAX_VALUE + ";") { row => row(0).isInstanceOf[SqlLong] should equal (true) }
    }

    "values that don't fit in an int" should "throw an exception on toInt" in {
      intercept[SqlException] {
        db.foreachRow("SELECT " + (Integer.MAX_VALUE + 1L) + ";") { row => row(0).toInt }
      }
    }

    "Non-ascii characters" should "be bound correctly" in {
      val str = "百"
      db.execute("CREATE TABLE non_ascii_test (str TEXT)")
      db.execute("INSERT INTO non_ascii_test VALUES (?1)", SqlText(str))
      db.foreachRow("SELECT str FROM non_ascii_test") { case Seq(SqlText(s)) =>
        s should equal(str)
        s.getBytes should equal(Array[Byte](-25, -103, -66))
      }
    }

    "Prepared statements" should "properly reset themselves" in {
      db.execute("CREATE TABLE bar (i INTEGER, d DOUBLE);")
      db.execute("INSERT INTO bar (i, d) VALUES (1, 2.0);")
      db.execute("INSERT INTO bar (i, d) VALUES (1, 3.0);")
      db.execute("INSERT INTO bar (i, d) VALUES (1, 4.0);")
      db.execute("INSERT INTO bar (i, d) VALUES (2, 5.0);")
      db.prepare("SELECT * FROM bar WHERE i = ?;") { stmt =>
        stmt.query(SqlLong(1)) { i => i.hasNext should equal(true)
          i.next()(1).toDouble should equal (2.0) }
        stmt.query(SqlLong(2)) { i => i.hasNext should equal(true)
          i.next()(1).toDouble should equal (5.0) }
      }
    }

    "Bind columns " should " have correct type affinity" in {
      val barBlob = SqlBlob("bar".map(_.toByte))
      val barText = SqlText("bar")
      val barBlobString = "X'" + "bar".map(c => "%02X".format(c.toByte)).mkString("") + "'"
      db.getRows("SELECT 1 AS x WHERE ?1 = ?2", barBlob, barText).length should equal (0)
      db.getRows("SELECT 1 AS x WHERE ?1 = " + barBlobString, barBlob).length should equal (1)
      db.getRows("SELECT 1 AS x WHERE ?1 = 'bar'", barBlob).length should equal (0)
      db.getRows("SELECT 1 AS x WHERE ?1 = 'bar'", barText).length should equal (1)

      db.execute("CREATE TEMP TABLE blob_test (b BLOB, t TEXT)")
      db.execute("INSERT INTO blob_test (b, t) VALUES (?1, ?2)", barBlob, barText)

      db.getRows("SELECT COUNT(*) FROM blob_test")(0)(0).toLong should equal(1)
      db.getRows("SELECT COUNT(*) FROM blob_test WHERE b = " + barBlobString)(0)(0).toLong should equal(1)
      db.getRows("SELECT COUNT(*) FROM blob_test WHERE b = 'bar'")(0)(0).toLong should equal(0)
      db.getRows("SELECT COUNT(*) FROM blob_test WHERE t = " + barBlobString)(0)(0).toLong should equal(0)
      db.getRows("SELECT COUNT(*) FROM blob_test WHERE t = 'bar'")(0)(0).toLong should equal(1)

      val types = db.mapRows("SELECT TYPEOF(b), TYPEOF(t) FROM blob_test")(_.map(_.toString).mkString(", "))
      types.mkString should equal ("blob, text")
      db.execute("DROP TABLE blob_test")
    }

    "Sql nulls" should "work" in {
      db.getRows("SELECT NULL").head.head should equal (SqlNull)
    }

    "Pattern matching" should "work for longs and nulls" in {
      db.foreachRow("SELECT 1, NULL") { case Seq(SqlLong(i), SqlNull) => i should equal (1) }
    }

    "Inserting nulls" should "work" in {
      db.execute("CREATE TABLE null_test (x, y)")
      db.execute("INSERT INTO null_test (x, y) VALUES (?, ?)", SqlLong(1), SqlNull)
      db.execute("INSERT INTO null_test (x, y) VALUES (?, ?)", SqlLong(2))
      db.execute("INSERT INTO null_test (x, y) VALUES (?, ?)", SqlNull, SqlLong(3))
      db.getRows("SELECT * FROM null_test WHERE x = 1").head should equal (Seq(SqlLong(1), SqlNull))
      db.getRows("SELECT * FROM null_test WHERE x = 2").head should equal (Seq(SqlLong(2), SqlNull))
      db.getRows("SELECT * FROM null_test WHERE y IS NOT NULL").head should equal (Seq(SqlNull, SqlLong(3)))
    }
}
