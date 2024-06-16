package service_setup;

import ecd3.RepositoryException;

public class InsufficientFundsException extends RepositoryException {

    public InsufficientFundsException(String message) {
        super(message);
    }
}
