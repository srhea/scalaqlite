// Copyright (c) 2010-2012 Sean C. Rhea <sean.c.rhea@gmail.com>
// All rights reserved.
//
// See the file LICENSE included in this distribution for details.

package org.srhea.scalaqlite;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class Sqlite3C implements Library {
  static {
    Native.register("sqlite3");
  }

  public static final int OK = 0;
  public static final int ERROR = 1;
  public static final int ROW = 100;
  public static final int DONE = 101;

  public static final int INTEGER = 1;
  public static final int FLOAT = 2;
  public static final int TEXT = 3;
  public static final int BLOB = 4;
  public static final int NULL = 5;

  public static native int sqlite3_open(String path, PointerByReference db);
  public static native int sqlite3_close(Pointer db);
  public static native int sqlite3_enable_load_extension(Pointer db, int onoff);
  public static native int sqlite3_prepare_v2(Pointer db, String sql, int len, PointerByReference stmt, PointerByReference tail);
  public static native int sqlite3_step(Pointer stmt);
  public static native int sqlite3_finalize(Pointer stmt);
  public static native int sqlite3_column_count(Pointer stmt);
  public static native String sqlite3_column_name(Pointer stmt, int n);
  public static native int sqlite3_column_type(Pointer stmt, int n);
  public static native int sqlite3_column_int(Pointer stmt, int n);
  public static native double sqlite3_column_double(Pointer stmt, int n);
  public static native String sqlite3_column_text(Pointer stmt, int n);
  public static native String sqlite3_errmsg(Pointer db);
}
