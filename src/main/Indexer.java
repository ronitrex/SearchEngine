package main;
import java.util.*;

import modules.documents.Document;
import modules.documents.DocumentCorpus;
import modules.index.*;
import modules.text.AdvancedTokenProcessor;
import modules.text.EnglishTokenStream;
import modules.text.TokenProcessor;

public class Indexer {
    private DocumentCorpus ucorpus;
    private MemoryIndex posindex;

    public Indexer(DocumentCorpus corpus){
        System.out.println("Indexer is initializing.");
//        System.out.println(corpus.getCorpusSize());
        ucorpus = corpus;
        indexCorpus();
    }

    public Index getPosIndex(){
        return posindex;
    }

    private void indexCorpus(){
        TokenProcessor processor = new AdvancedTokenProcessor();
        Iterable<Document> documentList = ucorpus.getDocuments();
        MemoryIndex index = new MemoryIndex();

        // an ArrayList of words or keys present in a given document corpus and the frequency
        // with which a certain word or key appears in a given document
        ArrayList<HashMap<String, Integer>> wordFreqList = new ArrayList<>();

        // DocWeight is the normalization associated with the length of the document
        ArrayList<Double> DocWeight = new ArrayList<>();
        ArrayList<Integer> DocLength = new ArrayList<>();
        ArrayList<Double> avgtftdList = new ArrayList<>();
        double avgDocLength = 0;

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
                    index.addTerm(processedToken, doc.getId(), position);
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

        avgDocLength = avgDocLength/ucorpus.getCorpusSize();
        DiskIndexWriter writer = new DiskIndexWriter();
        posindex = index;
        writer.writeIndex(index, DocWeight, wordFreqList, DocLength, avgtftdList, avgDocLength);

    }

}
