class Operators {

    // NO NEGATION

    public static boolean neg(boolean b) {
        return !b;
    }

    // NO SHORTCUT EVALUATION

    public static void checkInRange(long lowerBound, long upperBound, int value) {
        if (value < lowerBound || value > upperBound)
            throw new RuntimeException("value out of range");
    }
}