import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.List;

import main.Indexer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import modules.documents.DirectoryCorpus;
import modules.documents.Document;
import modules.documents.DocumentCorpus;
import modules.index.Index;
import modules.index.PositionalInvertedIndex;
import modules.index.Posting;
import modules.query.BooleanQueryParser;
import modules.query.QueryComponent;
import modules.text.AdvancedTokenProcessor;
import modules.text.EnglishTokenStream;
import modules.text.TokenProcessor;

class NearKTest {

	private BooleanQueryParser queryParser = new BooleanQueryParser();
	private static Index index;
	private static DocumentCorpus corpus;
	private static String nearKPositive = "[ron NEAR/4 balance]";
	private static String nearKNegative = "[balance NEAR/4 ron]";

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		corpus = DirectoryCorpus.loadJSONFileDirectory(Paths.get("testdirectory/dummyJSON"), ".json");
		Indexer ind = new Indexer(corpus);
		index = ind.getPosIndex();
	}

	@Test
	void testNearKPositive() {
		// search for documents that contain ron "near" balance

		QueryComponent userQuery = queryParser.parseQuery(nearKPositive);
		String expectedResult = "ZackRon";
		String result = "";
		for (Posting p : userQuery.getPostings(index)) {
			result = corpus.getDocument(p.getDocumentId()).getTitle();
			System.out.println("\nResult Document ID:" + corpus.getDocument(p.getDocumentId()).getTitle());
		}
		//ron "near" balance should pass
		assertEquals(expectedResult, result);

	}

	@Test
	void testNearKNegative() {
		// search for documents that contain balance "near" ron

		QueryComponent userQuery = queryParser.parseQuery(nearKNegative);
		int i = 0;
		try {
			for (Posting p : userQuery.getPostings(index)) {
				++i;
				System.out.println("\nResult Document/s ID:" + corpus.getDocument(p.getDocumentId()).getTitle());
			}

		} catch (Exception e) {
		}
		assertEquals(0, i);
	}
}
