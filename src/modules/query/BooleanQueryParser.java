package modules.query;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import modules.text.AdvancedTokenProcessor;
import modules.text.EnglishTokenStream;
import modules.text.TokenProcessor;

/**
 * Parses boolean queries. Does not handle phrase queries, NOT queries, NEAR queries, or
 * wildcard queries... yet.
 */
public class BooleanQueryParser {
	/**
	 * Identifies a portion of a string with a starting index and a length.
	 */
	private static class StringBounds {
		int start;
		int length;

		StringBounds(int start, int length) {
			this.start = start;
			this.length = length;
		}
	}

	/**
	 * Encapsulates a QueryComponent and the StringBounds that led to its parsing.
	 */
	private static class Literal {
		StringBounds bounds;
		QueryComponent literalComponent;
		boolean isPositive = true;

		Literal(StringBounds bounds, QueryComponent literalComponent, boolean isPositive) {
			this.bounds = bounds;
			this.literalComponent = literalComponent;
			this.isPositive = isPositive;
		}
	}

	/**
	 * Given a boolean query, parses and returns a tree of QueryComponents
	 * representing the query.
	 */
	public QueryComponent parseQuery(String query) {
		int start = 0;

		// General routine: scan the query to identify a literal, and put that literal
		// into a list.
		// Repeat until a + or the end of the query is encountered; build an AND query
		// with each
		// of the literals found. Repeat the scan-and-build-AND-query phase for each
		// segment of the
		// query separated by + signs. In the end, build a single OR query that composes
		// all of the built
		// AND subqueries.

		List<QueryComponent> allSubqueries = new ArrayList<>();
		List<QueryComponent> notSubQueries = new ArrayList<>();
		do {
			// Identify the next subquery: a portion of the query up to the next + sign.
			StringBounds nextSubquery = findNextSubquery(query, start);
			// Extract the identified subquery into its own string.
			String subquery = query.substring(nextSubquery.start, nextSubquery.start + nextSubquery.length);
			int subStart = 0;

			// Store all the individual components of this subquery.
			List<QueryComponent> subqueryLiterals = new ArrayList<>(0);
			List<QueryComponent> notQueryLiterals = new ArrayList<>(0);

			do {
				// Extract the next literal from the subquery.
				Literal lit = findNextLiteral(subquery, subStart);

				if (lit.isPositive == false) {
					// if literal starts with '-' it is a negative component, add it to list of
					// notQueryLiterals
					notQueryLiterals.add(lit.literalComponent);
				} else {
					// Add the literal component to the conjunctive list.
					subqueryLiterals.add(lit.literalComponent);
				}

				// Set the next index to start searching for a literal.
				subStart = lit.bounds.start + lit.bounds.length;

			} while (subStart < subquery.length());

			// if notQueryLiterals list is not empty, then add it to list of notSubQueries
			if (!notQueryLiterals.isEmpty())
				notSubQueries.add(new NotQuery(notQueryLiterals));
			// After processing all literals, we are left with a conjunctive list
			// of query components, and must fold that list into the final disjunctive list
			// of components.

			// If there was only one literal in the subquery, we don't need to AND it with
			// anything --
			// its component can go straight into the list.
			if (subqueryLiterals.size() == 1 && notQueryLiterals.isEmpty()) { // single term and no NOT terms
				allSubqueries.add(subqueryLiterals.get(0));
			} else if (subqueryLiterals.size() >= 1 && !notQueryLiterals.isEmpty()) {
				subqueryLiterals.addAll(notSubQueries);
				notSubQueries.clear();
				allSubqueries.add(new AndQuery(subqueryLiterals));
			} else {
				// With more than one literal, we must wrap them in an AndQuery component.
				allSubqueries.add(new AndQuery(subqueryLiterals));
			}
			start = nextSubquery.start + nextSubquery.length;
		} while (start < query.length());

		// After processing all subqueries, we either have a single component or
		// multiple components
		// that must be combined with an OrQuery.
		if (allSubqueries.size() == 1) {
			return allSubqueries.get(0);
		} else if (allSubqueries.size() > 1) {
			return new OrQuery(allSubqueries);
		} else {
			return null;
		}
	}

	/**
	 * Locates the start index and length of the next subquery in the given query
	 * string, starting at the given index.
	 */
	private StringBounds findNextSubquery(String query, int startIndex) {
		int lengthOut;

		// Find the start of the next subquery by skipping spaces and + signs.
		char test = query.charAt(startIndex);
		while (test == ' ' || test == '+') {
			test = query.charAt(++startIndex);
		}

		// Find the end of the next subquery.
		int nextPlus = query.indexOf('+', startIndex + 1);

		if (nextPlus < 0) {
			// If there is no other + sign, then this is the final subquery in the
			// query string.
			lengthOut = query.length() - startIndex;
		} else {
			// If there is another + sign, then the length of this subquery goes up
			// to the next + sign.

			// Move nextPlus backwards until finding a non-space non-plus character.
			test = query.charAt(nextPlus);
			while (test == ' ' || test == '+') {
				test = query.charAt(--nextPlus);
			}

			lengthOut = 1 + nextPlus - startIndex;
		}

		// startIndex and lengthOut give the bounds of the subquery.
		return new StringBounds(startIndex, lengthOut);
	}

	/**
	 * Locates and returns the next literal from the given subquery string.
	 */
	private Literal findNextLiteral(String subquery, int startIndex) {
		int subLength = subquery.length();
		int lengthOut;

		// Skip past white space.
		while (subquery.charAt(startIndex) == ' ') {
			++startIndex;
		}

		if ((subquery.charAt(startIndex) != '"') && (subquery.charAt(startIndex) != '[')) {
			// if subquery doesn't contain quotation marks or square brackets then do term
			// literal for single terms
			// Locate the next space to find the end of this literal.
			int nextSpace = subquery.indexOf(' ', startIndex);
			if (nextSpace < 0) {
				// No more literals in this subquery.
				lengthOut = subLength - startIndex;
			} else {
				lengthOut = nextSpace - startIndex;
			}

			if (subquery.charAt(startIndex) == '-') {
				++startIndex;
				Literal negativeLiteral = findNextLiteral(subquery, startIndex);
				negativeLiteral.isPositive = false;
				return negativeLiteral;
			}
			String TermLit = subquery.substring(startIndex, startIndex + lengthOut);
			TokenProcessor TermProcessor = new AdvancedTokenProcessor();

			EnglishTokenStream TermLits = new EnglishTokenStream(new StringReader(TermLit));

			Iterable<String> TermTokens = TermLits.getTokens();
			List<String> ProcessedTermLit = new ArrayList<>();

			for (String tokens : TermTokens) {
				List<String> processedTokens = TermProcessor.processToken(tokens);
				for (String processedToken : processedTokens) {
					ProcessedTermLit.add(processedToken);
				}
			}

			// This is a term literal containing a single term.
			return new Literal(new StringBounds(startIndex, lengthOut), new TermLiteral(ProcessedTermLit.get(0)), true);
		}

		else {
			String PhraseLit;
			int nearK;
			int nearKIndex = 0;
			if (subquery.charAt(startIndex) == '[') {
				// if subquery starts with '[', then treat it as phrase literal and create a
				// phrase literal and parse the query to get NEAR /K value.
				++startIndex;
				int nearTermEnd = subquery.indexOf(']', startIndex);
				nearK = subquery.indexOf('/', startIndex) + 1;
				nearKIndex = nearK;
				nearK = Character.getNumericValue(subquery.charAt(nearK));
				String term1 = subquery.substring(startIndex, nearKIndex - 6);
				String term2 = subquery.substring(nearKIndex + 2, nearTermEnd);
				lengthOut = nearTermEnd - startIndex;
				PhraseLit = term1 + " " + term2;
			} else {
				// if subquery contains quotation marks, then treat it as phrase literal and
				// create a phrase literal.
				++startIndex;
				int phraseEnd = subquery.indexOf('"', startIndex);
				lengthOut = phraseEnd - startIndex;
				PhraseLit = subquery.substring(startIndex, startIndex + lengthOut);
				nearK = 1;
			}

			TokenProcessor PhraseProcessor = new AdvancedTokenProcessor();
			EnglishTokenStream PhraseLits = new EnglishTokenStream(new StringReader(PhraseLit));

			Iterable<String> PhraseTokens = PhraseLits.getTokens();
			List<String> ProcessedPhraseLit = new ArrayList<>();

			for (String tokens : PhraseTokens) {
				List<String> processedTokens = PhraseProcessor.processToken(tokens);
				for (String processedToken : processedTokens) {
					ProcessedPhraseLit.add(processedToken);
				}
			}

			// If it's a single term phrase literal, simply return a term literal for it
			if (!PhraseLit.contains(" ")) {
				PhraseLit = PhraseLit + " " + PhraseLit;
				return new Literal(new StringBounds(startIndex, lengthOut + 1), new PhraseLiteral(PhraseLit, 0), true);
			}
			return new Literal(new StringBounds(startIndex, lengthOut + 1),
					new PhraseLiteral(ProcessedPhraseLit, nearK), true);
		}
		/*
		 * Instead of assuming that we only have single-term literals, modify this
		 * method so it will create a PhraseLiteral object if the first non-space
		 * character you find is a double-quote ("). In this case, the literal is not
		 * ended by the next space character, but by the next double-quote character.
		 */
	}
}
