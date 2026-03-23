/*
 * IRepository.java
 * Generic repository interface – the base contract for all repositories.
 * Author : Jonathan Munday (230677797)
 * Date   : 20 March 2026
 */
package za.ac.mycput.repository;

import java.util.List;

/**
 * Generic CRUD repository contract.
 *
 * <p>Following the "code to abstraction, not concretion" principle,
 * all concrete repositories must implement this interface so that
 * callers depend only on the interface, never on a specific class.</p>
 *
 * @param <T>  the domain entity type (e.g. {@code Property})
 * @param <ID> the type of the entity's identifier (e.g. {@code String})
 */
public interface IRepository<T, ID> {

    /**
     * Persists a new entity.
     *
     * @param entity the entity to save
     * @return the saved entity, or {@code null} on failure
     */
    T create(T entity);

    /**
     * Retrieves an entity by its identifier.
     *
     * @param id the unique identifier
     * @return the found entity, or {@code null} if not found
     */
    T read(ID id);

    /**
     * Replaces an existing entity with updated data.
     *
     * @param entity the updated entity (matched by ID)
     * @return the updated entity, or {@code null} on failure
     */
    T update(T entity);

    /**
     * Removes an entity by its identifier.
     *
     * @param id the unique identifier of the entity to remove
     * @return {@code true} if the entity was removed, {@code false} otherwise
     */
    boolean delete(ID id);

    /**
     * Returns every entity currently in the store.
     *
     * @return an unmodifiable list of all entities
     */
    List<T> getAll();
}
