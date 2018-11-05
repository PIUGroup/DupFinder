package de.b0n.dir.processor;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Sucht in einem gegebenen Verzeichnis und dessen Unterverzeichnissen nach
 * Dateien und sortiert diese nach Dateigröße.
 */
public class DuplicateLengthFinder {

	/**
	 * Einstiegsmethode zum Durchsuchen eines Verzeichnisses nach Dateien gleicher
	 * Größe.
	 * 
	 * @param folder
	 *            Zu durchsuchendes Verzeichnis
	 * @return 
	 */
	public static Map<Long, List<Path>> getResult(final Path folder) {
		if (folder == null) {
			throw new IllegalArgumentException("folder may not be null.");
		}
		
		File folder_ = folder.toFile();
		if (!folder_.exists()) {
			throw new IllegalArgumentException("folder must exist.");
		}
		if (!folder_.isDirectory()) {
			throw new IllegalArgumentException("folder must be a valid folder.");
		}
		
		try (Stream<Path> filesStream = Files.walk(folder, FileVisitOption.FOLLOW_LINKS)) {
			return filesStream.parallel().filter(path -> !path.toFile().isDirectory()).collect(Collectors.groupingByConcurrent(t -> {
				long size = 0L;
				try {
					size = Files.size(t);
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
				return size;
			}));
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
