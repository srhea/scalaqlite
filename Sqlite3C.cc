// Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
// All rights reserved.
//
// See the file LICENSE included in this distribution for details.

#include "Sqlite3C.h"
#include <stdio.h>
#include <string.h>
#include <sqlite3.h>

JNIEXPORT jint JNICALL
Java_Sqlite3C_open(JNIEnv *env, jclass cls, jstring jpath, jlongArray jdb)
{
    jboolean iscopy;
    const char *cpath = env->GetStringUTFChars(jpath, &iscopy);
    sqlite3 *db;
    jint r = sqlite3_open(cpath, &db);
    if (r == SQLITE_OK) {
        jlong a[] = {(jlong) db};
        env->SetLongArrayRegion(jdb, 0, 1, a);
    }
    env->ReleaseStringUTFChars(jpath, cpath);
    return r;
}

JNIEXPORT jint JNICALL
Java_Sqlite3C_close(JNIEnv *env, jclass cls, jlong jdb)
{
    sqlite3 *db = (sqlite3*) jdb;
    return sqlite3_close(db);
}

JNIEXPORT jint JNICALL
Java_Sqlite3C_enable_1load_1extension(JNIEnv *env, jclass cls, jlong jdb, jint onoff)
{
    sqlite3 *db = (sqlite3*) jdb;
    return sqlite3_enable_load_extension(db, onoff);
}

JNIEXPORT jint JNICALL
Java_Sqlite3C_prepare_1v2(
    JNIEnv *env, jclass cls, jlong jdb, jstring jsql, jlongArray jstmt)
{
    sqlite3 *db = (sqlite3*) jdb;
    jboolean iscopy;
    const char *csql = env->GetStringUTFChars(jsql, &iscopy);
    sqlite3_stmt *stmt;
    // XXX: does strlen work with UTF8?
    int r = sqlite3_prepare_v2(db, csql, strlen(csql), &stmt, NULL);
    if (r == SQLITE_OK) {
        jlong a[] = {(jlong) stmt};
        env->SetLongArrayRegion(jstmt, 0, 1, a);
    }
    env->ReleaseStringUTFChars(jsql, csql);
    return r;
}

JNIEXPORT jint JNICALL
Java_Sqlite3C_step(JNIEnv *env, jclass cls, jlong jstmt)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return sqlite3_step(stmt);
}

JNIEXPORT jint JNICALL
Java_Sqlite3C_finalize(JNIEnv *env, jclass cls, jlong jstmt)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return sqlite3_finalize(stmt);
}

JNIEXPORT jint JNICALL
Java_Sqlite3C_column_1count(JNIEnv *env, jclass cls, jlong jstmt)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return sqlite3_column_count(stmt);
}

JNIEXPORT jstring JNICALL
Java_Sqlite3C_column_1name(JNIEnv *env, jclass cls, jlong jstmt, jint n)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return env->NewStringUTF(sqlite3_column_name(stmt, n));
}

JNIEXPORT jint JNICALL
Java_Sqlite3C_column_1type(JNIEnv *env, jclass cls, jlong jstmt, jint n)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return sqlite3_column_type(stmt, n);
}

JNIEXPORT jint JNICALL
Java_Sqlite3C_column_1int(JNIEnv *env, jclass cls, jlong jstmt, jint n)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return sqlite3_column_int(stmt, n);
}

JNIEXPORT jfloat JNICALL
Java_Sqlite3C_column_1double(JNIEnv *env, jclass cls, jlong jstmt, jint n)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return sqlite3_column_double(stmt, n);
}

JNIEXPORT jstring JNICALL
Java_Sqlite3C_column_1text(JNIEnv *env, jclass cls, jlong jstmt, jint n)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return env->NewStringUTF((const char*) sqlite3_column_text(stmt, n));
}

JNIEXPORT jstring JNICALL
Java_Sqlite3C_errmsg(JNIEnv *env, jclass cls, jlong jdb)
{
    sqlite3 *db = (sqlite3*) jdb;
    return env->NewStringUTF(sqlite3_errmsg(db));
}
