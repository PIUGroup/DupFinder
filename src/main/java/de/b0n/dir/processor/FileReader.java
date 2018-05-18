package de.b0n.dir.processor;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Kapselt ein File und stellt darauf eine read()-Operation zur Verfügung.
 * Dient zum effizienten Teilen der Files bei unterschieden in den Streams und gleichzeitigem Halten des Stream-Zustands.
 * Die Datei wird erstmalig beim ersten Zugriff auf dessen Inhalt geöffnet.
 */
public class FileReader {
	/**
	 * Datei wurde zuende gelesen
	 */
	public static final int FINISHED = -1;
	/**
	 * Beim Lesen der Datei ist ein Fehler aufgetreten
	 */
	public static final int FAILING = -2;

	private final File file;
	private BufferedInputStream stream;

	/**
	 * Packt die Collection von Dateien in jeweils in einen FileStream, zusammengefasst in einer Queue.
	 * @param files In FileStreams zu kapselnde Files
	 * @return Queue mit FileStreams
	 */
	public static Queue<FileReader> pack(Collection<File> files) {
		Queue<FileReader> fileStreams = new ConcurrentLinkedQueue<FileReader>();
		for (File file : files) {
			fileStreams.add(new FileReader(file));
		}
		return fileStreams;
	}

	public static Queue<File> extract(Collection<FileReader> fileStreams) {
		Queue<File> filesQueue = new ConcurrentLinkedQueue<File>();
		for (FileReader fileStream : fileStreams) {
			filesQueue.add(fileStream.clear());
		}
		return filesQueue;
	}

	/**
	 * Schließt alle Streams der im FileStream hinterlegten Dateien.
	 * @param fileStreams zu schließende FileStreams
	 */
	public static void closeAll(Collection<FileReader> fileStreams) {
		for (FileReader fileStream : fileStreams) {
			fileStream.close();
		}
	}

	/**
	 * Erzeugt das Objekt.
	 * Der Stream zum Auslesen wird lazy erst bei Bedarf geöffnet.
	 * @param file 
	 */
	public FileReader(File file) {
		if (file == null) {
			throw new IllegalArgumentException("File may not be null.");
		}
		this.file = file;
	}

	/**
	 * Schließt den Stream im dem Adapter und liefert das File.
	 * @return zum Stream-Initialisieren genutzes File
	 */
	public File clear() {
		close();
		return file;
	}
	
	/**
	 * Schließt den Dateistream nach der Analyse oder im Fehlerfall.
	 */
	private void close() {
		if (stream == null) {
			return;
		}

		try {
			stream.close();
		} catch (IOException e) {
			//Could not close Stream. Nothing to do about that, resetting FileStream
		} finally {
			stream = null;
		}
	}

	/**
	 * Liefert ein Byte aus dem geöffneten Dateistream zur Inhaltsanalyse.
	 * @see InputStream.read()
	 * @return Wert gemäß InputStream.read()
	 */
	public int read() {
		int data = FAILING;
		try {
			if (stream == null) {
				stream = new BufferedInputStream(new FileInputStream(file));
			}
			data = stream.read();
		} catch (IOException e) {
			close();
		}
		return data;
	}
}