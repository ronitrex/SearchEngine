package modules.query;

import modules.index.Index;
import modules.index.Posting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

/**
 * A NotQuery composes other QueryComponents and returns postings that don't
 * contain all of the terms mentioned by the user.
 */
public class OrQuery implements QueryComponent {
	// The components of the Or query.
	private List<QueryComponent> mComponents;

	public OrQuery(List<QueryComponent> components) {
		mComponents = components;
	}

	/**
	 * Empty constructor to create objects of type AndQuery
	 */
	public OrQuery() {
	}

	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = null;

		// program the merge for an OrQuery, by gathering the postings of the
		// composed QueryComponents and
		// unioning the resulting postings.
		if (mComponents != null) {
			result = unionPostings(mComponents, index);
		}
		if (result != null)
			return result;
		else
			return Collections.EMPTY_LIST;
	}

	/**
	 * Returns the NOT Postings recursively
	 * 
	 * @param mQueryComponents - subquery components
	 * @param index            - the indexed vocabulary
	 * @return All postings that contain the term/terms
	 */
	public List<Posting> unionPostings(List<QueryComponent> mQueryComponents, Index index) {
		List<Posting> listOfPostings = new ArrayList<Posting>();
		List<QueryComponent> orComponents = new ArrayList<QueryComponent>();
		List<Posting> resultPostings = new ArrayList<Posting>();
		for (QueryComponent qc : mQueryComponents) {
			listOfPostings = qc.getPostings(index);
			if (!CollectionUtils.isEmpty(listOfPostings)) {
				orComponents.add(qc);
			}
		}
		if (!CollectionUtils.isEmpty(orComponents)) {
			resultPostings = orComponents.get(0).getPostings(index);
			for (int i = 1; i < orComponents.size(); i++) {
				resultPostings = unionPosting(resultPostings, orComponents.get(i).getPostings(index));
			}
			return resultPostings;
		} else {
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * Returns the union of two postings
	 * 
	 * @param p1 List of postings for term1
	 * @param p2 List of postings for term2
	 * @return List of postings which is the union of p1 and p2
	 */
	public List<Posting> unionPosting(List<Posting> p1, List<Posting> p2) {
		List<Posting> unionResult = new ArrayList<Posting>();
		if (p1 != null && p2 != null) {
			int i = 0, j = 0;
			for (; i < p1.size() && j < p2.size();) {
				if (p1.get(i).getDocumentId() > p2.get(j).getDocumentId()) {
					unionResult.add(p2.get(j));
					j += 1;
				} else if (p1.get(i).getDocumentId() == p2.get(j).getDocumentId()) {
					unionResult.add(p1.get(i));
					i += 1;
					j += 1;
				} else {
					unionResult.add(p1.get(i));
					i += 1;
				}
			}
			while (i < p1.size()) { // At-most one of the while loop is executed. For adding
				unionResult.add(p1.get(i)); // the remaining postings.
				i += 1;
			}
			while (j < p2.size()) {
				unionResult.add(p2.get(j));
				j += 1;
			}
		}
		return unionResult;
	}

	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" + String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
				+ " )";
	}
}
