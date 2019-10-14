package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import modules.documents.DirectoryCorpus;
import modules.documents.DocumentCorpus;
import modules.index.*;

public class SearchEngine {

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
//        Index index = ind.getPositionalInvertedIndex();


        // stop the timer
        long end = System.nanoTime();
        long elapsedTime = end - start;
        double executionTime = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
        // Print out the time taken to load and index the documents
        System.out.println("Time taken to load documents and index corpus in seconds:" + executionTime);
        System.out.println("\nDo you want to work in \n1. Boolean Query Mode \n2. Ranked Query Mode \nEnter Choice number.");
        int choice = Integer.parseInt(br.readLine());
        try{
            QueryResults results = new QueryResults(corpus, corpusSize);
            DiskIndex reader = new DiskIndex();
            if (choice == 1){
                results.DisplayBooleanResults(reader);
            }
            else if (choice == 2){
                results.DisplayRankedResults(reader);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
