# Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
# All rights reserved.
#
# See the file LICENSE included in this distribution for details.

ifeq ($(shell uname),Darwin)
LIBEXT=dylib
else
LIBEXT=so
endif

all: SqliteDb.class Sqlite3C.class libscalaqlite.$(LIBEXT)

clean:
	rm -f *.class Sqlite3C.h libscalaqlite.$(LIBEXT) perf.out

test: all
	LD_LIBRARY_PATH=. scala test.scala

.PHONY: all, clean, test

libscalaqlite.so: Sqlite3C.cc Sqlite3C.h
	g++ -O2 -o $@ -shared -Wl,-soname,$@ \
		-I/usr/lib/jvm/java-1.5.0-sun-1.5.0.22/include \
		-I/usr/lib/jvm/java-1.5.0-sun-1.5.0.22/include/linux \
		$< -static -lc -lsqlite3

libscalaqlite.dylib: Sqlite3C.cc Sqlite3C.h
	g++ -O2 -dynamiclib -o $@ \
		-I /System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers \
		-L/opt/local/lib \
		$< -lsqlite3

SqliteDb.class: Sqlite.scala Sqlite3C.class
	fsc $<

Sqlite3C.class: Sqlite3C.java
	javac $<

Sqlite3C.h: Sqlite3C.java
	javah -jni Sqlite3C

perf.out: perf.cc
	g++ -O2 -o $@ $< -lsqlite3
