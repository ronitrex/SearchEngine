package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

import modules.documents.DirectoryCorpus;
import modules.documents.DocumentCorpus;
import modules.index.*;
import modules.text.AdvancedTokenProcessor;

public class PositionalTermDocumentIndexer {

    public static void main(String[] args) throws IOException {

        System.out.println("Please enter the path of the directory to index : \n1.: testdirectory/nationalparks \n2.: testdirectory/dummyJSON \n3.: testdirectory/relevance_cranfield");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String directoryPath = br.readLine();
        try {
            Paths.get(directoryPath);
        } catch (InvalidPathException e) {
            System.out.println("Invalid path! Please enter a valid directory path. " + e);
        }

        // start timer
        long start = System.nanoTime();

        // DirectoryCorpus.loadTextDirectory(Paths.get("").toAbsolutePath(), ".txt");
        // Load files from directory and read
        DocumentCorpus corpus = DirectoryCorpus.loadJSONFileDirectory(Paths.get(directoryPath), ".json");
        int corpusSize = corpus.getCorpusSize();
        System.out.println("\nFound " + corpusSize+ " documents in the directory. Indexing the documents...\n");

        // Index the corpus by calling indexCorpus() method
        new Indexer(corpus);
//        Index PosIndex = ind.getPosIndex();
//        for (String i : PosIndex.getVocabulary()){
//            System.out.print(i + " || ");
//        }

        PositionalDiskIndex reader = new PositionalDiskIndex();
        // stop the timer
        long end = System.nanoTime();
        long elapsedTime = end - start;
        double executionTime = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
        // Print out the time taken to load and index the documents
        System.out.println("Time taken to load documents and index corpus in seconds:" + executionTime);

        do {
            System.out.println("\nSpecial terms to search for operations:");
            System.out.println(":q - exit the program.");
            System.out.println(":stem 'token' - stems the token and prints the stemmed token.");
            System.out.println(":index directoryname - index the directoryname.");
            System.out.println(":vocab - print first 1000 terms in the vocabulary sorted alphabetically.");

            System.out.print("\nPlease enter query to be searched: ");
//			String query = "nation";
            String query = br.readLine();
            if (query.contains(":q") || query.contains(":stem") || query.contains(":index") || query.contains(":vocab"))
                executeSpecialQuery(query, reader);
            else if (!query.isEmpty()) {
                List<String> userQuery = Arrays.asList(query.split("\\s+"));
                List<String> uQueries = new ArrayList<>();
                for (String uQuery : userQuery) {
                    try {
//                        System.out.println("Step new query");
                        String uQuerystemmed = AdvancedTokenProcessor.stemTokenJava(uQuery);
                        if (reader.hasPostings(uQuerystemmed)) {
                            System.out.println(uQuery);
                            uQueries.add(uQuerystemmed);
                        }
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }

                    try {
                        WeightScheme weightscheme = new WeightScheme(uQueries, reader, corpus, corpusSize);
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

            } else {
                System.out.println("Please enter a valid search term!");
            }
        } while (true);

    }

    private static void executeSpecialQuery(String query, Index index) {
        if (query.equals(":q")) {
            System.out.println("Exiting System...");
            System.exit(0);
        } else if (query.contains(":stem")) {
            String stemmedToken = "";
            try {
                stemmedToken = AdvancedTokenProcessor.stemTokenJava(query.split("\\s+")[1]);
                System.out.println("Stemmed token is :" + stemmedToken);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        } else if (query.contains(":index")) {
            System.out.println("Indexing...");
            String directoryPath = Paths.get(query.split("\\s+")[1]).toString();
            DocumentCorpus corpus = DirectoryCorpus.loadTextDirectory(Paths.get(directoryPath).toAbsolutePath(),
                    ".txt");
//            index = indexCorpus(corpus);
        } else if (query.contains(":vocab")) {
            List<String> vocabulary = index.getVocabulary();
            System.out.println("First 1000 terms in vocabulary are as follows:");
            int vocabSize = vocabulary.size();
            if (vocabSize > 1000)
                vocabSize = 1000;
            for (int i = 0; i < vocabSize; i++) {
                System.out.println(vocabulary.get(i));
            }
        }
    }
}
