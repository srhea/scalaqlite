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
        db.query("SELECT count(*) FROM foo;").row(0) should equal (SqlInt(2))
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
        val d = db.query("SELECT 1234567890123.0;").map(_.map( _.toDouble)).head.head
        d should equal (1234567890123.0)
    }
}
