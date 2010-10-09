class Sqlite3 {
    public static final int SQLITE_OK = 0;
    public static final int SQLITE_ROW = 100;
    public static final int SQLITE_DONE = 101;

    public static final int SQLITE_INTEGER = 1;
    public static final int SQLITE_FLOAT = 2;
    public static final int SQLITE_TEXT = 3;
    public static final int SQLITE_BLOB = 4;
    public static final int SQLITE_NULL = 5;

    native static public int sqlite3_open(String path, long[] db);
    native static public int sqlite3_close(long db);
    native static public int sqlite3_enable_load_extension(long db, int onoff);
    native static public int sqlite3_prepare_v2(long db, String sql, long[] stmt);
    native static public int sqlite3_step(long stmt);
    native static public int sqlite3_finalize(long stmt);
    native static public int sqlite3_column_count(long stmt);
    native static public String sqlite3_column_name(long stmt, int n);
    native static public int sqlite3_column_type(long stmt, int n);
    native static public int sqlite3_column_int(long stmt, int n);
    native static public float sqlite3_column_double(long stmt, int n);
    native static public String sqlite3_column_text(long stmt, int n);
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

        r = sqlite3_prepare_v2(db[0], "select * from foo;", stmt);
        if (r != SQLITE_OK)
            throw new Exception("prepare_v2 failed(" + r + "): " + sqlite3_errmsg(db[0]));
        for (;;) {
            r = sqlite3_step(stmt[0]);
            if (r == SQLITE_DONE)
                break;
            if (r != SQLITE_ROW)
                throw new Exception("step failed(" + r + "): " + sqlite3_errmsg(db[0]));
            for (int i = 0; i < sqlite3_column_count(stmt[0]); ++i) {
                if (i != 0) System.out.print(" ");
                switch (sqlite3_column_type(stmt[0], i)) {
                   case SQLITE_INTEGER:
                       System.out.println(Integer.toString(sqlite3_column_int(stmt[0], i)));
                       break;
                   case SQLITE_FLOAT:
                       System.out.println(Double.toString(sqlite3_column_double(stmt[0], i)));
                       break;
                   case SQLITE_TEXT:
                       System.out.println(sqlite3_column_text(stmt[0], i));
                       break;
                   default:
                       throw new Exception("unknown column type");
                }
            }
            System.out.println("");
        }
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
