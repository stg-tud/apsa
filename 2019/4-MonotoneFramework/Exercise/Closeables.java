import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

class Closeables implements AutoCloseable{

    //
    // TRUE POSITIVES
    //

    static void tp0() throws Exception {
        Closeable c = new FileInputStream("Test.txt");
        print(c);
    }

    static void tp1() throws Exception {
        Closeable c = new FileInputStream("Test.txt");
        abort();
        if (c != null)
            c.close();
    }

    static void tp2(boolean b) throws Exception {
        Closeable c = new FileInputStream("Test.txt");
        if (b)
            c.close();
    }

    //
    // TRY TO PROVOKE FALSE POSITIVES
    //

    public Closeables() {
        super();
    }

    public void close() { /* OK */ }


    static void fp0() throws Exception {
        try (Closeable c = new FileInputStream("Test.txt")) {
            print(c);
        } catch (IOException ioe) {
            print("That's bad!");
        }
    }

    static void fp1(Closeable c) throws Exception {
        // We don't care, because the closeable is not created by this method!
        print(c);
        if (c != null)
            c.close();
    }

    static Closeable c = null;

    static void fp2_1() throws Exception {
        Closeables.c = new FileInputStream("Test.txt");
    }

    static void fp2_2() throws Exception {
        // We don't care, because the closeable is not created by this method!
        Closeable c = Closeables.c;
        if (c != null)
            c.close();
    }

    static void fp3(boolean b) throws Exception {
        Closeable c = b ? new FileOutputStream("Test.txt") : new FileInputStream("Test.txt");
        try {
            print("Great!");
        } finally {
            if (c != null)
                c.close();
        }

    }

    static void fp4() throws Exception {
        Closeable c = new FileInputStream("Test.txt");
        c.close();
    }

    // HELPERS

    private static void abort() {
        throw new UnknownError();
    }

    private static void print(Object o) {
        System.out.println(o);
    }
}