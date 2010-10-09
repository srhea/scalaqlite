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
