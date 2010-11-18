# Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
# All rights reserved.
#
# See the file LICENSE included in this distribution for details.

ifeq ($(shell uname),Darwin)
LIBEXT=dylib
else
LIBEXT=so
endif

TARGET=libscalaqlite.$(LIBEXT)

all: $(TARGET)

clean:
	rm -f Sqlite3C.h $(TARGET)

.PHONY: all, clean

libscalaqlite.so: Sqlite3C.cc Sqlite3C.h
	g++ -O2 -o $@ -shared -Wl,-soname,libscalaqlite.so \
		-I/usr/lib/jvm/java-1.5.0-sun-1.5.0.22/include \
		-I/usr/lib/jvm/java-1.5.0-sun-1.5.0.22/include/linux \
		$< -static -lc -lsqlite3

libscalaqlite.dylib: Sqlite3C.cc Sqlite3C.h
	g++ -O2 -dynamiclib -o $@ \
		-I /System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers \
		-L/opt/local/lib \
		$< -lsqlite3

Sqlite3C.h: ../classes/org/srhea/scalaqlite/Sqlite3C.class
	javah -classpath ../classes org.srhea.scalaqlite.Sqlite3C
