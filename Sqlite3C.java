public class Sqlite3C {
    public static final int OK = 0;
    public static final int ROW = 100;
    public static final int DONE = 101;

    public static final int INTEGER = 1;
    public static final int FLOAT = 2;
    public static final int TEXT = 3;
    public static final int BLOB = 4;
    public static final int NULL = 5;

    native static public int open(String path, long[] db);
    native static public int close(long db);
    native static public int enable_load_extension(long db, int onoff);
    native static public int prepare_v2(long db, String sql, long[] stmt);
    native static public int step(long stmt);
    native static public int finalize(long stmt);
    native static public int column_count(long stmt);
    native static public String column_name(long stmt, int n);
    native static public int column_type(long stmt, int n);
    native static public int column_int(long stmt, int n);
    native static public float column_double(long stmt, int n);
    native static public String column_text(long stmt, int n);
    native static public String errmsg(long db);

    static {
        System.loadLibrary("scalaqlite");
    }
}
