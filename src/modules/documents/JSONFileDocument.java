package modules.documents;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;

import com.google.gson.Gson;

/**
 * Represents a document that is saved as a simple JSON file in the local file system.
 */

public class JSONFileDocument implements FileDocument{
	private int mDocumentId;
	private Path mFilePath;
	private JsonDoc mJsonDoc;

	/**
	 * Constructs a JSONFileDocument with the given document ID and the json content representing the file at the given
	 * absolute file path.
	 */
	public JSONFileDocument(int id, Path absoluteFilePath) {
		mDocumentId = id;
		mFilePath = absoluteFilePath;
		mJsonDoc = getJsonContent();
	}
	@Override
	public int getId() {
		return mDocumentId;
	}

	@Override
	public Reader getContent() {
		try {
			String body = mJsonDoc.getBody();
			BufferedReader reader = new BufferedReader(new StringReader(body));
			return reader;

		}catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getTitle() {
		return mJsonDoc.getTitle();
	}

	@Override
	public Path getFilePath() {
		return mFilePath;
	}
	
	/**
	 * Maps the content in json file to a JsonDoc object using gson 
	 * @return The content in the Json File.
	 */
	public JsonDoc getJsonContent() {
		//Create a new Gson object
        Gson gson = new Gson();
        BufferedReader br = null;
        try {
			br = new BufferedReader(new FileReader(mFilePath.toString()));

		} catch (Exception e) {
			e.printStackTrace();
		}
        //convert the json to Java object
        JsonDoc jsonContent = gson.fromJson(br, JsonDoc.class);
        try{
        	br.close();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return jsonContent;
	}
	
	public static FileDocument loadJSONFileDocument(Path absolutePath, int documentId) {
		return new JSONFileDocument(documentId, absolutePath);
	}
}
