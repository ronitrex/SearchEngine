import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import main.Indexer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import modules.documents.DirectoryCorpus;
import modules.documents.DocumentCorpus;
import modules.index.Index;
import modules.index.Posting;
import modules.query.BooleanQueryParser;
import modules.query.QueryComponent;


class NotQueryTest {
	
	private BooleanQueryParser queryParser = new BooleanQueryParser();
	private static Index index;
	private static DocumentCorpus corpus;
	private static String notQueryPositive = "zack -ron";
	private static String notQueryNegative = "ron -sean";

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		corpus = DirectoryCorpus.loadJSONFileDirectory(Paths.get("testdirectory/dummyJSON"), ".json");
		Indexer ind = new Indexer(corpus);
		index = ind.getPosIndex();
	}
	
	@Test
	void testNotQueryPositive() {
		// search for documents that contain zack but not ron
		QueryComponent userQuery = queryParser.parseQuery(notQueryPositive);
		String expectedResult = "Zack";
		String result = "";
		for (Posting p : userQuery.getPostings(index)) {
			result = corpus.getDocument(p.getDocumentId()).getTitle();
			System.out.println("\nResult Document ID:" + corpus.getDocument(p.getDocumentId()).getTitle());
		}
		// zack & ron appear together, hence this should pass
		assertEquals(expectedResult, result);
	}
	
	@Test
	void testNotQueryNegative() {
		// search for documents that contain ronit but not sean
		QueryComponent userQuery = queryParser.parseQuery(notQueryNegative);
		List<String> expectedResults = new ArrayList<>();
		List<String> results = new ArrayList<>();
		expectedResults.add("ZackRon");
		expectedResults.add("Ron");
		for (Posting p : userQuery.getPostings(index)) {
			results.add(corpus.getDocument(p.getDocumentId()).getTitle());
			System.out.println("Result Document ID:" + corpus.getDocument(p.getDocumentId()).getTitle());
		}
		assertEquals(expectedResults, results);
	}
}
