import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

class SynchronizedStore<T> {

    private static SynchronizedStore<Object> NULL_STORE = null;

    public static SynchronizedStore<Object> getNULL_STORE() {
        if (NULL_STORE == null) {
            synchronized(SynchronizedStore.class) {
                if (NULL_STORE == null) {
                    NULL_STORE = new SynchronizedStore<Object>(null);
                }
            }
        }
        return NULL_STORE;
    }

    private T t;

    public SynchronizedStore(T t) {
        this.t = t;
    }

    public synchronized T getT() {
        return t;
    }

    public synchronized void setT(T t) {
        this.t = t;
    }
}
