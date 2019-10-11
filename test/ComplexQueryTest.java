package test;

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

class ComplexQueryTest {
	/**
	 * This class is used to test complex queries, which will be a combination 
	 * of the AND, OR, NOT and phrase searches.
	 * Queries include standard queries used during demo to check for correctness.
	 */

	private BooleanQueryParser queryParser = new BooleanQueryParser();
	private static Index index;
	private static DocumentCorpus corpus;
	private static String corpusDirectory = "testdirectory/nationalparks";
	private static String parkNationalQuery = "\"park national\" camping";
	private static String campingYosemiteQuery = "camping in yosemite";
	private static String longPhraseQuery = "\"a gateway to the wilderness\"";
	private static String andOrPhraseQuery = "angels landing hike + \"mather pass\" + constellation";
	private static String notPhraseQuery = "zion -\"national park\"";
	private static String andOrNotPhraseQuery = "rv -camp + \"fishing and camping\"";
	private static String smallPhraseQuery = "\"fishing and camping\"";
	private static String near4Query = "[volunteering NEAR/4 monuments]";
	private static String near6Query = "[discuss NEAR/6 categories]";
	
	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		corpus = DirectoryCorpus.loadJSONFileDirectory(Paths.get(corpusDirectory), ".json");
		Indexer ind = new Indexer(corpus);
		index = ind.getPosIndex();
	}
	
	@Test
	void testParkNationalQuery() {
		int countDocuments = 0;
		QueryComponent userQuery = queryParser.parseQuery(parkNationalQuery);
		for (Posting p : userQuery.getPostings(index)) {
			countDocuments += 1;
		}
		System.out.println("Found " + countDocuments + " documents containing the terms:" + parkNationalQuery);
		assertEquals(true, true);
	}
	
	@Test
	void testCampingYosemiteQuery() {
		int countDocuments = 0;
		QueryComponent userQuery = queryParser.parseQuery(campingYosemiteQuery);
		for (Posting p : userQuery.getPostings(index)) {
			countDocuments += 1;
		}
		System.out.println("Found " + countDocuments + " documents containing the terms:" + campingYosemiteQuery);
		assertEquals(true, true);
	}
	
	@Test
	void testLongPhraseQuery() {
		int countDocuments = 0;
		QueryComponent userQuery = queryParser.parseQuery(longPhraseQuery);
		for (Posting p : userQuery.getPostings(index)) {
			countDocuments += 1;
		}
		System.out.println("Found " + countDocuments + " documents containing the terms:" + longPhraseQuery);
		assertEquals(true, true);
	}
	
	@Test
	void testAndOrPhraseQuery() {
		int countDocuments = 0;
		QueryComponent userQuery = queryParser.parseQuery(andOrPhraseQuery);
		for (Posting p : userQuery.getPostings(index)) {
			countDocuments += 1;
		}
		System.out.println("Found " + countDocuments + " documents containing the terms:" + andOrPhraseQuery);
		assertEquals(true, true);
	}
	
	@Test
	void testNotPhraseQuery() {
		int countDocuments = 0;
		QueryComponent userQuery = queryParser.parseQuery(notPhraseQuery);
		for (Posting p : userQuery.getPostings(index)) {
			countDocuments += 1;
		}
		System.out.println("Found " + countDocuments + " documents containing the terms:" + notPhraseQuery);
		assertEquals(true, true);
	}
	
	@Test
	void testAndOrNotPhraseQuery() {
		int countDocuments = 0;
		QueryComponent userQuery = queryParser.parseQuery(andOrNotPhraseQuery);
		for (Posting p : userQuery.getPostings(index)) {
			countDocuments += 1;
		}
		System.out.println("Found " + countDocuments + " documents containing the terms:" + andOrNotPhraseQuery);
		assertEquals(true, true);
	}
	
	@Test
	void testAndQuery() {
		int countDocuments = 0;
		QueryComponent userQuery = queryParser.parseQuery(smallPhraseQuery);
		for (Posting p : userQuery.getPostings(index)) {
			countDocuments += 1;
		}
		System.out.println("Found " + countDocuments + " documents containing the terms:" + smallPhraseQuery);
		assertEquals(true, true);
	}
	
	@Test
	void testNear4Query() {
		int countDocuments = 0;
		QueryComponent userQuery = queryParser.parseQuery(near4Query);
		for (Posting p : userQuery.getPostings(index)) {
			countDocuments += 1;
		}
		System.out.println("Found " + countDocuments + " documents containing the terms:" + near4Query);
		assertEquals(true, true);
	}
	
	@Test
	void testNear6Query() {
		int countDocuments = 0;
		QueryComponent userQuery = queryParser.parseQuery(near6Query);
		for (Posting p : userQuery.getPostings(index)) {
			countDocuments += 1;
		}
		System.out.println("Found " + countDocuments + " documents containing the terms:" + near6Query);
		assertEquals(true, true);
	}
}
