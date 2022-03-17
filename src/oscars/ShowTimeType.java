package oscars;

/** Names for the start and end times of the broadcast */
public enum ShowTimeType {
    START,
    END;

    @Override
    public String toString() {
        return "Show Time " + name();
    }
}