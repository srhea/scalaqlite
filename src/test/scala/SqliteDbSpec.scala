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
        db.query("SELECT count(*) FROM foo;").toSeq.head.head should equal (SqlInt(2))
    }

    "SELECT *" should "output all the rows" in {
        var list: List[String] = Nil
        for (row <- db.query("SELECT * FROM foo;"))
            list = row.map(_.toString).mkString(" ") :: list
        list.reverse.mkString("\n") should equal ("1 2.0 foo\n3 NULL bar")
    }

    "SqliteResultSet.map" should "work" in {
        val s = db.query("SELECT * FROM foo;").map(_.mkString(" ")).mkString("\n")
        s should equal ("1 2.0 foo\n3 NULL bar")
    }

    "SqliteResultSet.filter" should "work" in {
        for (row <- db.query("SELECT * FROM foo;") if row(2).toString == "bar")
          row.mkString(" ") should equal ("3 NULL bar")
    }

    "doubles" should "have full precision" in {
        val d = db.query("SELECT 1234567890123.0;").map(_.map(_.toDouble)).toSeq.head.head
        d should equal (1234567890123.0)
    }

    "longs" should "have full precision" in {
        val low = Integer.MIN_VALUE - 1L
        val high = Integer.MAX_VALUE + 1L
        val i = db.query("SELECT " + low + ";").toSeq.head.head
        i.isInstanceOf[SqlLong] should equal (true)
        i.toLong should equal (low)
        val j = db.query("SELECT " + high + ";").toSeq.head.head
        j.isInstanceOf[SqlLong] should equal (true)
        j.toLong should equal (high)
    }

    "values that fit in an int" should "be returned as an int" in {
      db.query("SELECT " + Integer.MIN_VALUE + ";").toSeq.head.head.isInstanceOf[SqlInt] should equal (true)
      db.query("SELECT " + Integer.MAX_VALUE + ";").toSeq.head.head.isInstanceOf[SqlInt] should equal (true)
    }
}
