package brega.gabriele.deduplicationengine.fileio;

import brega.gabriele.deduplicationengine.controller.chunker.Chunker;
import brega.gabriele.deduplicationengine.model.Chunk;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@Service
public final class FileIO {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 1024;
    private final int bufferMaxSize = DEFAULT_BUFFER_SIZE;
    @Autowired
    private Chunker chunker;
    public FileIO(Chunker chunker) {
        this.chunker = chunker;
    }

    public void readFile(Path path) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        try (InputStream input = Files.newInputStream(path)) {
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                if (bytesRead == 0) {
                    continue;
                }

                chunker.setFileFinished(false);
                processBuffer(buffer, bytesRead);
            }

            chunker.setFileFinished(true);

            if (chunker.getBuffer().length > 0) {
                chunker.RLE(chunker.getBuffer());
            }
        }
    }

    private void processBuffer(byte[] buffer, int length) {
        byte[] leftover = chunker.getBuffer();
        byte[] data;

        if (leftover.length > 0) {
            data = new byte[leftover.length + length];

            System.arraycopy(leftover, 0, data, 0, leftover.length);
            System.arraycopy(buffer, 0, data, leftover.length, length);
        } else {
            data = Arrays.copyOf(buffer, length);
        }

        chunker.RLE(data);

    }


}

