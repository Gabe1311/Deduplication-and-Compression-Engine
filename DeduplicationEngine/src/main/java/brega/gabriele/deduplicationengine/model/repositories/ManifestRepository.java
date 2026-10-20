package brega.gabriele.deduplicationengine.model.repositories;


import brega.gabriele.deduplicationengine.model.Manifest;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface ManifestRepository extends CrudRepository<Manifest, Long> {
    Optional<Manifest> findByHash(String hash);
}
