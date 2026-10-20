package brega.gabriele.deduplicationengine.controller.chunker.loggin;

import brega.gabriele.deduplicationengine.model.Chunk;

@FunctionalInterface
public interface ChunkConsumer {
    Chunk accept(Chunk chunk);
}
