package service_setup;

import ecd3.RepositoryException;

public class NoAccountFoundException extends RepositoryException {

    public NoAccountFoundException(String message) {
        super(message);
    }
}
