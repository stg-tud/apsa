class Constants {
    // CONSTANTS ARE (TYPICALLY) INLINED AT COMPILE TIME!

    public static final String LECTURE_TITLE = "APSA";

    public static final long C = 299792458l;// speed of light in a vacuum

    public static Long c() {
        return 299792458l; // speed of light in a vacuum
    }

    public static Long getC() {
        return C; // speed of light in a vacuum
    }

    public static String getLectureTitle() {
        return LECTURE_TITLE;
    }
}
