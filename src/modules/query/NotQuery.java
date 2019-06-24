package modules.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import modules.index.Index;
import modules.index.Posting;

public class NotQuery implements QueryComponent {
	// The NOT query component.
	// NOT Query components are negative by default.
	private List<QueryComponent> mComponents;
	private boolean isComponentNegative = true;

	public NotQuery(List<QueryComponent> components) {
		mComponents = components;
	}

	/**
	 * Empty constructor to create objects of type NOTQuery
	 */
	public NotQuery() {
	}

	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = null;

		// program the merge for a NotQuery, by gathering the postings of the
		// composed QueryComponents and
		// returning the postings that don't contain the resulting postings.
		if (mComponents != null) {
			result = getNotPostings(mComponents, index);
		}
		if (result != null)
			return result;
		else
			return Collections.EMPTY_LIST;
	}

	/**
	 * Returns the NOT Postings recursively
	 * 
	 * This method returns the NOT result of a query that contains NOT components.
	 * 
	 * @param mQueryComponents Query components that need to be used to NOT to get
	 *                         the result
	 * @param index            The Positional inverted index used to refer to
	 *                         vocabulary
	 * @return Returns the list of postings of the query components.
	 */
	public List<Posting> getNotPostings(List<QueryComponent> mQueryComponents, Index index) {
		List<Posting> postingResult = mQueryComponents.get(0).getPostings(index);
		try {
			List<QueryComponent> restComponents = mQueryComponents.subList(1, mQueryComponents.size());
			while (!CollectionUtils.isEmpty(postingResult) && !CollectionUtils.isEmpty(restComponents)) {
				OrQuery orQuery = new OrQuery();
				postingResult = orQuery.unionPosting(postingResult, restComponents.get(0).getPostings(index));
				restComponents = restComponents.subList(1, mQueryComponents.size());
			}
		} catch (Exception e) {
			return postingResult;
		}
		return postingResult;
	}

	/**
	 * Given two postings p1 and p2, does the AND NOT merge between them
	 * 
	 * @param p1 List of postings for term1
	 * @param p2 List of postings for term2
	 * @return Returns list of postings that contain term1 but NOT term2
	 */
	public List<Posting> notPosting(List<Posting> p1, List<Posting> p2) {
		List<Posting> notResult = new ArrayList<Posting>();
		if (p1 != null && p2 != null) {
			int i = 0, j = 0;
			for (; i < p1.size() && j < p2.size();) {
				if (p2.get(j).getDocumentId() > p1.get(i).getDocumentId()) {
					notResult.add(p1.get(i));
					i += 1;
				} else if (p2.get(j).getDocumentId() == p1.get(i).getDocumentId()) {
					i += 1;
					j += 1;
				} else {
					j += 1;
				}
			}

			while (i < p1.size()) {
				notResult.add(p1.get(i));
				i += 1;
			}
		}
		return notResult;
	}

	@Override
	public String toString() {
		return String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
