package de.b0n.dir.processor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FileStreamErrorTest {
	private final Path path = Paths.get("");

	@Mock
	private BufferedInputStream stream;

	@InjectMocks
	private FileReader fileStream = new FileReader(path);
	
	@Test
	public void validRead() throws IOException {
		assertNotNull(fileStream);
		when(stream.read()).thenReturn(66);
		
		assertEquals(66, fileStream.read());
		
		verify(stream).read();
		verifyNoMoreInteractions(stream);
	}

	@Test
	public void failedRead() throws IOException {
		when(stream.read()).thenThrow(new IOException("No Message"));
		
		try {
			fileStream.read();
		} catch (IllegalStateException e) {
			assertEquals("Stream of " + path.toString() + " could not be read: No Message", e.getMessage());
		}

		verify(stream).read();
		verify(stream).close();
		verifyNoMoreInteractions(stream);
	}
	
	@Test
	public void validClose() throws IOException {
		assertNotNull(fileStream);
		
		fileStream.clear();
		
		verify(stream).close();
		verifyNoMoreInteractions(stream);
	}

	@Test
	public void failedClose() throws IOException {
		doThrow(new IOException("No Message")).when(stream).close();
		
		try {
			fileStream.clear();
		} catch (IllegalStateException e) {
			assertEquals("Could not close Stream. Nothing to do about that, resetting FileStream.", e.getMessage());
		}

		verify(stream).close();
		verifyNoMoreInteractions(stream);
	}
}
