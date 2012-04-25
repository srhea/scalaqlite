// Copyright (c) 2010-2012 Sean C. Rhea <sean.c.rhea@gmail.com>
// All rights reserved.
//
// See the file LICENSE included in this distribution for details.

package org.srhea.scalaqlite

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.ptr.PointerByReference

trait Sqlite3C extends Library {
  def sqlite3_open(path: String, db: PointerByReference): Int
  def sqlite3_close(db: Pointer): Int
  def sqlite3_enable_load_extension(db: Pointer, onoff: Int): Int
  def sqlite3_prepare_v2(db: Pointer, sql: String, len: Int, stmt: PointerByReference, tail: PointerByReference): Int
  def sqlite3_step(stmt: Pointer): Int
  def sqlite3_finalize(stmt: Pointer): Int
  def sqlite3_column_count(stmt: Pointer): Int
  def sqlite3_column_name(stmt: Pointer, n: Int): String
  def sqlite3_column_type(stmt: Pointer, n: Int): Int
  def sqlite3_column_int(stmt: Pointer, n: Int): Int
  def sqlite3_column_double(stmt: Pointer, n: Int): Double
  def sqlite3_column_text(stmt: Pointer, n: Int): String
  def sqlite3_errmsg(db: Pointer): String
}

object Sqlite3C {
  final val OK = 0
  final val ERROR = 1
  final val ROW = 100
  final val DONE = 101

  final val INTEGER = 1
  final val FLOAT = 2
  final val TEXT = 3
  final val BLOB = 4
  final val NULL = 5

  final val Instance = Native.loadLibrary("sqlite3", classOf[Sqlite3C]).asInstanceOf[Sqlite3C]
}
