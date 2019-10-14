package modules.index;

import java.util.ArrayList;
import java.util.List;


public class DiskIndex implements Index {
    private DiskIndexReader reader = new DiskIndexReader();
    private List<String> vocabulary = reader.getVocabulary();
    private List<Long> postingsPositions = reader.getPostingsPosition();

    @Override
    public List<Posting> getPostings(String term) {
        List<Posting> postingList = new ArrayList<>();
        try {
            long postingPosition = postingsPositions.get(BinarySearchVocabulary(term));
            postingList = reader.PostingListReader(postingPosition);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Posting list can't be found for " + term);
        }
        return postingList;
    }

    @Override
    public List<String> getVocabulary(){
        return vocabulary;
    }

    public boolean hasPostings(String term){
        return (BinarySearchVocabulary(term)!=-1);
    }


    /**
     * Retrieves the position at which the queried term is. This also gives the position it has in
     * vocabTablePositions list
     * @param searchTerm    The term that needs to be searched
     * @return              index of the search term; -1 if not found
     */
    private int BinarySearchVocabulary(String searchTerm){
        int low = 0;
        int high = vocabulary.size() - 1;
        int mid = -2;
        while (low <= high) {
            mid = (low + high) / 2;
            if (vocabulary.get(mid).compareTo(searchTerm) < 0) {
                low = mid + 1;
            } else if (vocabulary.get(mid).compareTo(searchTerm) > 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        if(mid==0) {
            return mid;
        }
//        System.out.println(mid + " error value in result: " + searchTerm);
        return -1;
    }

    public double getLoad(int DocID){
        return reader.getLoad(DocID);
    }

    public double getavgDocLength(){
        return reader.getavgDocLength();
    }

    public int getbyteLength(int DocID){
        return reader.getbyteLength(DocID);
    }

    public int getdocLength(int DocID){
        return reader.getdocLength(DocID);
    }

    public double getavgtftd(int DocID){
        return reader.getavgtftd(DocID);
    }
}