import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

class ImmutableStore<T> {

    private T t;

    public ImmutableStore(T t) {
        this.t = t;
    }

    @SuppressWarnings("unchecked")
    public ImmutableStore(File fromFile) throws IOException, ClassNotFoundException {
        try (ObjectInputStream oin = new ObjectInputStream(new FileInputStream(fromFile))) {
            this.t = (T) oin.readObject();
        }
    }

    public T getT() {
        return t;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof ImmutableStore)
            return t.equals(((ImmutableStore<?>) other).t);
        else
            return false;
    }

    public List<String> sortByStringLengthRepresentation(ImmutableStore<?> other) {
        ArrayList<String> l = new ArrayList<>(2);
        l.add(this.t.toString());
        l.add(other.t.toString());
        l.sort((String s1, String s2) -> {
            return s1.length() - s2.length();
        });
        return l;
    }
}
