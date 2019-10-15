package main;

import modules.documents.DocumentCorpus;
import modules.index.DiskIndex;
import modules.index.Posting;
import modules.query.BooleanQueryParser;
import modules.query.QueryComponent;
import modules.text.AdvancedTokenProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

class QueryResults {

    QueryResults(DocumentCorpus documentCorpus){
        long start = System.nanoTime();                          // start timer
        new Indexer(documentCorpus);                             // Index the corpus by calling indexCorpus() method
        long end = System.nanoTime();                            // Stop timer
        long elapsedTime = end - start;
        double executionTime = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
        System.out.println("Time taken to index the corpus is "+ executionTime);
        System.out.println("\nSpecial queries available:");
        System.out.println(":q      -   Quit to the main screen.");
        System.out.println(":vocab  -   print upto first 1000 terms in the vocabulary sorted alphabetically.");
        System.out.println(":stem   -   print the stemmed form of a word or term");
        System.out.println(":boolean-   set mode to boolean query mode");
        System.out.println(":ranked -   set mode to ranked query mode. [DEFAULT]");
        try{
            displayResults(documentCorpus);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void displayResults(DocumentCorpus documentCorpus){
        String mode = "ranked";
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        DiskIndex diskIndex = new DiskIndex();
        boolean quit = false;

        while(!quit){
            String query="";
            try{
                System.out.print("\nPlease enter query to be searched: ");
                query = br.readLine();
            }catch (IOException e){
                e.printStackTrace();
            }

            if (query.contains(":vocab")){
                List<String> vocabulary = diskIndex.getVocabulary();
                System.out.println("Upto first 1000 terms in vocabulary are as follows:");
                int vocabSize = vocabulary.size();
                if (vocabSize > 1000)
                    vocabSize = 1000;
                for (int i = 0; i < vocabSize; i++) {
                    System.out.println(vocabulary.get(i));
                }
            }

            else if (query.contains(":stem")) {
                String stemmedToken;
                try {
                    stemmedToken = AdvancedTokenProcessor.stemTokenJava(query.split("\\s+")[1]);
                    System.out.println("Stemmed token is :" + stemmedToken);
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            }

            else if (query.contains(":q")) { quit = true; }

            else if (query.contains(":boolean")) { mode = "boolean"; System.out.println("Switched to Boolean Mode");}

            else if (query.contains(":ranked")) { mode = "ranked"; System.out.println("Switched to Ranked Mode");}

            else if (!query.isEmpty()) {
                switch (mode){
                    case "boolean":
                        DisplayBooleanResults(documentCorpus, diskIndex, query);
                        break;
                    case "ranked":
                        DisplayRankedResults(documentCorpus, diskIndex, query);
                }
            } else {
                System.out.println("Please enter a valid search term!");
            }
        }
    }

    private void DisplayRankedResults(DocumentCorpus documentCorpus, DiskIndex diskIndex, String query){
        int corpusSize = documentCorpus.getCorpusSize();
        String[] userQuery = query.split("\\s+");
        List<String> userQueryStemmed = new ArrayList<>();
        AdvancedTokenProcessor advancedTokenProcessor = new AdvancedTokenProcessor();
        for (String uQuery : userQuery) {
            try {
                List<String> stemmedQueries = advancedTokenProcessor.processToken(uQuery);
                for (String stemmedQuery : stemmedQueries) {
                    if (diskIndex.hasPostings(stemmedQuery)) {
                        userQueryStemmed.add(stemmedQuery);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

        try {
            WeightScheme weightscheme = new WeightScheme();
            PriorityQueue<Integer> defaultWeightPQ = weightscheme.getDefaultWeight(diskIndex, userQueryStemmed, corpusSize);
            PriorityQueue<Integer> okapibm25PQ = weightscheme.getOkapibm25(diskIndex, userQueryStemmed, corpusSize);
            PriorityQueue<Integer> wackyPQ = weightscheme.getWacky(diskIndex, userQueryStemmed, corpusSize);
            PriorityQueue<Integer> TFidfPQ = weightscheme.getTfIdf(diskIndex, userQueryStemmed, corpusSize);

            System.out.println("Results : \tdefault | okapi-bm25 | wacky | idf documentID and documentName for reference\n");
            try {
                for (int i = 0; i < 20; i++) {
                    System.out.print("Position : " + (i + 1) + " :\t\t");
                    System.out.print(defaultWeightPQ.remove()+ "\t\t");
                    System.out.print(okapibm25PQ.remove() + "\t\t");
                    System.out.print(wackyPQ.remove() + "\t\t");
                    int tfIdf = TFidfPQ.remove();
                    System.out.print(tfIdf + "\t\t");
                    System.out.println(documentCorpus.getDocument(tfIdf).getTitle());
                }
            } catch (Exception e) {
                System.out.println("Not enough documents");
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void DisplayBooleanResults(DocumentCorpus documentCorpus, DiskIndex diskIndex, String query){
        BooleanQueryParser parser = new BooleanQueryParser();
        QueryComponent newQuery = parser.parseQuery(query);
        List<Posting> postingList = newQuery.getPostings(diskIndex);
        for (Posting p : postingList){
            System.out.println(documentCorpus.getDocument(p.getDocumentId()).getTitle());
        }
    }

}
