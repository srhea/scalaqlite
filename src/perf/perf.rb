# Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
# All rights reserved.
#
# See the file LICENSE included in this distribution for details.

# Use an in-memory db, as we're only trying to test the performance of the
# Ruby code, not the database itself.  Otherwise, the inserts are limited by
# disk write speed.
require 'rubygems'
require 'sqlite3'
db = SQLite3::Database.new(':memory:')
db.execute("CREATE TABLE foo (i INTEGER, f DOUBLE, t TEXT);")
start = Time.now
count = 100000
count.times do
  db.execute("INSERT INTO foo (i, f, t) VALUES (1, 2.0, 'foo');")
end
lat = (Time.now - start).to_f
puts("inserts took " + (lat*1000).to_s + " ms: " + (count / lat).to_s + " rows/s")
sum = 0
start = Time.now
db.execute("SELECT * FROM foo;") {|row|
  sum += row.first.to_i
}
if (count != sum); raise("count and sum don't match"); end
lat = (Time.now - start).to_f
puts("query took " + (lat*1000).to_s + " ms: " + (count / lat).to_s + " rows/s")
db.close
