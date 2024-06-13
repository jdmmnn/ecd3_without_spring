package ecd3.domain;

import java.io.Serializable;
import java.util.Arrays;

public class Operation implements Serializable {
    public String aggregateName;
    public String methodName;
    public OperationEnum cud;
    public Object[] args;

    public Operation(String aggregateName, String aggregateId, OperationEnum cud, Object[] parameters) {
        this.aggregateName = aggregateName;
        this.methodName = aggregateId;
        this.cud = cud;
        this.args = parameters;
    }

    @Override
    public String toString() {
        return "Operation{" +
                "aggregateName='" + aggregateName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", cud=" + cud +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
