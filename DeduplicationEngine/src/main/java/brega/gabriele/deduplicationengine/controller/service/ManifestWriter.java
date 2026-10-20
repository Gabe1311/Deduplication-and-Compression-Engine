
package brega.gabriele.deduplicationengine.controller.service;

import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

public class ManifestWriter implements AutoCloseable {

    static final byte CHUNK_RECORD = 1;
    static final byte OPERATION_RECORD = 2;
    static final byte END_RECORD = 0;

    private final Path chunksFile;
    private final Path operationsFile;

    private DataOutputStream chunksOutput;
    private DataOutputStream operationsOutput;

    public ManifestWriter(Path tempDirectory) throws IOException {
        Files.createDirectories(tempDirectory);

        chunksFile = Files.createTempFile(
                tempDirectory, "chunks-", ".tmp");

        operationsFile = Files.createTempFile(
                tempDirectory, "operations-", ".tmp");

        chunksOutput = new DataOutputStream(
                new BufferedOutputStream(
                        Files.newOutputStream(chunksFile)));

        operationsOutput = new DataOutputStream(
                new BufferedOutputStream(
                        Files.newOutputStream(operationsFile)));
    }

    public void writeChunk(long chunkId, String hash)
            throws IOException {

        byte[] hashBytes = hash.getBytes(StandardCharsets.US_ASCII);

        chunksOutput.writeByte(CHUNK_RECORD);
        chunksOutput.writeLong(chunkId);
        chunksOutput.writeInt(hashBytes.length);
        chunksOutput.write(hashBytes);
    }

    public void writeOperation(
            byte operationType,
            long position,
            long amount) throws IOException {

        operationsOutput.writeByte(OPERATION_RECORD);
        operationsOutput.writeByte(operationType);
        operationsOutput.writeLong(position);
        operationsOutput.writeLong(amount);
    }

    public void finish() throws IOException {
        chunksOutput.close();
        operationsOutput.close();
    }

    Path getChunksFile() {
        return chunksFile;
    }

    Path getOperationsFile() {
        return operationsFile;
    }

    @Override
    public void close() throws IOException {
        finish();
    }
}
