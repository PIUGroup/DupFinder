package de.b0n.dir.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.junit.Test;

public class DuplicateLengthFinderTest {
	
	private static final Predicate<Entry<Long, List<Path>>> hasSingleItemInEntry = entry -> entry.getValue()
			.size() < 2;

	private static final String PATH_SAME_SIZE_PathS_IN_TREE_FOLDER = "src/test/resources/duplicateTree";
	private static final String PATH_Path = "src/test/resources/Test1.txt";
	private static final String PATH_INVALID_FOLDER = "src/test/resourcesInvalid/";
	private static final String PATH_NO_SAME_SIZE_FOLDER = "src/test/resources/duplicateTree/subfolder";
	private static final String PATH_SAME_SIZE_FOLDER = "src/test/resources/noDuplicates";
	private static final String PATH_EMPTY_FOLDER = "src/test/resources/emptyFolder";
	private static final String PATH_PLENTY_SAME_SIZE_FOLDER = "src/test/resources/";
	private static final String PATH_SAME_SIZE_IN_FLAT_FOLDER = "src/test/resources/folderOnlyFolder/flatDuplicateTree";
	private static final String PATH_FOLDER_ONLY_FOLDER = "src/test/resources/folderOnlyFolder";

	@Test(expected = IllegalArgumentException.class)
	public void noArgumentFolder() {
		DuplicateLengthFinder.getResult(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void scanInvalidFolder() {
		final Path folder = Paths.get(PATH_INVALID_FOLDER);
		DuplicateLengthFinder.getResult(folder);
	}

	@Test(expected = IllegalArgumentException.class)
	public void scanPath() {
		final Path folder = Paths.get(PATH_Path);
		DuplicateLengthFinder.getResult(folder);
	}

	@Test
	public void scanFlatFolder() {
		final Path folder = Paths.get(PATH_SAME_SIZE_IN_FLAT_FOLDER);
		final Map<Long, List<Path>> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 1, result.values().size());
		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2,
				result.values().iterator().next().size());
	}

	@Test
	public void scanFolderOnlyFolder() {
		final Path folder = Paths.get(PATH_FOLDER_ONLY_FOLDER);
		final Map<Long, List<Path>> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 1, result.values().size());
		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2,
				result.values().iterator().next().size());
	}

	@Test
	public void scanEmptyFolder() throws IOException {
		final Path folder = Paths.get(PATH_EMPTY_FOLDER);
		if (Files.createDirectory(folder, (FileAttribute<?>) null) != null) {
			final Map<Long, List<Path>> result = DuplicateLengthFinder.getResult(folder);
			assertNotNull(result);
			assertTrue(result.values().isEmpty());
			Files.delete(folder);
		}
	}

	@Test
	public void scanNoDuplicates() {
		final Path folder = Paths.get(PATH_NO_SAME_SIZE_FOLDER);
		final Map<Long, List<Path>> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		assertEquals("Es darf nur eine Gruppe gefunden werden", 1, result.values().size());
		assertEquals("In der gefundenen Gruppe darf nur ein Element sein", 1, result.values().iterator().next().size());
	}
	
	@Test
	public void scanDuplicates() {
		final Path folder = Paths.get(PATH_SAME_SIZE_FOLDER);
		final Map<Long, List<Path>> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		assertEquals("Es darf nur eine Gruppe gefunden werden", 1, result.values().size());
		assertEquals("In der gefundenen Gruppe dürfen nur zwei Element sein", 2, result.values().iterator().next().size());
	}

	@Test
	public void scanDuplicatesInTree() {
		final Path folder = Paths.get(PATH_SAME_SIZE_PathS_IN_TREE_FOLDER);
		final Map<Long, List<Path>> result = DuplicateLengthFinder.getResult(folder);
		assertNotNull(result);
		Iterator<List<Path>> elementsIterator = result.values().iterator();
		assertEquals(1, result.values().size());
		assertEquals(2, elementsIterator.next().size());
	}

	@Test
	public void scanDuplicatesInBiggerTree() {
		final Path folder = Paths.get(PATH_PLENTY_SAME_SIZE_FOLDER);
		Map<Long, List<Path>> result = new HashMap<>();

		DuplicateLengthFinder.getResult(folder);
		result.entrySet().parallelStream().filter(hasSingleItemInEntry).forEach(entry -> result.remove(entry.getKey()));

		assertEquals("falsche Anzahl an Dateien gleicher Größe bestimmt", 2, result.values().size());
		Iterator<List<Path>> elementsIterator = result.values().iterator();
		assertEquals("falsche Anzahl von 26 Byte-Datei Vorkommen bestimmt", 2, elementsIterator.next().size());
		assertEquals("falsche Anzahl von 91 Byte-Datei Vorkommen bestimmt", 6, elementsIterator.next().size());
	}
}
