package modules.query;

import modules.index.Index;
import modules.index.Posting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

/**
 * Represents a phrase literal consisting of one or more terms that must occur
 * in sequence.
 */
public class PhraseLiteral implements QueryComponent {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();

	// The kth position between two terms, k = 1 for phrase literals, k = n for NEAR
	// /k literals where n is entered by user
	private int k;

	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms, int k2) {
		mTerms.addAll(terms);
		k = k2;
	}

	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms
	 * separated by spaces.
	 */
	public PhraseLiteral(String terms, int k2) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
		k = k2;
	}

	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms
	 * separated by spaces.
	 */
	public PhraseLiteral(String terms) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
	}

	@Override
	public List<Posting> getPostings(Index index) {
		// Retrieve the postings for the individual terms the phrase and positional merge them together.
		List<Posting> result = null;

		if (mTerms != null) {
			try {
				result = intersectPostings(mTerms, index);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * The intersectPostings finds a list of postings that are common to two given terms and iterates over the
	 * length of the phrase to find any point where there are no common postings, Finally it gives all the postings
	 * where the whole phrase is present.
	 * @param Terms 	string term or words in the phrase
	 * @param index		index to query to get the postings
	 * @return			list of posting where the whole phrase or near k operator exists
	 */
	public List<Posting> intersectPostings(List<String> Terms, Index index) {

		List<Posting> common = new ArrayList<>();
		int i = mTerms.size() - 1;
		while (i > 0) {

			String term1 = Terms.get(i);
			String term2 = Terms.get(--i);
			List<Posting> postingResult1 = index.getPostings(term1);
			List<Posting> postingResult2 = index.getPostings(term2);

			if (CollectionUtils.isEmpty(postingResult1) || CollectionUtils.isEmpty(postingResult2)) {
				return Collections.EMPTY_LIST;
			} else {
				if (CollectionUtils.isEmpty(common))
					common = positionalIntersect(postingResult1, postingResult2, k);
				else
					common = positionalIntersect(common, postingResult2, k);
				if (common.isEmpty()) {
					break;
				}
			}
		}
		return common;
	}

	/**
	 * positionalIntersect checks how close to each other two terms in a given phrase are.
	 * This method checks for both phrases and near K operators
	 * @param term1 		The last term in a given phrase or remaining phrase
	 * @param term2			The second last term in a given phrase or remaining phrase
	 * @param k				The required distance between term1 and term2
	 * @return				Positions where Term2..[k]..Term1 holds true for a given phrase or near K operator
	 */
	public List<Posting> positionalIntersect(List<Posting> term1, List<Posting> term2, int k) {
		List<Posting> postingResult = new ArrayList<Posting>();
		int i = 0, j = 0;
		for (; i < term1.size() && j < term2.size();) {
			if (term1.get(i).getDocumentId() == term2.get(j).getDocumentId()) {
				List<Integer> pp1 = term1.get(i).getPositionsInDoc();
				List<Integer> pp2 = term2.get(j).getPositionsInDoc();
				List<Integer> phrasePosition = new ArrayList<Integer>();
				for (Integer positions1 : pp1) {
					for (Integer positions2 : pp2) {

						/**
						 * Term 1 and term 2 are moving from the end of the phrase to the beginning of the phrase.
						 * Therefore, their arrangement is Term2.. [k].. Term1 in any given phrase.
						 */

						int relative_position = positions1 - positions2;
						if ((relative_position <= k) && (relative_position >= 0)) {
							phrasePosition.add(positions2);
						} else if ((positions1 - positions2) > k) {
							continue;
						}
					}
				}
				if (phrasePosition.isEmpty()) {
					i += 1;
					j += 1;
					continue;
				} else {
					Posting ans = new Posting(term1.get(i).getDocumentId(), phrasePosition, 0);
					postingResult.add(ans);
					i += 1;
					j += 1;
					continue;
				}
			} else if (term1.get(i).getDocumentId() < term2.get(j).getDocumentId()) {
				i += 1;
			} else {
				j += 1;
			}
		}
		return postingResult;
	}

	@Override
	public String toString() {
		return "\"" + String.join(" ", mTerms) + "\"";
	}
}