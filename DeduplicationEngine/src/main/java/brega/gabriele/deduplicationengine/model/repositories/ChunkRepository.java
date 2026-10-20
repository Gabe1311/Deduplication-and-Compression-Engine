package brega.gabriele.deduplicationengine.model.repositories;

import brega.gabriele.deduplicationengine.model.Chunk;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface ChunkRepository
        extends CrudRepository<Chunk, Long> {
    Optional<Chunk> findByHash(String hash);
}
