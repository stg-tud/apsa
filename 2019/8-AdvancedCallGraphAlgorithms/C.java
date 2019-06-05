class C {

    static void  m(Object[]a, Object[][] b) {
        a = b;
        a[0] = b;
        a = b[1];
    }
}