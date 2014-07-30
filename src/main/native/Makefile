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

SOURCES=Sqlite3C.cc
OBJECTS=$(SOURCES:.cpp=.o)
INSTALLDIR=../../../target/native
SCALAVER=2.10.3

all: $(TARGET)

clean:
	rm -f Sqlite3C.h $(TARGET)
	rm -rf *.o
	rm -rf ${INSTALLDIR}

libscalaqlite.so: $(OBJECTS) Sqlite3C.h
	g++ -O2 -shared -fPIC -o $@ -shared -Wl,-soname,libscalaqlite.so \
	        -I${JAVA_HOME}/include \
	        -I${JAVA_HOME}/include/linux \
		$< -lc -lsqlite3

.cpp.o:
	$(CC) $(CFLAGS) $< -o $@

libscalaqlite.dylib: Sqlite3C.cc Sqlite3C.h
	g++ -O2 -dynamiclib -o $@ \
		-I /System/Library/Frameworks/JavaVM.framework/Versions/Current/Headers \
		-L/opt/local/lib \
		$< -lsqlite3

install:$(TARGET)
	mkdir -p ../../../target/native
	install -m 0755 -t ../../../target/native $<

Sqlite3C.h: ../../../target/scala-${SCALAVER}/classes/org/srhea/scalaqlite/Sqlite3C.class
	javah -classpath ../../../target/scala-${SCALAVER}/classes org.srhea.scalaqlite.Sqlite3C

.PHONY: all, clean, install
