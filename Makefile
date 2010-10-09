
all: Sqlite3C.class libSqlite3Java.dylib

clean:
	rm Sqlite3C.class Sqlite3C.h libSqlite3Java.dylib

run: Sqlite3C.class libSqlite3Java.dylib
	scala-2.8 sqlite3.scala

.PHONY: all, clean, run

libSqlite3Java.dylib: Sqlite3C.cc Sqlite3C.h
	g++ -dynamiclib -o $@ \
		-I /System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers \
		-L/opt/local/lib \
		$< -lsqlite3

Sqlite3C.class: Sqlite3C.java
	javac $<

Sqlite3C.h: Sqlite3C.java
	javah -jni Sqlite3C

cex: main.o
	g++ -o $@ $^ -lsqlite3

main.o: main.cc
	g++ -c -W -Wall -Werror -I../sqlite-3.5.6 $<
