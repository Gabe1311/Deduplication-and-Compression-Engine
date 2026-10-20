package brega.gabriele.deduplicationengine.model;

import jakarta.persistence.*;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;


@Entity
@Table
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String originalPath;
    private LocalDate creationDate;


    public File() {
    }

    public File(Path originalPath, LocalDate creationDate) {
        this.originalPath = originalPath.toString();
        this.creationDate = creationDate;
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public void setOriginalPath(Path originalPath) {
        this.originalPath = originalPath.toString();
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setOriginalPath(String originalPath) {
        this.originalPath = originalPath;
    }
}
