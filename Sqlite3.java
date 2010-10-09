class Sqlite3 {
    native static public int sqlite3_open(String path, long[] db);

    public static void main(String[] args) {
        long db[] = {0};
        int r = sqlite3_open(":memory:", db);
        System.out.println("r=" + r + ", db=" + Long.toHexString(db[0]));
    }

    static {
        System.loadLibrary("Sqlite3Java");
    }
}
