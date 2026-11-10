package za.ac.mycput.domain.exception;

public class DuplicateEntityException extends RuntimeException {

    public DuplicateEntityException(String entity, String id) {
        super(entity + " " + id + " already exists");
    }
}
