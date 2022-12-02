package query.condition;

/**
 * This is an abstract class for all the query types.
 */
public abstract class QueryCondition {

    private QueryMode mode = QueryMode.Client;

    public QueryMode getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = QueryMode.valueOf(mode);
    }

    public enum QueryMode {
        Client("Client"),
        Filter("Filter"),
        Coprocessor("Coprocessor");
        private String name;

        QueryMode(String name) {
            this.name = name;
        }
    }
}
