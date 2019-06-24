package modules.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import modules.index.Index;
import modules.index.Posting;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an
 * intersection-like operation.
 */
public class AndQuery implements QueryComponent {
	private List<QueryComponent> mComponents;

	public AndQuery(List<QueryComponent> components) {
		mComponents = components;
	}

	/**
	 * Empty constructor to create objects of type AndQuery
	 */
	public AndQuery() {
	}

	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> positiveResult = null;
		List<Posting> notResult = null;
		List<Posting> result = null;
		// program the merge for an AndQuery, by gathering the postings of the
		// composed QueryComponents and
		// intersecting the resulting postings.

		if (mComponents != null) {
			List<QueryComponent> notQueryComponents = new ArrayList<>();
			List<QueryComponent> positiveComponents = new ArrayList<>();
			for (QueryComponent qc : mComponents) {
				try {
					NotQuery notQuery = (NotQuery) qc;// if casting succeeds then we have NOT component in list
					notQueryComponents.add(notQuery);
				} catch (Exception e) {
					positiveComponents.add(qc);
				} // after loop, notQueryComponents will contain all negative components.
			}
			positiveResult = intersectPostings(positiveComponents, index);
			
			// if notQueryComponents list is not empty, then do AND NOT
			// Get OR of all not components and then do AND NOT with remaining AND component
			if (!notQueryComponents.isEmpty()) {
				OrQuery orQuery = new OrQuery();
				notResult = orQuery.unionPostings(notQueryComponents, index);
				NotQuery notQuery = new NotQuery();
				if(!CollectionUtils.isEmpty(notResult))
				result = notQuery.notPosting(positiveResult, notResult);
				else {
					result = positiveResult;
				}
			} else {
				result = positiveResult;
			}
		}
		if (result != null)
			return result;
		else
			return Collections.EMPTY_LIST;
	}

	/**
	 * This method returns the AND result of an AND query.
	 * @param mQueryComponents Query components that need to be ANDed to get the result
	 * @param index The Positional inverted index used to refer to vocabulary
	 * @return Returns the list of postings of the query components.
	 */
	public List<Posting> intersectPostings(List<QueryComponent> mQueryComponents, Index index) {
		try {
			List<Posting> postingResult = mQueryComponents.remove(0).getPostings(index);
			List<QueryComponent> restComponents = new ArrayList<QueryComponent>();
			if (mQueryComponents.size() == 1)
				restComponents = mQueryComponents;
			else {
				if (mQueryComponents.size() > 1)
				restComponents = mQueryComponents.subList(0, mQueryComponents.size());
			}
				while (!CollectionUtils.isEmpty(postingResult) && !CollectionUtils.isEmpty(restComponents)) {
					postingResult = intersectPosting(postingResult, restComponents.remove(0).getPostings(index));
			}
			return postingResult;
		} catch (Exception e) {
			return Collections.EMPTY_LIST;
		}
	}

	/**
	 * Given two postings p1 and p2, does the AND merge between them
	 * @param p1 List of postings for term1
	 * @param p2 List of postings for term2
	 * @return Returns list of postings that contain both term1 and term2
	 */
	public List<Posting> intersectPosting(List<Posting> p1, List<Posting> p2) {
		List<Posting> intersectResult = new ArrayList<Posting>();
		if (p1 != null && p2 != null) {
			for (int i = 0, j = 0; i < p1.size() && j < p2.size();) {
				if (p1.get(i).getDocumentId() == p2.get(j).getDocumentId()) {
					intersectResult.add(p1.get(i));
					i += 1;
					j += 1;
				} else if (p1.get(i).getDocumentId() < p2.get(j).getDocumentId()) {
					i += 1;
				} else {
					j += 1;
				}
			}
		}
		return intersectResult;
	}

	@Override
	public String toString() {
		return String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
