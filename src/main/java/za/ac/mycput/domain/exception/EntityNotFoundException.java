package za.ac.mycput.domain.exception;

public class EntityNotFoundException extends RuntimeException {

    private final String entity;

    public EntityNotFoundException(String entity, String id) {
        super(entity + " " + id + " was not found");
        this.entity = entity;
    }

    public String entity() {
        return entity;
    }
}
