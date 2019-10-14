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


class AndQueryTest {
	private BooleanQueryParser queryParser = new BooleanQueryParser();
	private static Index index;
	private static DocumentCorpus corpus;
	private static String andQueryPositive = "zack ron";
	private static String andQueryNegative = "sean ron";

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		corpus = DirectoryCorpus.loadJSONFileDirectory(Paths.get("testdirectory/dummyJSON"), ".json");
		Indexer ind = new Indexer(corpus);
		index = ind.getPositionalInvertedIndex();	}

	@Test
	void testAndQueryPositive() {
		// search for documents that contain zack & ron
		QueryComponent userQuery = queryParser.parseQuery(andQueryPositive);
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
	void testAndQueryNegative() {
		// search for documents that contain sean & ron
		QueryComponent userQuery = queryParser.parseQuery(andQueryNegative);
		int expectedResult = 0; //No documents contain both terms together
		int i = 0;

		for (Posting p : userQuery.getPostings(index)) {
			i += 1;
			System.out.println("\nResult Document ID:" + corpus.getDocument(p.getDocumentId()).getTitle());
		}
		// sean and ronit don't appear in any of the documents, hence this should fail
		assertEquals(expectedResult, i);
	}
}
