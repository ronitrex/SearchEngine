package modules.index;

import modules.documents.Document;
import modules.documents.DocumentCorpus;
import modules.text.AdvancedTokenProcessor;
import modules.text.EnglishTokenStream;
import modules.text.TokenProcessor;

import java.util.*;

/**
 * Implements an Index using a positional inverted index.
 */
public class PositionalInvertedIndex implements Index{
    // an ArrayList of words or keys present in a given document corpus and the frequency
    // with which a certain word or key appears in a given document
    private ArrayList<HashMap<String, Integer>> wordFreqList = new ArrayList<>();

    // DocWeight is the normalization associated with the length of the document
    private ArrayList<Double> DocWeight = new ArrayList<>();
    private ArrayList<Integer> DocLength = new ArrayList<>();
    private ArrayList<Double> avgtftdList = new ArrayList<>();
    private HashMap<String, List<Posting>> vocabulary = new HashMap<>();
    private double avgDocLength = 0;

    public ArrayList<HashMap<String, Integer>> getWordFreqList(){
        return wordFreqList;
    }
    // DocWeight is the normalization associated with the length of the document
    public ArrayList<Double> getDocWeight(){
        return DocWeight;
    }
    public ArrayList<Integer> getDocLength(){
        return DocLength;
    }
    public ArrayList<Double> getAvgtftdList(){
        return avgtftdList;
    }
    public double getAvgDocLength() { return avgDocLength; }

    public PositionalInvertedIndex(DocumentCorpus corpus){
        System.out.println("PositionalInvertedIndex is starting");
        indexCorpus(corpus);
    }

    public List<Posting> getPostings(String term) {
        List<Posting> results;
        results = vocabulary.get(term);
        return results;
    }

    /**
     * Sorts the vocabulary alphabetically and returns the sorted vocabulary.
     */
    public List<String> getVocabulary() {
        List<String> mVocabulary = new ArrayList<>(vocabulary.keySet());
        Collections.sort(mVocabulary);
        return Collections.unmodifiableList(mVocabulary);
    }

    private void indexCorpus(DocumentCorpus corpus){
        TokenProcessor processor = new AdvancedTokenProcessor();
        Iterable<Document> documentList = corpus.getDocuments();


        for (Document doc : documentList) {
            System.out.println("Indexing Document :" + doc.getTitle());
            EnglishTokenStream docStream = new EnglishTokenStream(doc.getContent());

            Iterable<String> docTokens = docStream.getTokens();

            int position = 0;
            HashMap<String, Integer> wordFreq = new HashMap<>();
            for (String tokens : docTokens) {

                position += 1;
                List<String> processedTokens = processor.processToken(tokens);
                for (String processedToken : processedTokens) {
                    if (wordFreq.containsKey(processedToken)) {
                        wordFreq.replace(processedToken, wordFreq.get(processedToken)+1);
                    }
                    else{
                        wordFreq.put(processedToken, 1);
                    }
                    addTerm(processedToken, doc.getId(), position);
                }
            }
            wordFreqList.add(wordFreq);

            // DocWeight of the document currently being precessed.
            // The DocWeight of a document is square root of (sum of squares of all w d,t terms)
            double currentDocWeight = 0.0f;
            Set<String> keys = wordFreq.keySet();

            //docL is an indicator of the number of different terms in a document.
            int docL = keys.size();
            DocLength.add(docL);

            //avgDocLength is a single value for the entire document corpus
            //it is the average of all docL values
            avgDocLength = avgDocLength + docL;

            // avgtftd is the average tf t,d count for a particular document.
            double avgtftd = 0.0f;

            for(String key: keys){
                int tftd = wordFreq.get(key);
                double wdt = (1 + Math.log(tftd));
                currentDocWeight = currentDocWeight + (wdt*wdt);
                avgtftd = avgtftd + tftd;
            }
            avgtftd = avgtftd/docL;

            //avgtftd for the document being processed
            avgtftdList.add(avgtftd);
            // Ld associated with the current document.
            DocWeight.add(Math.sqrt(currentDocWeight));
        }

        avgDocLength = avgDocLength/corpus.getCorpusSize();
    }



    /**
     * Adds the term to the index by recording the terms' position and document id.
     *
     * @param term       Term to be indexed
     * @param documentId DocumentId from where the term has been fetched
     * @param position   Position of the term in the document
     */
    public void addTerm(String term, int documentId, int position) {
        if (vocabulary.containsKey(term)) { // if the vocabulary contains the term then
            List<Posting> postingList = vocabulary.get(term); // get the related posting
            if (documentId > postingList.get(postingList.size() - 1).getDocumentId()) { // new document
                List<Integer> positions = new ArrayList<Integer>();
                positions.add(position);
                Posting p = new Posting(documentId, positions);
                postingList.add(p);
            } else if (documentId == postingList.get(postingList.size() - 1).getDocumentId()) {
                List<Integer> positions = postingList.get(postingList.size() - 1).getPositionsInDoc();
                positions.add(position);
            }

        } else { // vocabulary doesn't contain the term, add the new term to vocabulary
            List<Posting> postingList = new ArrayList<Posting>();
            List<Integer> positions = new ArrayList<Integer>();
            positions.add(position);
            Posting p = new Posting(documentId, positions);
            postingList.add(p);
            vocabulary.put(term, postingList);
        }
    }
}