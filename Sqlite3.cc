#include "Sqlite3.h"
#include <stdio.h>
JNIEXPORT jint JNICALL
Java_Sqlite3_sqlite3_1open(JNIEnv *, jclass, jstring, jlongArray)
{
    printf("Hello, world!");
}
