package ecd3;

public class Operation {
    String aggregateName;
    String methodName;
    Object[] args;

    public Operation(String aggregateName, String aggregateId, Object[] parameters) {
        this.aggregateName = aggregateName;
        this.methodName = aggregateId;
        this.args = parameters;
    }
}
