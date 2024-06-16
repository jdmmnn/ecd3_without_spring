package service_setup;

import ecd3.RepositoryException;

public class AccountAllReadyExistsException extends RepositoryException {

    public AccountAllReadyExistsException(String message) {
        super(message);
    }
}
