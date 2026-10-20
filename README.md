# Java Deduplication-and-Compression-Engine

A Java-based file deduplication and compression project exploring how content-defined chunking, cryptographic hashing, and data transformation techniques can reduce redundant storage while preserving the ability to reconstruct original files.

The project combines algorithmic experimentation with practical backend engineering. It is being developed to investigate the trade-offs between storage efficiency, processing performance, implementation complexity, and data integrity.

## Project Overview

When storing multiple large files, a significant amount of data may be repeated across files or between different versions of the same file. Storing every byte independently wastes space, especially when files contain similar content.

This project aims to address that problem by splitting files into variable-sized chunks, identifying identical chunks using SHA-256 hashes, and storing unique chunk data separately from the metadata needed to reconstruct the original files.

Unlike approaches that divide files into fixed-size blocks, the engine uses **content-defined chunking (CDC)**. Chunk boundaries are determined by the content itself, helping unchanged regions remain identifiable even when insertions or deletions shift their positions within a file.

The project is currently under development. The core chunking, hashing, RLE, and chunk-storage components are being developed into a complete pipeline, while the manifest format, reconstruction process, reliability mechanisms, and additional compression techniques remain areas of ongoing work.

## How It Works

### 1. Content-Defined Chunking

The engine uses a Gear-based rolling fingerprint to identify potential chunk boundaries.

As the file is processed, each byte contributes to a rolling fingerprint. The fingerprint is checked against configurable bit masks, subject to minimum and maximum chunk-size constraints. When a boundary condition is met, the current chunk is emitted and the fingerprint is reset for the next chunk.

This approach is useful for deduplication because fixed-size chunking can lose alignment after a small insertion or deletion. With content-defined chunking, subsequent boundaries can realign according to the data, allowing unchanged regions to remain candidates for reuse.

The chunking parameters can be tuned to explore how average chunk size, boundary detection, processing cost, and deduplication effectiveness interact.

### 2. SHA-256 Chunk Identification

Each chunk is hashed using SHA-256 to produce a content identifier.

When a chunk is processed, the engine can use its hash to check whether identical content has already been stored. If a matching chunk exists, the existing stored chunk can be reused instead of storing another copy. Otherwise, the new chunk is persisted.

This separates **logical file content** from **physical chunk storage**: multiple files can reference the same stored chunk.

SHA-256 provides a practical content-identification mechanism, although hash matches should not be treated as a mathematical guarantee of byte-for-byte equality. A robust implementation should verify content when required and handle persistence conflicts safely.

### 3. Run-Length Encoding (RLE)

Run-Length Encoding is implemented as an additional data transformation stage.

RLE represents consecutive repetitions of the same byte using a more compact representation. For example, a long sequence of identical bytes can be represented by a value and information describing the run, rather than storing every repeated byte independently.

RLE is included for two reasons:

- **Compression experimentation:** It provides a relatively simple technique for reducing repetitive data and investigating how preprocessing affects the amount of data subsequently chunked and stored.
- **Deduplication experimentation:** Transforming data before chunking may change chunk boundaries and the resulting hashes. This makes it useful to investigate whether preprocessing improves or reduces the opportunity to identify duplicate chunks.

RLE is not expected to improve every file. Files with few repeated bytes may see little benefit, and a transformation can introduce overhead or change chunk alignment. Its value must therefore be evaluated against an untransformed baseline.

Because RLE changes the representation of the data, the engine must also preserve the information required to reverse the transformation and restore the original bytes exactly.

### 4. Persistent Chunk Storage

Chunk metadata and chunk data serve different purposes.

The database is intended to manage metadata such as chunk identifiers, hashes, and relationships between files and their chunks. The actual chunk bytes are stored separately on disk.

This design avoids treating the database as the storage location for all file content and provides a foundation for managing large files without loading entire files into memory.

The current implementation includes database-backed chunk metadata and a chunk-storage service. Consistency between database records and disk files, crash recovery, and validation of stored data are still important development goals.

### 5. Manifest-Based File Representation

A manifest records the information needed to describe how a file is represented by stored chunks and transformation operations.

The planned binary manifest format includes versioning and typed records, providing a basis for recording ordered chunk references and the operations needed to restore the original representation.

The manifest format is currently in progress. Reliable reconstruction requires more than identifying the correct chunks: ordering, operation metadata, format validation, and the exact inverse of every transformation must all be handled correctly.

## Development Roadmap

The project is being developed incrementally, prioritizing correctness and a reliable end-to-end pipeline before adding more advanced algorithms.

### Current implementation

- Java and Spring Boot application structure.
- Gear-based content-defined chunking.
- SHA-256 hashing for chunk identification.
- Run-Length Encoding implementation.
- Database-backed chunk metadata and a separate chunk-storage service.
- Development of a versioned binary manifest format.

These components are not yet equivalent to a fully validated, production-ready deduplication system. Integration, testing, reconstruction, and reliability work remain necessary.

### Planned improvements

**1. Reliable file reconstruction**

Complete the manifest-driven reconstruction pipeline so that files can be restored byte-for-byte. This includes preserving chunk order, interpreting transformation metadata correctly, and testing empty files, boundary cases, and large inputs.

**2. LZ4 compression**

Introduce LZ4 as a practical compression option and compare its storage and processing costs with the existing RLE stage. The goal is to understand how general-purpose compression interacts with deduplication and whether compression should occur before or after particular processing stages.

**3. SimHash-based similarity detection**

Explore SimHash as a way to identify potentially similar chunks, rather than limiting the system to exact duplicate detection. Similarity detection could help discover opportunities that ordinary hash equality cannot identify.

SimHash is not a replacement for SHA-256: similarity fingerprints can identify candidates, while exact content identification and validation remain separate concerns.

**4. Delta encoding**

Investigate storing differences between similar chunks instead of storing each chunk in full. This could reduce storage requirements when chunks share substantial content, but it introduces additional complexity: delta creation, application, metadata, reference management, and reconstruction must all be handled correctly.

The project will need to evaluate whether the savings justify the additional processing and implementation costs.

**5. Crash recovery and storage integrity**

Improve consistency between the database, chunk files, and manifests. Planned work includes validating stored data, handling incomplete operations, recovering from interrupted writes, and ensuring that original files are not discarded before their replacements have been stored and verified.

**6. Performance and scalability**

Evaluate the engine with larger files and datasets, investigate processing bottlenecks, and improve memory efficiency and storage throughput where the measurements justify doing so.

## Benchmarking Strategy

Benchmarking is being introduced alongside development, rather than postponed until the entire project is finished. Early measurements can expose obvious performance problems and help guide implementation decisions.

However, comprehensive benchmarking will become more meaningful once the main pipeline is integrated and reconstruction correctness has been established.

Comparing isolated algorithms before their interactions are understood can produce misleading results. For example, RLE may reduce the number of bytes processed by the chunker while also changing chunk boundaries. LZ4 may reduce stored size but increase processing time. Delta encoding may save space while increasing reconstruction cost.

A useful evaluation must therefore measure both individual components and the complete system.

Planned measurements include:

- **Storage reduction:** Original size compared with the total storage required by unique chunks, metadata, manifests, and transformation data.
- **Deduplication effectiveness:** How much data is reused across files and file versions.
- **Processing throughput:** Time required to ingest and process files.
- **Reconstruction performance:** Time required to restore files from their stored representation.
- **Memory usage:** Behavior when processing files larger than available working memory.
- **Algorithm comparisons:** The effects of RLE, LZ4, chunking parameters, and future similarity or delta techniques.
- **Correctness:** Byte-for-byte comparison between original and reconstructed files.

Measurements will be performed on repeatable datasets and under documented conditions. The goal is not simply to maximize a compression ratio, but to understand the trade-offs between storage savings, ingestion time, reconstruction time, memory consumption, and system complexity.

## Engineering Goals

Beyond implementing deduplication algorithms, this project provides an opportunity to explore practical software engineering challenges, including:

- Streaming file processing and memory management.
- Separation of metadata from stored binary data.
- Database persistence and content-addressed storage.
- Versioned binary formats and deterministic serialization.
- Data integrity, failure handling, and recovery.
- Automated correctness tests and reproducible performance experiments.

## Project Status

This is an ongoing personal engineering project combining backend development with experimentation in storage optimization.

The immediate priority is to complete and validate the core workflow: ingest a file, identify and persist its chunks, write a reliable manifest, and reconstruct the original file correctly. Advanced compression, similarity detection, delta encoding, and more extensive performance evaluation will build on that foundation.

The long-term objective is to develop a measurable, reliable deduplication engine and use it to investigate how different algorithms and architectural decisions affect real-world storage efficiency.
