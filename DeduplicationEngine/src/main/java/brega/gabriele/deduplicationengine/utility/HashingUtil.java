package brega.gabriele.deduplicationengine.utility;

import brega.gabriele.deduplicationengine.model.Chunk;
import brega.gabriele.deduplicationengine.model.Operation;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;

public class HashingUtil {

    public static String sha256(byte[] data){
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);

            return HexFormat.of().formatHex(hash);
        }catch(NoSuchAlgorithmException e){
            throw new IllegalStateException("SHA-256 algorithm not found",e);
        }
    }

    public static String sha256Manifest(List<Operation> operations, List<Chunk> chunks) {
        StringBuilder sb = new StringBuilder();
        for (Operation operation : operations) {
            sb.append("#" + operation.getAmount() + "*" + operation.getPosition());
        }
        sb.append("\n");
        for (Chunk chunk : chunks) {
            sb.append(chunk.getHash()+ " ");
        }

        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);

            return HexFormat.of().formatHex(hash);
        }catch(NoSuchAlgorithmException e){
            throw new IllegalStateException("SHA-256 algorithm not found",e);
        }
    }
}
