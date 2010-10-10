# Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
# All rights reserved.
#
# See the file LICENSE included in this distribution for details.

all: Sqlite.class Sqlite3C.class libscalaqlite.dylib

clean:
	rm -f *.class Sqlite3C.h libscalaqlite.dylib

test: all
	scala test.scala

.PHONY: all, clean, test

libscalaqlite.dylib: Sqlite3C.cc Sqlite3C.h
	g++ -dynamiclib -o $@ \
		-I /System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers \
		-L/opt/local/lib \
		$< -lsqlite3

Sqlite.class: Sqlite.scala Sqlite3C.class
	scalac $<

Sqlite3C.class: Sqlite3C.java
	javac $<

Sqlite3C.h: Sqlite3C.java
	javah -jni Sqlite3C
