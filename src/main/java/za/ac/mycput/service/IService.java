/*
 * IService.java
 * Generic service interface – the base contract for all services.
 * Author : Jonathan Munday (230677797)
 * Date   : 19 July 2026
 */
package za.ac.mycput.service;

/**
 * Generic CRUD service contract.
 *
 * @param <T>  the domain entity type (e.g. Lease)
 * @param <ID> the type of the entity's identifier (e.g. String)
 */
public interface IService<T, ID> {

    /**
     * Creates a new entity.
     *
     * @param t the entity to create
     * @return the created entity
     */
    T create(T t);

    /**
     * Reads an entity by its identifier.
     *
     * @param id the unique identifier
     * @return the found entity, or null if not found
     */
    T read(ID id);

    /**
     * Updates an existing entity.
     *
     * @param t the updated entity
     * @return the updated entity
     */
    T update(T t);

    /**
     * Deletes an entity by its identifier.
     *
     * @param id the unique identifier
     * @return true if deleted, false otherwise
     */
    boolean delete(ID id);
}
