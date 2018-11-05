package de.b0n.dir.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

public class DuplicateContentFinderTest {

	private static final String PATH_FILE_1A = "src/test/resources/Test1.txt";
	private static final String PATH_FILE_1B = "src/test/resources/noDuplicates/Test1.txt";
	private static final String PATH_FILE_2A = "src/test/resources/Test2.txt";
	private static final String PATH_FILE_2B = "src/test/resources/noDuplicates/Test2.txt";

	private static final DuplicateContentFinderCallback FAILING_DCF_CALLBACK = new FailingDuplicateContentFinderCallback();

	@Test(expected = IllegalArgumentException.class)
	public void noArguments() {
		DuplicateContentFinder.getResult(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void noArgumentCallback() {
		final Queue<Path> input = new ConcurrentLinkedQueue<Path>();
		DuplicateContentFinder.getResult(input, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void noArgumentInput() {
		DuplicateContentFinder.getResult(null, FAILING_DCF_CALLBACK);
	}

	@Test
	public void scanFailingInputWithCallback() {
		List<Path> failpaths = new ArrayList<>();
		final Path path = new Path() {

			@Override
			public FileSystem getFileSystem() {
				throw new IllegalStateException();
			}

			@Override
			public boolean isAbsolute() {
				return false;
			}

			@Override
			public Path getRoot() {
				return null;
			}

			@Override
			public Path getFileName() {
				return null;
			}

			@Override
			public Path getParent() {
				return null;
			}

			@Override
			public int getNameCount() {
				return 0;
			}

			@Override
			public Path getName(int index) {
				return null;
			}

			@Override
			public Path subpath(int beginIndex, int endIndex) {
				return null;
			}

			@Override
			public boolean startsWith(Path other) {
				return false;
			}

			@Override
			public boolean startsWith(String other) {
				return false;
			}

			@Override
			public boolean endsWith(Path other) {
				return false;
			}

			@Override
			public boolean endsWith(String other) {
				return false;
			}

			@Override
			public Path normalize() {
				return null;
			}

			@Override
			public Path resolve(Path other) {
				return null;
			}

			@Override
			public Path resolve(String other) {
				return null;
			}

			@Override
			public Path resolveSibling(Path other) {
				return null;
			}

			@Override
			public Path resolveSibling(String other) {
				return null;
			}

			@Override
			public Path relativize(Path other) {
				return null;
			}

			@Override
			public URI toUri() {
				return null;
			}

			@Override
			public Path toAbsolutePath() {
				return null;
			}

			@Override
			public Path toRealPath(LinkOption... options) throws IOException {
				return null;
			}

			@Override
			public File toFile() {
				return null;
			}

			@Override
			public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
				return null;
			}

			@Override
			public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
				return null;
			}

			@Override
			public Iterator<Path> iterator() {
				return null;
			}

			@Override
			public int compareTo(Path other) {
				return 0;
			}
		};
		
		DuplicateContentFinderCallback callback = new FailingDuplicateContentFinderCallback() {

			@Override
			public void failedFile(Path failedPath) {
				failpaths.add(failedPath);
			}
		};
		
		final List<Path> input = new ArrayList<>();
		input.add(path);
		DuplicateContentFinder.getResult(input, callback);
		assertEquals(1, failpaths.size());
	}

	@Test
	public void scanEmptyInput() {
		final Queue<Path> input = new ConcurrentLinkedQueue<Path>();
		final Queue<List<Path>> output = DuplicateContentFinder.getResult(input);
		assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
		assertTrue("Es muss ein leeres Ergebnis zurück gegeben werden: " + output.size(), output.isEmpty());
	}

	@Test
	public void scanEmptyInputWithCallback() {
		final Queue<Path> input = new ConcurrentLinkedQueue<Path>();
		DuplicateContentFinder.getResult(input, FAILING_DCF_CALLBACK);
	}

	@Test
	public void scanSingleInput() {
		final Path path = Paths.get(PATH_FILE_1A);
		final List<Path> input = new ArrayList<>();
		input.add(path);
		final Queue<List<Path>> output = DuplicateContentFinder.getResult(input);
		assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
		assertTrue("Es muss ein leeres Ergebnis zurück gegeben werden", output.isEmpty());
	}

	@Test
	public void scanSingleDuplicateInput() {
		final Path path1 = Paths.get(PATH_FILE_1A);
		final Path path2 = Paths.get(PATH_FILE_1B);
		final Queue<Path> input = new ConcurrentLinkedQueue<Path>();
		input.add(path1);
		input.add(path2);
		final Queue<List<Path>> output = DuplicateContentFinder.getResult(input);
		assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
		assertEquals("Es muss ein Ergebnis mit zwei Dubletten zurück gegeben werden", 1, output.size());
		assertEquals("Es muss ein Ergebnis mit zwei Dubletten zurück gegeben werden", 2, output.peek().size());
		assertTrue("Es muss ein Ergebnis Test1.txt zurück gegeben werden",
				output.peek().get(0).endsWith("Test1.txt"));
	}

	@Test
	public void scanDoubleDuplicateInput() {
		final Path path1 = Paths.get(PATH_FILE_1A);
		final Path path2 = Paths.get(PATH_FILE_1B);
		final Path path3 = Paths.get(PATH_FILE_2A);
		final Path path4 = Paths.get(PATH_FILE_2B);
		final Queue<Path> input = new ConcurrentLinkedQueue<Path>();
		input.add(path1);
		input.add(path3);
		input.add(path4);
		input.add(path2);
		final Queue<List<Path>> output = DuplicateContentFinder.getResult(input);
		assertNotNull("Es muss ein Ergebnis zurück gegeben werden", output);
		assertEquals("Es müssen zwei Ergebnisse zurück gegeben werden", 2, output.size());
		List<Path> group1 = output.remove();
		List<Path> group2 = output.remove();
		assertEquals("Es müssen zwei Ergebnisse mit zwei Dubletten zurück gegeben werden", 2, group1.size());
		assertEquals("Es müssen zwei Ergebnisse mit zwei Dubletten zurück gegeben werden", 2, group2.size());
		assertTrue("Es muss ein Ergebnis Test1.txt zurück gegeben werden",
				group1.get(0).endsWith("Test1.txt") || group2.get(0).endsWith("Test1.txt"));
		assertTrue("Es muss ein Ergebnis Test2.txt zurück gegeben werden",
				group1.get(0).endsWith("Test2.txt") || group2.get(0).endsWith("Test2.txt"));
	}

	@Test
	public void scanDuplicateInputWithCallback() {
		final List<List<Path>> duplicateList = new ArrayList<>();
		final Path path1 = Paths.get(PATH_FILE_1A);
		final Path path2 = Paths.get(PATH_FILE_1B);
		final Queue<Path> input = new ConcurrentLinkedQueue<Path>();
		input.add(path1);
		input.add(path2);
		DuplicateContentFinderCallback callback = new FailingDuplicateContentFinderCallback() {

			@Override
			public void duplicateGroup(List<Path> duplicatePaths) {
				duplicateList.add(duplicatePaths);
			}
		};

		DuplicateContentFinder.getResult(input, callback);

		assertEquals(1, duplicateList.size());
		assertEquals(2, duplicateList.get(0).size());
		assertTrue("Es muss ein Ergebnis Test1.txt zurück gegeben werden",
				duplicateList.get(0).get(0).endsWith("Test1.txt"));
		assertTrue(duplicateList.get(0).contains(path1));
		assertTrue(duplicateList.get(0).contains(path2));
	}
}
