// Copyright (c) 2010 Sean C. Rhea <sean.c.rhea@gmail.com>
// All rights reserved.
//
// See the file LICENSE included in this distribution for details.

// See the comments in Sqlite3C.java.  Also, the sqlite3 C/C++ interface
// supports UTF-16 strings.  I would like to switch this file to use UTF-16,
// as it would save Java the effort of converting to/from UTF-8, but I haven't
// sorted out the details of byte ordering yet.

#include "org_srhea_scalaqlite_Sqlite3C.h"
#include <stdio.h>
#include <string.h>
#include <sqlite3.h>

JNIEXPORT jint JNICALL
Java_org_srhea_scalaqlite_Sqlite3C_open(JNIEnv *env, jclass cls, jstring jpath, jlongArray jdb)
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
Java_org_srhea_scalaqlite_Sqlite3C_close(JNIEnv *env, jclass cls, jlong jdb)
{
    sqlite3 *db = (sqlite3*) jdb;
    return sqlite3_close(db);
}

JNIEXPORT jint JNICALL
Java_org_srhea_scalaqlite_Sqlite3C_enable_1load_1extension(JNIEnv *env, jclass cls, jlong jdb, jint onoff)
{
    sqlite3 *db = (sqlite3*) jdb;
    return sqlite3_enable_load_extension(db, onoff);
}

JNIEXPORT jint JNICALL
Java_org_srhea_scalaqlite_Sqlite3C_prepare_1v2(
    JNIEnv *env, jclass cls, jlong jdb, jstring jsql, jlongArray jstmt)
{
    sqlite3 *db = (sqlite3*) jdb;
    jboolean iscopy;
    const char *csql = env->GetStringUTFChars(jsql, &iscopy);
    sqlite3_stmt *stmt;
    int r = sqlite3_prepare_v2(db, csql, -1, &stmt, NULL);
    if (r == SQLITE_OK) {
        jlong a[] = {(jlong) stmt};
        env->SetLongArrayRegion(jstmt, 0, 1, a);
    }
    env->ReleaseStringUTFChars(jsql, csql);
    return r;
}

JNIEXPORT jint JNICALL Java_org_srhea_scalaqlite_Sqlite3C_reset
  (JNIEnv *env, jclass cls, jlong jstmt)
{
   sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
   return sqlite3_reset(stmt);
}

JNIEXPORT jint JNICALL Java_org_srhea_scalaqlite_Sqlite3C_bind_1null
  (JNIEnv *env, jclass cls, jlong jstmt, jint jindex)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return sqlite3_bind_null(stmt, jindex);
}

JNIEXPORT jint JNICALL Java_org_srhea_scalaqlite_Sqlite3C_bind_1int64
  (JNIEnv *env, jclass cls, jlong jstmt, jint jindex, jlong jvalue)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return sqlite3_bind_int64(stmt, jindex, (sqlite3_int64) jvalue);
}

JNIEXPORT jint JNICALL Java_org_srhea_scalaqlite_Sqlite3C_bind_1double
  (JNIEnv *env, jclass cls, jlong jstmt, jint jindex, jdouble jvalue)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return sqlite3_bind_double(stmt, jindex, jvalue);
}

JNIEXPORT jint JNICALL Java_org_srhea_scalaqlite_Sqlite3C_bind_1text
  (JNIEnv *env, jclass cls, jlong jstmt, jint jindex, jbyteArray jvalue)
{
    jint len = env->GetArrayLength(jvalue);
    jboolean iscopy;
    jbyte *cvalue = (jbyte *) env->GetByteArrayElements(jvalue, &iscopy);
    int r = sqlite3_bind_text((sqlite3_stmt*) jstmt, jindex, (const char *) cvalue, len, SQLITE_TRANSIENT);
    env->ReleaseByteArrayElements(jvalue, cvalue, 0);
    return r;
}

JNIEXPORT jint JNICALL Java_org_srhea_scalaqlite_Sqlite3C_bind_1blob
  (JNIEnv *env, jclass cls, jlong jstmt, jint jindex, jbyteArray jvalue)
{
    jint len = env->GetArrayLength(jvalue);
    jboolean iscopy;
    jbyte *cvalue = (jbyte *) env->GetByteArrayElements(jvalue, &iscopy);
    int r = sqlite3_bind_blob((sqlite3_stmt*) jstmt, jindex, (const char *) cvalue, len, SQLITE_TRANSIENT);
    env->ReleaseByteArrayElements(jvalue, cvalue, 0);
    return r;
}

JNIEXPORT jint JNICALL
Java_org_srhea_scalaqlite_Sqlite3C_step(JNIEnv *env, jclass cls, jlong jstmt)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return sqlite3_step(stmt);
}

JNIEXPORT jint JNICALL
Java_org_srhea_scalaqlite_Sqlite3C_finalize(JNIEnv *env, jclass cls, jlong jstmt)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return sqlite3_finalize(stmt);
}

JNIEXPORT jint JNICALL
Java_org_srhea_scalaqlite_Sqlite3C_column_1count(JNIEnv *env, jclass cls, jlong jstmt)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return sqlite3_column_count(stmt);
}

JNIEXPORT jint JNICALL
Java_org_srhea_scalaqlite_Sqlite3C_column_1type(JNIEnv *env, jclass cls, jlong jstmt, jint n)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return sqlite3_column_type(stmt, n);
}

JNIEXPORT jlong JNICALL
Java_org_srhea_scalaqlite_Sqlite3C_column_1int64(JNIEnv *env, jclass cls, jlong jstmt, jint n)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return sqlite3_column_int64(stmt, n);
}

JNIEXPORT jdouble JNICALL
Java_org_srhea_scalaqlite_Sqlite3C_column_1double(JNIEnv *env, jclass cls, jlong jstmt, jint n)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    return sqlite3_column_double(stmt, n);
}

JNIEXPORT jbyteArray JNICALL Java_org_srhea_scalaqlite_Sqlite3C_column_1blob
  (JNIEnv *env, jclass cls, jlong jstmt, jint n)
{
    sqlite3_stmt *stmt = (sqlite3_stmt*) jstmt;
    jsize len = sqlite3_column_bytes(stmt, n);
    jbyteArray result = env->NewByteArray(len);
    env->SetByteArrayRegion(result, 0, len, (jbyte *) sqlite3_column_blob(stmt, n));
    return result;
}

JNIEXPORT jstring JNICALL
Java_org_srhea_scalaqlite_Sqlite3C_errmsg(JNIEnv *env, jclass cls, jlong jdb)
{
    sqlite3 *db = (sqlite3*) jdb;
    return env->NewStringUTF(sqlite3_errmsg(db));
}

JNIEXPORT jint JNICALL
Java_org_srhea_scalaqlite_Sqlite3C_sqlite3_1changes(JNIEnv *, jclass, jlong jdb)
{
    return sqlite3_changes((sqlite3*) jdb);
}
