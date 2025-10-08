package pl.kielce.tu.backend.service.validation;

import pl.kielce.tu.backend.exception.ValidationException;

public interface Validator<T> {

    void validate(T object) throws ValidationException;

}
