package modules.documents;

import java.nio.file.Path;

public interface FileDocumentFactory {
	FileDocument createFileDocument(Path absoluteFilePath, int documentId);
}
