package brega.gabriele.deduplicationengine.controller;

import brega.gabriele.deduplicationengine.fileio.FileIO;
import brega.gabriele.deduplicationengine.model.File;
import org.springframework.stereotype.Controller;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

@Controller
public class DedupController {
    private FileIO fileIO;
    public DedupController(FileIO fileIO) {
        this.fileIO = fileIO;

    }


    public void addFileToSystem(Path path){
        File file = new File(path, LocalDate.now());
        try {
            fileIO.readFile(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
