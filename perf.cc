// Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
// All rights reserved.
//
// See the file LICENSE included in this distribution for details.

#include <assert.h>
#include <stdio.h>
#include <sqlite3.h>
#include <string.h>
#include <stdlib.h>
#include <sys/time.h>

using namespace std;

void
execute(sqlite3 *db, const char *sql)
{
    sqlite3_stmt *stmt;
    int r = sqlite3_prepare_v2(db, sql, strlen(sql), &stmt, NULL);
    if (r != SQLITE_OK) {
        printf("error: %s\n", sqlite3_errmsg(db));
        exit(1);
    }
    for (;;) {
        r = sqlite3_step(stmt);
        if (r == SQLITE_DONE)
            break;
    }
    sqlite3_finalize(stmt);
}

const timeval &
operator-=(timeval &a, const timeval &b) {
    if (b.tv_usec > a.tv_usec) {
        a.tv_sec -= 1;
        a.tv_usec += 1000000;
    }
    a.tv_sec -= b.tv_sec;
    a.tv_usec -= b.tv_usec;
    return a;
}

int
main()
{
    sqlite3 *db;
    // Use an in-memory db, as we're only trying to test the performance of
    // the C++ code, not the database itself.  Otherwise, the inserts are
    // limited by disk write speed.
    int r = sqlite3_open(":memory:", &db);
    execute(db, "CREATE TABLE foo (i INTEGER, f DOUBLE, t TEXT);");
    int count = 100000;
    struct timeval start, lat;
    gettimeofday(&start, NULL);
    for (int i = 0; i < count; ++i)
        execute(db, "INSERT INTO foo (i, f, t) VALUES (1, 2.0, 'foo');");
    gettimeofday(&lat, NULL);
    lat -= start;
    double lat_us = lat.tv_sec * 1000 * 1000 + lat.tv_usec;
    printf("inserts took %f ms: %f rows/s\n", lat_us/1000, count/lat_us*1000*1000);
    int sum = 0;
    gettimeofday(&start, NULL);
    const char *sql = "SELECT * FROM foo;";
    sqlite3_stmt *stmt;
    sqlite3_prepare_v2(db, sql, strlen(sql), &stmt, NULL);
    for (;;) {
        int r = sqlite3_step(stmt);
        if (r == SQLITE_DONE)
            break;
        sum += sqlite3_column_int(stmt, 0);
    }
    assert(sum == count);
    sqlite3_finalize(stmt);
    gettimeofday(&lat, NULL);
    lat -= start;
    lat_us = lat.tv_sec * 1000 * 1000 + lat.tv_usec;
    printf("query took %f ms: %f rows/s\n", lat_us/1000, count/lat_us*1000*1000);
    sqlite3_close(db);
}
