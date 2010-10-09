#include "Sqlite3.h"
#include <stdio.h>
#include <sqlite3.h>

JNIEXPORT jint JNICALL
Java_Sqlite3_sqlite3_1open(JNIEnv *env, jclass cls, jstring jpath, jlongArray jdb)
{
    printf("sqlite3_open\n");
    jboolean iscopy;
    const char *cpath = env->GetStringUTFChars(jpath, &iscopy);
    sqlite3 *db;
    jint r = sqlite3_open(cpath, &db);
    if (r == SQLITE_OK) {
        jlong a[] = {(jlong) db};
        env->SetLongArrayRegion(jdb, 0, 1, a);
    }
    return r;
}

JNIEXPORT jint JNICALL
Java_Sqlite3_sqlite3_1enable_1load_1extension(JNIEnv *env, jclass cls, jlong jdb, jint onoff)
{
    printf("sqlite3_enable_load_extension\n");
    sqlite3 *db = (sqlite3*) jdb;
    return sqlite3_enable_load_extension(db, onoff);
}

