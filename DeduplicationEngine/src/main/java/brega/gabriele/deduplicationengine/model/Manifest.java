package brega.gabriele.deduplicationengine.model;

import brega.gabriele.deduplicationengine.utility.HashingUtil;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table
public class Manifest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long manifest_id;

    @Column(unique = true, nullable = false)
    private String hash;

    @ManyToOne
    @JoinColumn(name = "file_id")
    private File file;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    @JoinTable(name = "Manifest_Chunk",
            joinColumns = @JoinColumn(name = "manifest_id"),
            inverseJoinColumns = @JoinColumn(name = "chunkId"))
    private List<Chunk> chunks;

    @Transient
    private List<Operation> operations;



    public Manifest(List<Chunk> chunks, List<Operation> operations) {
        this.chunks = chunks;

        this.operations = operations;
        HashingUtil.sha256Manifest(operations,chunks);
    }

    public Manifest() {
        chunks = new ArrayList<>();
        operations = new ArrayList<>();
    }







    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    public Long getId() {
        return manifest_id;
    }

    public void setId(Long id) {
        this.manifest_id = id;
    }

    public List<Chunk> getChunks() {
        return chunks;
    }

    public void setChunks(List<Chunk> chunks) {
        this.chunks = chunks;
    }

    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }

    public void addChunks(List<Chunk> chunks) {
        this.chunks.addAll(chunks);
    }
    public void addOperations(List<Operation> operations) {
        this.operations.addAll(operations);
    }
}
