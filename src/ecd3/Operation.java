package ecd3;

import java.io.Serializable;

public class Operation implements Serializable {
    String aggregateName;
    String methodName;
    Object[] args;

    public Operation(String aggregateName, String aggregateId, Object[] parameters) {
        this.aggregateName = aggregateName;
        this.methodName = aggregateId;
        this.args = parameters;
    }
}
