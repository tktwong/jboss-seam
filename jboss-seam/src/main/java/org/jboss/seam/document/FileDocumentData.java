package org.jboss.seam.document;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import org.jboss.seam.util.Resources;

/**
 * Stores DocumentData in a file, delete at end when DocumentStore is destroyed.
 */
public class FileDocumentData extends DocumentData {

	private File data;

	public FileDocumentData(String baseName, DocumentType documentType, File data) {
		super(baseName, documentType);
		this.data = data;
	}

	@Override
	public void writeDataToStream(OutputStream stream) throws IOException {
		InputStream is = null;
		try {
			
			is = new BufferedInputStream(Files.newInputStream(data.toPath()));
			byte[] buffer = new byte[4096];
			int read = 0;
			while ((read = is.read(buffer)) > 0) {
				stream.write(buffer, 0, read);
			}
		} finally {
			Resources.close(is);
		}
	}

	public File getData() {
		return this.data;
	}

}
