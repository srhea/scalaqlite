class Sqlite3 {
    public static final int SQLITE_OK = 0;
    public static final int SQLITE_DONE = 101;

    native static public int sqlite3_open(String path, long[] db);
    native static public int sqlite3_close(long db);
    native static public int sqlite3_enable_load_extension(long db, int onoff);
    native static public int sqlite3_prepare_v2(long db, String sql, long[] stmt);
    native static public int sqlite3_step(long stmt);
    native static public int sqlite3_finalize(long stmt);
    native static public String sqlite3_errmsg(long db);

    public static void main(String[] args) throws Exception {
        long db[] = {0};
        int r = sqlite3_open(":memory:", db);
        if (r != SQLITE_OK)
            throw new Exception("open failed(" + r + "): " + sqlite3_errmsg(db[0]));
        r = sqlite3_enable_load_extension(db[0], 1);
        if (r != SQLITE_OK)
            throw new Exception("enable_load_extension failed(" + r + "): " + sqlite3_errmsg(db[0]));
        long stmt[] = {0};
        r = sqlite3_prepare_v2(db[0], "create table foo (i integer, f double, t text);", stmt);
        if (r != SQLITE_OK)
            throw new Exception("prepare_v2 failed(" + r + "): " + sqlite3_errmsg(db[0]));
        r = sqlite3_step(stmt[0]);
        if (r != SQLITE_DONE)
            throw new Exception("step failed(" + r + "): " + sqlite3_errmsg(db[0]));
        r = sqlite3_finalize(stmt[0]);
        if (r != SQLITE_OK)
            throw new Exception("finalize failed(" + r + "): " + sqlite3_errmsg(db[0]));

        r = sqlite3_prepare_v2(db[0], "insert into foo (i, f, t) values (1, 2.0, 'foo');", stmt);
        if (r != SQLITE_OK)
            throw new Exception("prepare_v2 failed(" + r + "): " + sqlite3_errmsg(db[0]));
        r = sqlite3_step(stmt[0]);
        if (r != SQLITE_DONE)
            throw new Exception("step failed(" + r + "): " + sqlite3_errmsg(db[0]));
        r = sqlite3_finalize(stmt[0]);
        if (r != SQLITE_OK)
            throw new Exception("finalize failed(" + r + "): " + sqlite3_errmsg(db[0]));

        r = sqlite3_close(db[0]);
        if (r != SQLITE_OK)
            throw new Exception("close failed(" + r + "): " + sqlite3_errmsg(db[0]));
    }

    static {
        System.loadLibrary("Sqlite3Java");
    }
}
