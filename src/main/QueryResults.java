package main;

import modules.documents.DirectoryCorpus;
import modules.documents.DocumentCorpus;
import modules.index.Index;
import modules.index.DiskIndex;
import modules.index.Posting;
import modules.query.BooleanQueryParser;
import modules.query.QueryComponent;
import modules.text.AdvancedTokenProcessor;
import modules.text.TokenProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.TimeUnit;

public class QueryResults {
    public QueryResults(DocumentCorpus documentCorpus){
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
                String stemmedToken = "";
                try {
                    stemmedToken = AdvancedTokenProcessor.stemTokenJava(query.split("\\s+")[1]);
                    System.out.println("Stemmed token is :" + stemmedToken);
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            }

            else if (query.contains(":q")) { quit = true; }

            else if (query.contains(":boolean")) { mode = "boolean"; }

            else if (query.contains(":ranked")) { mode = "ranked"; }

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

    public void DisplayRankedResults(DocumentCorpus documentCorpus, DiskIndex diskIndex, String query){
        List<String> userQuery = Arrays.asList(query.split("\\s+"));
        List<String> uQueries = new ArrayList<>();
        for (String uQuery : userQuery) {
            try {
//                        System.out.println("Step new query");
                String uQuerystemmed = AdvancedTokenProcessor.stemTokenJava(uQuery);
                if (diskIndex.hasPostings(uQuerystemmed)) {
                    System.out.println(uQuery);
                    uQueries.add(uQuerystemmed);
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }

            try {
                WeightScheme weightscheme = new WeightScheme(uQueries, diskIndex, documentCorpus);
                PriorityQueue<Integer> WSdefPQ = weightscheme.defaultWeightDocIds();
                PriorityQueue<Integer> WSokiPQ = weightscheme.okapiweightSortedDocIds();
                PriorityQueue<Integer> WSwaPQ = weightscheme.wackySortedDocIds();
                PriorityQueue<Integer> WSidfPQ = weightscheme.idfSortedDocIds();
                System.out.println("\n\nResults : default | okapi-bmp | wacky | idf");
                try {
                    for (int i = 0; i < 20; i++) {
                        System.out.print("Position : " + (i + 1) + " : ");
                        System.out.print(WSdefPQ.remove() + " ");
                        System.out.print(WSokiPQ.remove() + " ");
                        System.out.print(WSwaPQ.remove() + " ");
                        System.out.print(WSidfPQ.remove() + " ");
                        System.out.println();
                    }
                } catch (Exception e) {
                    System.out.println("Not enough documents");
                }

            } catch (Throwable e) {
                e.printStackTrace();
            }
        }

    }

    public void DisplayBooleanResults(DocumentCorpus documentCorpus, DiskIndex reader, String query){
        BooleanQueryParser parser = new BooleanQueryParser();
        QueryComponent NewQuery = parser.parseQuery(query);
        List<Posting> postingList = NewQuery.getPostings(reader);
        for (Posting p : postingList){
            System.out.println(documentCorpus.getDocument(p.getDocumentId()).getTitle());
        }
    }

}
