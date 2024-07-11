package com.anrisoftware.dwarfhustle.model.db.lmbd

import static java.lang.Math.round

import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Collectors

/**
 * Utils.
 */
class Utils {

    static void listFiles(def log, Path path) {
        Files.list(path).withCloseable {
            it.filter({file -> !Files.isDirectory(file)}).collect(Collectors.toSet()).each {
                def size = it.toFile().size()
                log.info "Size {} = {} B, {} KB, {} MB, {} GB.", it, size, round(size / 1024), round(size / 1024 / 1024), round(size / 1024 / 1024 / 1024)
            }
        }
    }
}
