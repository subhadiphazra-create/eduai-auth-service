package com.eduai.auth.mapper;

/**
 * Generic mapper interface — all mappers in the system implement this contract.
 * Enforces a consistent toEntity/toResponse pattern and makes mappers easily testable
 * and swappable (e.g., switching from manual mapping to MapStruct without changing callers).
 *
 * @param <E> Entity type
 * @param <RQ> Request DTO type (for create operations)
 * @param <RS> Response DTO type
 */
public interface GenericMapper<E, RQ, RS> {

    /**
     * Convert a request DTO to an entity.
     * Typically called during create operations.
     * Audit fields (id, createdAt, updatedAt) should NOT be set here — JPA handles them.
     */
    E toEntity(RQ request);

    /**
     * Convert an entity to a response DTO.
     * Called for all read/write operations that return data to the client.
     */
    RS toResponse(E entity);
}
