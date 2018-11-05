package de.b0n.dir.processor;

import static org.junit.Assert.fail;

import java.nio.file.Path;
import java.util.List;

public class FailingDuplicateContentFinderCallback implements DuplicateContentFinderCallback {

	@Override
	public void uniqueFile(Path uniqueFile) {
		fail();
	}

	@Override
	public void failedFile(Path failedFile) {
		fail();
	}

	@Override
	public void duplicateGroup(List<Path> queue) {
		fail();
	}
}
