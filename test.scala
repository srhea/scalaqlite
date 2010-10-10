// Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
// All rights reserved.
//
// See the file LICENSE included in this distribution for details.

var db = new SqliteDb(":memory:")
db.execute("CREATE TABLE foo (i INTEGER, f DOUBLE, t TEXT);")
db.execute("INSERT INTO foo (i, f, t) VALUES (1, 2.0, 'foo');")
db.execute("INSERT INTO foo (i, f, t) VALUES (3, NULL, 'bar');")
for (row <- db.query("SELECT * FROM foo;"))
    println(row.map(_.toString).mkString(" "))
db.close()
