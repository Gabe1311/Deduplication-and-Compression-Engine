package brega.gabriele.deduplicationengine.controller.service;

import brega.gabriele.deduplicationengine.model.Chunk;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ChunkStorageService {

    public void saveChunk(Chunk chunk) throws IOException {
        Path directory = Path.of("storage", "chunk");
        Files.createDirectories(directory);

        Path filePath = directory.resolve(
                chunk.getChunkId() + ".chunk"
        );

        try (OutputStream out = Files.newOutputStream(filePath)) {
            out.write(chunk.getData());
        }
    }
}
