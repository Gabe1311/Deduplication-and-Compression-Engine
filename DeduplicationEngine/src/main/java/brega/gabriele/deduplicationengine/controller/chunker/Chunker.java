package brega.gabriele.deduplicationengine.controller.chunker;
import brega.gabriele.deduplicationengine.controller.chunker.loggin.ChunkConsumer;
import brega.gabriele.deduplicationengine.controller.chunker.loggin.LoggingChunkConsumer;
import brega.gabriele.deduplicationengine.fileio.FileIO;
import brega.gabriele.deduplicationengine.model.Chunk;
import brega.gabriele.deduplicationengine.model.Manifest;
import brega.gabriele.deduplicationengine.model.Operation;
import brega.gabriele.deduplicationengine.model.repositories.ChunkRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
@Service
public class Chunker {
    private long[] gearTable;

    //temporary values
    private long strictMask = 3;
    private long looseMask = 7;
    private long min =3;
    private long max =10;
    private long target = 6;
    private long minRleOper = 2;
    private byte[] buffer;
    private boolean isFileFinished = true;
    private ChunkConsumer chunkConsumer;
    private ChunkRepository chunkRepository;

    public Chunker(LoggingChunkConsumer chunkConsumer, ChunkRepository chunkRepository) {
        this.chunkConsumer = chunkConsumer;
        this.chunkRepository = chunkRepository;

        generateGearTable();
        buffer = new byte[0];
    }

    public Manifest RLE(byte[] data){
        if(data.length == 0)
            return null;

        byte[] result = new byte[data.length];
        Manifest manifest = new Manifest();
        List<Operation> operations = new ArrayList<>();
        long count = 1L;
        long resultIndex = 1;
        result[0] = data[0];
        for(int i = 1; i < data.length; i++){
            if(data[i] == data[i-1]){
                count++;
            }else{
                if(count >1){
                    operations.add(new Operation(count, resultIndex -1 ));
                    count = 1L;
                }
                result[(int) resultIndex++] = data[i];
            }
        }
        if (count > 1) {
            operations.add(
                    new Operation(count,  (resultIndex -1))
            );
        }
        manifest.addOperations(operations);

        return fastCDChunker( Arrays.copyOfRange(result, 0, (int) resultIndex),manifest);
    }

    public Manifest fastCDChunker(byte[] data, Manifest manifest){
        List<Chunk> chunks = new ArrayList<>();
        int chunkStart = 0;
        long fingerprint = 0;
        for(int i = 0; i < data.length; i++){
            long mask;
            if(i-chunkStart >= target){
                mask=looseMask;
            }else{
                mask= strictMask;
            }

            int index = data[i] & 0xFF;
            fingerprint = (fingerprint << 1) +gearTable[index];


            if((i-chunkStart >= min && (fingerprint & mask) ==0) ||i-chunkStart +1 >= max ){
                Chunk chunk = new Chunk();
                chunk.setDataAndHash( Arrays.copyOfRange(data,chunkStart,i+1));
                chunks.add(chunkConsumer.accept(chunk));
                chunkStart = i+1;
                fingerprint = 0;
            }


        }
        if (chunkStart < data.length && isFileFinished) {
            Chunk chunk = new Chunk();
            chunk.setDataAndHash(Arrays.copyOfRange(data,chunkStart, data.length));
            chunks.add(chunkConsumer.accept(chunk));
            buffer = new byte[0];
        }else{
            buffer = Arrays.copyOfRange(data,chunkStart,data.length);
        }
        manifest.addChunks(chunks);

        return manifest;
    }






    private void generateGearTable() {
        long[] table = new long[256];
        Random random = new Random(123456789L);

        for (int i = 0; i < table.length; i++) {
            table[i] = random.nextLong();
        }
        gearTable = table;
    }

    public boolean isFileFinished() {
        return isFileFinished;
    }

    public void setFileFinished(boolean fileFinished) {
        isFileFinished = fileFinished;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }
}
