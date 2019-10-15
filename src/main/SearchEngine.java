package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import modules.documents.DirectoryCorpus;
import modules.documents.DocumentCorpus;

public class SearchEngine {

    public static void main(String[] args) throws IOException {

        while(true){
            String directoryPath = "";
            System.out.println("Please enter the path of the directory to index :" +
                    "\n1.: testdirectory/nationalparks" +
                    "\n2.: testdirectory/dummyJSON" +
                    "\n3.: testdirectory/relevance_cranfield" +
                    "\n4.: Enter custom path" +
                    "\n5.: Quit the search engine"
            );
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            int choice = Integer.parseInt(br.readLine());

            switch (choice){
                case 1:
                    directoryPath = "testdirectory/nationalparks";
                    break;
                case 2:
                    directoryPath = "testdirectory/dummyJSON";
                    break;
                case 3:
                    directoryPath = "testdirectory/relevance_cranfield";
                    break;
                case 4:
                    System.out.println("Enter path : ");
                    directoryPath = br.readLine();
                    break;
                case 5:
                    System.exit(0);
                    break;
                default:
                    System.out.println("Some error.");
            }
            try {
                Paths.get(directoryPath);
            } catch (InvalidPathException e) {
                System.out.println("Invalid path! Please enter a valid directory path. " + e);
            }

            // DirectoryCorpus.loadTextDirectory(Paths.get("").toAbsolutePath(), ".txt");
            // Load files from directory and read
            DocumentCorpus corpus = DirectoryCorpus.loadJSONFileDirectory(Paths.get(directoryPath), ".json");
            int corpusSize = corpus.getCorpusSize();
            System.out.println("\nFound " + corpusSize+ " documents in the directory. Indexing the documents...\n");
            new QueryResults(corpus);
        }

    }

}
