# SearchEngine

A simple search engine that works with JSON files to perform search operations over an indexed directory.


# Introduction :
	
The project involved creating an index from a corpus of documents provided. The project focused on the following key areas 

    - How can the indexing process be made faster
    - How to gain better insights from the data created while creating such an index
    - different kinds of ranking algorithms that can applied to gain more meaningful information from these results
    - precision-recall trade-off     


**Abstract :**

Information retrieval represents one of the most classical problem in computer science.  How do we define an information as useful to user and how do we ascertain if we can improve precision and recall of our search engine in meaningful ways:

At first we look at how common stop words like the, is etc. can be effectively ignored to produce better results in most scenarios. We call this the weight a term has in a document. If a term appears too many times in a document it is highly likely that the document could be extremely relevant for that term. But on the other hand this approach is biased towards very large documents, which have a certain term repeat many times. Therefore it becomes necessary to normalize the effect this property has on our results.

Other interesting thing to note is that we can assume that if a term appears too many times in a given corpus, then it can not really be trusted to be a good discriminator between two documents obtained for a given query.

Therefore in due course, we find out some features about our corpus and the index we created from it and somehow engineer a concept of mathematical relevance. It may or may not be accurate. And for terms like “Apple” which can have different meaning depending on the context in which the term has been used.



# Overview :

We process a query without any Boolean operators and return the top 20 documents satisfying the query. We use the term at a time algorithm :



a) Acquire an accumulator for results for each document. This is how relevant a document is to the user query.

b) For each term in user query, we get the posting lists returned in the results of the posting for the query term. Then for each posting

-  Calculate wqt using the given formulas
-  Calculate wdt using the given formulas

- For each document d returned in the posting list of the result of query term increase the accumulator Accum of that document by (wqt * wdt) and divide now zero accumulators with Ld.


Example : a term like 'Jacaranda trees' will return many documents that may contain the word trees many times but our irrelevant. But a document that mentions the term 'Jacaranda' is certainly more relevant to our query.





***Variant tf-idf formulas:*** The information we just discussed has been encapsulated in these formulas through which we hope to get good results with good precision and recall. 
![](file:./readme/Schemes.png)

N → Number of documents in the corpora. 

tf → the term frequency of a term in a document. i.e. number of times the term appears in a given document.


dft → the document frequency of a term in document corpus. i.e the number of documents containing the term in a given document corpus.

idft → the inverse of dft.
  
W q,t → the weight associated with a query term.
      
W d,t → the weight associated with each document contained in the results for the given query term.
      
Ld → the normalization of the effect of document length on our results.


These formulas take different factors into consideration while giving us a result. All these formulas can be thought of having some biases towards the factors they favor heavily, and we will see in our results how these factors come into play.


# Methodology :

• The entire document corpus was indexed using a positional inverted index, which in simpler terms is a way of keeping track of which document appears where in our corpus. Once indexing is complete, the index is written to disk. We also write other important values about our index on the disk such as the tf and idf values.

• The disk based index is created from a memory based index which is sorted alphabetically. This allows us to perform more efficient binary search over a document corpus for our term. The index stores a list of 'postings' for a given term. A posting is an object that represents a document and all the positions a term appears in that document. A list of postings thus give us all the documents containing a given term and the position of terms in the document.
  
  
• When a user submits a query, the index stored on the disk is queried for a list of documents that contain the particular term. The index is made of stemmed tokens, and the user query is also stemmed to retrieve the results. This is done to avoid having two lists for words like “national, nation” which may represent a single information need.
  
• Once a list of documents corresponding to a query have been retrieved, the formulas mentioned above are applied over it. The formulas give discriminating set of values that we can use to provide some form of notion of relevance to our documents.

• The documents are stored in a priority queue and then popped from the top of the queue. The most relevant documents as per our query are ranked at the top followed by other documents.
  
• The results are compared to see which formula performs closer to our ideal results

**Running the project :**

The project was developed on openjdk8 and supports maven dependency management. Simply import the pom.xml file to automatically configure the IDE.

The testdirectory contains some documents that this project has been tested on. The relative path of these directories can simply be copy-pasted when running the project.