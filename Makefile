
all: Sqlite3.class libSqlite3Java.dylib

.PHONY: all

libSqlite3Java.dylib: Sqlite3.cc Sqlite3.h
	g++ -dynamiclib -o $@ \
		-I /System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers \
		-L/opt/local/lib \
		$< -lsqlite3

Sqlite3.class: Sqlite3.java
	javac $<

Sqlite3.h: Sqlite3.class
	javah -jni Sqlite3

cex: main.o
	g++ -o $@ $^ -lsqlite3

main.o: main.cc
	g++ -c -W -Wall -Werror -I../sqlite-3.5.6 $<
