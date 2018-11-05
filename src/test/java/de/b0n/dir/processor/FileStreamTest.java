package de.b0n.dir.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by huluvu424242 on 06.01.17.
 */
public class FileStreamTest {

	private static final String PATH_VALID_FILE = "src/test/resources/Test1.txt";
	private static final String PATH_INVALID_FILE = "src/test/resources/Testxxx1.txt";

	private Path textFile = null;
	private FileReader fileStream = null;

	@Before
	public void setUp() {
		textFile = Paths.get(PATH_VALID_FILE);
		assertTrue(textFile.toFile().exists());

	}

	@After
	public void tearDown() {
		textFile = null;
		if (fileStream != null) {
			fileStream.clear();
			fileStream = null;
		}
	}

	@Test
	public void createInstanceFromValidFile() {
		final FileReader fileStream = new FileReader(textFile);
		assertNotNull(fileStream);
	}

	@Test
	public void createInstanceFromInvalidFile() {
		final Path invalidFile = Paths.get(PATH_INVALID_FILE);
		assertFalse(invalidFile.toFile().exists());
		final FileReader fileStream = new FileReader(invalidFile);
		assertEquals(FileReader.FAILING, fileStream.read());
	}

	@Test
	public void createInstanceFromNull() {
		try {
			new FileReader(null);
			fail();
		} catch (IllegalArgumentException ex) {
			assertNotNull(ex);
		}
	}

	@Test
	public void getValidStreamWithValidFile() throws IOException {
		final FileReader fileStream = new FileReader(this.textFile);
		assertNotNull(fileStream);

		for (int i = 0; i < 91; i++) {
			assertTrue(fileStream.read() > 0);
		}
		assertEquals(-1, fileStream.read());
	}

	@Test
	public void getValidFileWithValidFile() throws IOException {
		final FileReader stream = new FileReader(textFile);
		assertNotNull(stream);
		assertSame(textFile, stream.clear());
		assertEquals("wrong file size", 91, Files.size(stream.clear()));
	}

	@Test
	public void beNiceIfDoubleClose() {
		final FileReader fileStream = new FileReader(textFile);
		assertNotNull(fileStream);
		assertEquals('U', fileStream.read());
		fileStream.clear();
		assertEquals('U', fileStream.read());
		fileStream.clear();
		fileStream.clear();
	}
}
