package brega.gabriele.deduplicationengine.controller.service;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ManifestMerger {

    private static final int MAGIC = 0x444D414E; // DMAN
    private static final int VERSION = 1;

    public static Path merge(
            ManifestWriter writer,
            Path finalManifest) throws IOException {

        Files.createDirectories(finalManifest.toAbsolutePath().getParent());

        Path tempManifest = Files.createTempFile(
                finalManifest.toAbsolutePath().getParent(),
                "manifest-", ".tmp");

        try (DataOutputStream output = new DataOutputStream(
                new BufferedOutputStream(
                        Files.newOutputStream(tempManifest)))) {

            output.writeInt(MAGIC);
            output.writeInt(VERSION);

            copyChunks(writer.getChunksFile(), output);
            copyOperations(writer.getOperationsFile(), output);

            output.writeByte(ManifestWriter.END_RECORD);
        }

        Files.move(
                tempManifest,
                finalManifest,
                StandardCopyOption.REPLACE_EXISTING);

        Files.deleteIfExists(writer.getChunksFile());
        Files.deleteIfExists(writer.getOperationsFile());

        return finalManifest;
    }

    private static void copyChunks(
            Path file,
            DataOutputStream output) throws IOException {

        try (DataInputStream input = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {

            while (input.available() > 0) {
                byte type = input.readByte();
                long chunkId = input.readLong();

                int hashLength = input.readInt();
                byte[] hash = input.readNBytes(hashLength);

                if (hash.length != hashLength) {
                    throw new EOFException("Incomplete chunk record");
                }

                output.writeByte(type);
                output.writeLong(chunkId);
                output.writeInt(hashLength);
                output.write(hash);
            }
        }
    }

    private static void copyOperations(
            Path file,
            DataOutputStream output) throws IOException {

        try (DataInputStream input = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(file)))) {

            while (input.available() > 0) {
                byte type = input.readByte();
                byte operationType = input.readByte();
                long position = input.readLong();
                long amount = input.readLong();

                output.writeByte(type);
                output.writeByte(operationType);
                output.writeLong(position);
                output.writeLong(amount);
            }
        }
    }
}
