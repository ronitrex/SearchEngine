

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

class OrQueryTest {

	private BooleanQueryParser queryParser = new BooleanQueryParser();
	private static Index index;
	private static DocumentCorpus corpus;
	private static String orQueryPositive = "zack + ron";
	private static String orQueryNegative = "sean + john";

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		corpus = DirectoryCorpus.loadJSONFileDirectory(Paths.get("testdirectory/dummyJSON"), ".json");
		Indexer ind = new Indexer(corpus);
		index = ind.getPositionalInvertedIndex();
	}
	
	@Test
	void testOrQueryPositive() {
		// search for documents that contain zack or ron
		QueryComponent userQuery = queryParser.parseQuery(orQueryPositive);
		List<String> expectedResults = new ArrayList<>();
		List<String> results = new ArrayList<>();
		expectedResults.add("ZackRon");
		expectedResults.add("Zack");
		expectedResults.add("Ron");
		for (Posting p : userQuery.getPostings(index)) {
			results.add(corpus.getDocument(p.getDocumentId()).getTitle());
			System.out.println("Result Document ID:" + corpus.getDocument(p.getDocumentId()).getTitle());
		}
		assertEquals(expectedResults, results);
	}
	
	@Test
	void testOrQueryNegative() {
		// search for documents that contain john or sean
		QueryComponent userQuery = queryParser.parseQuery(orQueryNegative);
		int expectedResult = 0; //No documents contain both terms together
		int i = 0;

		for (Posting p : userQuery.getPostings(index)) {
			i += 1;
			System.out.println("\nResult Document ID:" + corpus.getDocument(p.getDocumentId()).getTitle());
		}
		assertEquals(expectedResult, i);
	}
}
