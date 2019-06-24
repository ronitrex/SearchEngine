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
    private PositionalInvertedIndex posindex;

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
        PositionalInvertedIndex index = new PositionalInvertedIndex();
        ArrayList<HashMap<String, Integer>> docLoads = new ArrayList<>();
        ArrayList<Double> load = new ArrayList<>();
        ArrayList<Integer> docLength = new ArrayList<>();
        ArrayList<Double> avgtftdList = new ArrayList<>();
        double avgDocLength = 0;

        for (Document doc : documentList) {
            System.out.println("Indexing Document :" + doc.getTitle());
            EnglishTokenStream docStream = new EnglishTokenStream(doc.getContent());

            Iterable<String> docTokens = docStream.getTokens();

            int i = 0;
            HashMap<String, Integer> wordFreq = new HashMap<>();
            for (String tokens : docTokens) {

                i += 1;
                List<String> processedTokens = processor.processToken(tokens);
                for (String processedToken : processedTokens) {
                    if (wordFreq.containsKey(processedToken)) {
                        wordFreq.replace(processedToken, wordFreq.get(processedToken)+1);
                    }
                    else{
                        wordFreq.put(processedToken, 1);
                    }
                    index.addTerm(processedToken, doc.getId(), i);
                }
            }
            docLoads.add(wordFreq);
            double currentLoad = 0.0f;
            Set<String> keys = wordFreq.keySet();
            int docL = keys.size();
            docLength.add(docL);
            avgDocLength = avgDocLength + docL;
            double avgtftd = 0.0f;

            for(String key: keys){
                int tftd = wordFreq.get(key);
                currentLoad = currentLoad + Math.sqrt(1 + Math.log(tftd));
                avgtftd = avgtftd + tftd;
//				System.out.println(currentLoad + " " + key + " "+ Math.sqrt(1 + Math.log(wordFreq.get(key))) );
            }
            avgtftd = avgtftd/docL;
            avgtftdList.add(avgtftd);
            load.add(Math.sqrt(currentLoad));
        }

        avgDocLength = avgDocLength/docLength.size();
        DiskIndexWriter writer = new DiskIndexWriter();
        writer.WriteIndex(index, load, docLoads, docLength, avgtftdList, avgDocLength);
        posindex = index;
    }

}
