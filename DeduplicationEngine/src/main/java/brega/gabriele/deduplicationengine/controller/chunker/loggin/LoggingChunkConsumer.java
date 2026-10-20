package brega.gabriele.deduplicationengine.controller.chunker.loggin;

import brega.gabriele.deduplicationengine.controller.service.ChunkStorageService;
import brega.gabriele.deduplicationengine.fileio.FileIO;
import brega.gabriele.deduplicationengine.model.Chunk;
import brega.gabriele.deduplicationengine.model.repositories.ChunkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;


@Component
public class LoggingChunkConsumer implements ChunkConsumer {

    private final ChunkRepository chunkRepository;
    private final ChunkStorageService chunkStorageService;

    public LoggingChunkConsumer(
            ChunkRepository chunkRepository,
            ChunkStorageService chunkStorageService) {
        this.chunkRepository = chunkRepository;
        this.chunkStorageService = chunkStorageService;
    }

    @Override
    public Chunk accept(Chunk chunk) {
        // Reuse an existing chunk if its hash is already stored.
        Optional<Chunk> existing =
                chunkRepository.findByHash(chunk.getHash());

        if (existing.isPresent()) {
            System.out.println(chunk.getHash() + " already exists");
            return existing.get();

        }

        // Save metadata first to generate the chunk ID.
        Chunk savedChunk = chunkRepository.save(chunk);

        try {
            // Save the bytes using the generated database ID.
            chunkStorageService.saveChunk(savedChunk);
            return savedChunk;
        } catch (IOException e) {
            // Attempt to remove the database record if disk storage fails.
            try {
                chunkRepository.delete(savedChunk);
            } catch (RuntimeException cleanupException) {
                e.addSuppressed(cleanupException);
            }

            throw new UncheckedIOException(
                    "Failed to save chunk " + savedChunk.getHash(), e);
        }
    }
}
