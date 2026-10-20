package brega.gabriele.deduplicationengine.model;


import brega.gabriele.deduplicationengine.utility.HashingUtil;
import jakarta.persistence.*;
import jakarta.persistence.Transient;

@Entity
@Table(name = "Chunk")
public class Chunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long chunkId;

    @Column(unique = true, nullable = false)
    private String hash;

    @Transient
    byte[] data;

    public Chunk( byte[] data) {
        this.data = data;
        hash = HashingUtil.sha256(data);
    }

    public Chunk() {
    }

    public byte[] getData() {
        return data;
    }

    public void setDataAndHash(byte[] data) {
        this.data = data;
        hash = HashingUtil.sha256(data);
    }

    public String toString() {
        return data.toString();
    }

    public long getChunkId() {
        return chunkId;
    }

    public void setChunkId(long chunkId) {
        this.chunkId = chunkId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
