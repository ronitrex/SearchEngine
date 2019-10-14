import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;

import main.Indexer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import modules.documents.DirectoryCorpus;
import modules.documents.DocumentCorpus;
import modules.index.Index;
import modules.index.Posting;
import modules.query.BooleanQueryParser;
import modules.query.QueryComponent;

class PhraseLiteralTest {
	private static String phraseQueryPositive = "\"zack and ron\"";
	private static String phraseQueryNegative = "\"are partner\"";
	private BooleanQueryParser queryParser = new BooleanQueryParser();
	private static Index index;
	private static DocumentCorpus corpus;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		corpus = DirectoryCorpus.loadJSONFileDirectory(Paths.get("testdirectory/dummyJSON"), ".json");
		Indexer ind = new Indexer(corpus);
		index = ind.getPositionalInvertedIndex();
	}

	@Test
	void testPhraseQueryPositive() {
		// search for documents that contain phrase defined in phraseQueryPositive

		QueryComponent userQuery = queryParser.parseQuery(phraseQueryPositive);
		String expectedResult = "ZackRon";
		String result = "";
		for (Posting p : userQuery.getPostings(index)) {
			result = corpus.getDocument(p.getDocumentId()).getTitle();
			System.out.println("\nResult Document ID:" + corpus.getDocument(p.getDocumentId()).getTitle());
		}
		// zack & ron appear together, hence this should pass
		assertEquals(expectedResult, result);
	}

	@Test
	void testPhraseQueryNegative() {
		// search for documents that contain phrase defined in phraseQueryNegative
		QueryComponent userQuery = queryParser.parseQuery(phraseQueryNegative);
		int expectedResult = 0; //No documents contain both terms together
		int i = 0;

		for (Posting p : userQuery.getPostings(index)) {
			i += 1;
			System.out.println("\nResult Document ID:" + corpus.getDocument(p.getDocumentId()).getTitle());
		}
		assertEquals(expectedResult, i);
	}
}
