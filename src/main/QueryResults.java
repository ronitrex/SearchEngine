package main;

import modules.documents.DirectoryCorpus;
import modules.documents.DocumentCorpus;
import modules.index.Index;
import modules.index.PositionalDiskIndex;
import modules.index.PositionalInvertedIndex;
import modules.index.Posting;
import modules.query.BooleanQueryParser;
import modules.query.QueryComponent;
import modules.text.AdvancedTokenProcessor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

public class QueryResults {
    private DocumentCorpus ucorpus;
    private int ucorpusSize;

    public QueryResults(DocumentCorpus corpus, int corpusSize){
        ucorpus = corpus;
        ucorpusSize= corpusSize;
        System.out.println("Special queries available:");
        System.out.println(":q - Quit the search engine.");
        System.out.println(":vocab - print first 1000 terms in the vocabulary sorted alphabetically.");
    }

    public void DisplayRankedResults(PositionalDiskIndex reader)  throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        do {
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
                        WeightScheme weightscheme = new WeightScheme(uQueries, reader, ucorpus, ucorpusSize);
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

    public void DisplayBooleanResults(Index index) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        do {
            System.out.print("\nPlease enter query to be searched: ");
//			String query = "nation";
            String query = br.readLine();
            if (query.contains(":q") || query.contains(":stem") || query.contains(":index") || query.contains(":vocab"))
                executeSpecialQuery(query, index);
            else if (!query.isEmpty()) {
                BooleanQueryParser parser = new BooleanQueryParser();
                QueryComponent NewQuery = parser.parseQuery(query);
                List<Posting> postingList = NewQuery.getPostings(index);
                for (Posting p : postingList){
                    System.out.println(ucorpus.getDocument(p.getDocumentId()).getTitle());
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
