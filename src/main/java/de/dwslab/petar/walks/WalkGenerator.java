package de.dwslab.petar.walks;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.sparql.sse.SSE;
import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ReadWrite;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.FileManager;

public class WalkGenerator {
	public static final Logger log = LoggerFactory
			.getLogger(WalkGenerator.class);

	public static String directPropsQuery = "SELECT ?p ?o WHERE {$ENTITY$ ?p ?o}";

	/**
	 * defines the depth of the walk (only nodes are considered as a step)
	 */
	public static int depthWalk = 4;
	/**
	 * defines the number of walks from each node
	 */
	public static int numberWalks = 200;

	/**
	 * the query for extracting paths
	 */
	public static String walkQuery = "";

	public static int processedEntities = 0;
	public static int processedWalks = 0;
	public static int fileProcessedLines = 0;

	public static long startTime = 0;

	/**
	 * the rdf model
	 */
	public static Model model;

	public static Dataset dataset;

	public static String fileName = "walks_en.txt";

	/**
	 * file writer for all the paths
	 */
	public static Writer writer;

	public void generateWalks(String repoLocation, String outputFile,
			int nmWalks, int dpWalks, int nmThreads, int offset, int limit, String inputFile) throws IOException {
		// set the parameters
		numberWalks = nmWalks;
		depthWalk = dpWalks;
		fileName = outputFile;

		// int the writer
		try {
			writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outputFile, false), "utf-8"),
					32 * 1024);
		} catch (UnsupportedEncodingException | FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		HashMap<Integer,Dictionary<String,Integer>> entitiesWithPositions = selectAllEntitiesFromFilePerQuery (inputFile);
		
		for (Entry<Integer, Dictionary<String, Integer>> entry : entitiesWithPositions.entrySet())
			generateQueryFromEntities (depthWalk, numberWalks, entry.getValue());
		
		
		// generate the query
		walkQuery = generateQuery(depthWalk, numberWalks);

		// open the dataset
		
		//model = ModelFactory.createDefaultModel();
		 
	     dataset = TDBFactory.createDataset(repoLocation);
	     model = ModelFactory.createDefaultModel();
	     model.read("/home/souza/rdf2vec/entities/mappingbased_objects_en.ttl.gz");
	   //  model.read("/home/souza/rdf2vec/entities/e.ttl.gz");
	    /* 
	     Triple triple = SSE.parseTriple("(<"+ line + "> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Thing>)") ;
			g2.add(triple) ;
		
		
		model = ModelFactory.createModelForGraph(g2) ;
		*/
		System.out.println("	SELECTING all entities from repo");
//		List<String> entities = selectAllEntities(offset, limit);
		//List<String> entities = selectAllEntitiesFromFile (inputFile);
		
		//List<String> entitiesTotal = selectAllEntities (offset, limit);

	/*	System.out.println("Total number of entities to process: "
				+ entities.size());
		ThreadPoolExecutor pool = new ThreadPoolExecutor(nmThreads, nmThreads,
				0, TimeUnit.SECONDS,
				new java.util.concurrent.ArrayBlockingQueue<Runnable>(
						entities.size()));

		startTime = System.currentTimeMillis();*/
		
		/*
		for (String entity : entities) {

			EntityThread th = new EntityThread(entity);

			pool.execute(th);

		}

		pool.shutdown();
		try {
			pool.awaitTermination(10, TimeUnit.DAYS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 	*/
	}

	/**
	 * Selects all entities from the repo
	 * 
	 * @return
	 */
	public static List<String> selectAllEntities(int offset, int limit) {
		List<String> allEntities = new ArrayList<String>();

/*		String queryStrign = "Select ?s Where { ?s <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/2002/07/owl#Thing>} OFFSET "
				+ offset + "LIMIT " + limit;
*/
	
	String queryStrign = "SELECT DISTINCT (COUNT(?s) AS ?count) where {?s ?p ?o } ";  
		Query query = QueryFactory.create(queryStrign);

		// Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		int totalEntities = 0 ;
		
		while (results.hasNext()) {
			QuerySolution result = results.next();
			System.out.println("Total entities: " + result.get("count"));
			totalEntities = result.get("count").asLiteral().getInt();
//			allEntities.add(result.get("s").toString());
		}
		
		String queryStrign2 =  "SELECT ?s ?o where {?s ?p ?o }" /*OFFSET " + offset + " LIMIT " + totalEntities*/ ;
		Query query2 = QueryFactory.create(queryStrign2);
		
		qe = QueryExecutionFactory.create(query2, model);
		results = qe.execSelect();
		
		while (results.hasNext()) {
			QuerySolution result = results.next();
			
			 if (!allEntities.contains(result.get("s").toString()))
				allEntities.add(result.get("s").toString());
			 if (!allEntities.contains(result.get("o").toString()))
				allEntities.add(result.get("o").toString());
		}
		
		qe.close();
		return allEntities;
	}

	/**
	 * Selects all entities from the file
	 * 
	 * @return
	 * @throws IOException 
	 */
	public static List<String> selectAllEntitiesFromFile(String filePath) throws IOException {
		List<String> allEntities = new ArrayList<String>();

		File f = new File (filePath);
		FileReader fr = new FileReader (f);
		
		BufferedReader br = new BufferedReader (fr);
		
		
		String line = "";
		while ((line=br.readLine())!=null)
		{
			if (!allEntities.contains(line))
				allEntities.add(line);
		}
		
		return allEntities;
		
	}

	/**
	 * Selects all entities from the file
	 * 
	 * @return
	 * @throws IOException 
	 */
	public static HashMap<Integer,Dictionary<String,Integer>> selectAllEntitiesFromFilePerQuery(String filePath) throws IOException {
		List<String> allEntities = new ArrayList<String>();

		HashMap<Integer,Dictionary<String,Integer>> queriesMap = new HashMap<Integer,Dictionary<String,Integer>>();
		File f = new File (filePath);
		FileReader fr = new FileReader (f);
		
		BufferedReader br = new BufferedReader (fr);
		
		
		String line = "";
		while ((line=br.readLine())!=null)
		{
			StringTokenizer token = new StringTokenizer (line);
			
			if (line.contains("#"))
				continue;
			
			Dictionary<String,Integer> dict = new Hashtable<String,Integer>();
			
			while (token.hasMoreElements())
			{
				String e = token.nextToken();
				int position = Integer.parseInt(token.nextToken());
				int queryId = Integer.parseInt(token.nextToken());
				
				if (queriesMap.containsKey(queryId))
				{
					Dictionary<String,Integer> d = queriesMap.get(queryId);
					d.put(e, position);
					queriesMap.put(queryId, d);
				}
				else
				{
					Dictionary<String,Integer> d = new Hashtable<String,Integer>();
					d.put(e, position);
					queriesMap.put(queryId, d);
				}
			}
		}
		
		return queriesMap;
		
	}

	
	
	/**
	 * Adds new walks to the list; If the list is filled it is written to the
	 * file
	 * 
	 * @param tmpList
	 */
	public synchronized static void writeToFile(List<String> tmpList) {
		processedEntities++;
		processedWalks += tmpList.size();
		fileProcessedLines += tmpList.size();
		for (String str : tmpList)
			try {
				writer.write(str + "\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		if (processedEntities % 100 == 0) {
			System.out
					.println("TOTAL PROCESSED ENTITIES: " + processedEntities);
			System.out.println("TOTAL NUMBER OF PATHS : " + processedWalks);
			System.out.println("TOTAL TIME:"
					+ ((System.currentTimeMillis() - startTime) / 1000));
		}
		// flush the file
		if (fileProcessedLines > 3000000) {
			fileProcessedLines = 0;
			try {
				writer.flush();
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			int tmpNM = (processedWalks / 3000000);
			String tmpFilename = fileName.replace(".txt", tmpNM + ".txt");
			try {
				writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(tmpFilename, false), "utf-8"),
						32 * 1024);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * generates the query with the given depth
	 * 
	 * @param depth
	 * @return
	 */
	public static String generateQuery(int depth, int numberWalks) {
		String selectPart = "SELECT ?p ?o1";
		String mainPart = "{ $ENTITY$ ?p ?o1  ";
		String query = "";
		for (int i = 1; i < depth; i++) {
			mainPart += ". ?o" + i + " ?p" + i + "?o" + (i + 1);
			selectPart += " ?p" + i + "?o" + (i + 1);
		}
		//query = selectPart + " WHERE " + mainPart + "} LIMIT 1000";
		query = selectPart + " WHERE " + mainPart + " " 
		 + " BIND(RAND() AS ?sortKey) } ORDER BY ?sortKey LIMIT "
		 + numberWalks;
		return query;
	}
	
	/**
	 * generates the query with the given depth
	 * 
	 * @param depth
	 * @return
	 */
	public static String generateQueryFromEntities(int depth, int numberWalks, Dictionary<String,Integer> dict) {
		String selectPart = "SELECT ?p ?o1";
		String mainPart = "{ $ENTITY$ ?p ?o1  ";
		String query = "";
		
		List<String> keys = Collections.list(dict.keys());
		Map<String, Integer> dictCopy = keys.stream().collect(Collectors.toMap(Function.identity(), dict::get));
		
		for (Entry<String,Integer> e : dictCopy.entrySet())
		{
			int position = e.getValue();
			String uri = e.getKey();
			String nameVar = "$ENTITY" + position + "$";
			if (position <=3)
			{
				switch (position)
				{
					case (1): 
					{
						mainPart = "{ " + nameVar + " ?p ?o1  ";
						break;
					}
					case (2):
					{
						mainPart = "{ ?o1 " + nameVar + " ?o2  ";
						selectPart = "SELECT ?o1 ?o2";
						break;
					}
					case (3):
					{
						mainPart = "{ ?o1 ?p " + nameVar;
						break;
					}
				}
				
				mainPart = mainPart.replace(nameVar, "<" + uri + ">");
					
			}
			else if (position > 3 && position <=6)
			{
				mainPart = "{ ?o1 ?p ?o2  ";
			}
				
			
		}
		for (int i = 1; i < depth; i++) {
			mainPart += ". ?o" + i + " ?p" + i + "?o" + (i + 1);
			selectPart += " ?p" + i + "?o" + (i + 1);
		}
		query = selectPart + " WHERE " + mainPart + " } LIMIT " + numberWalks;
		/*query = selectPart + " WHERE " + mainPart + " " 
		 + " BIND(RAND() AS ?sortKey) } ORDER BY ?sortKey LIMIT "
		 + numberWalks;*/
		return query;
	}
	static class EntityThread implements Runnable {

		private String entity;

		private List<String> finalList;

		public EntityThread(String entity) {
			this.entity = entity;
			finalList = new ArrayList<String>();
		}

		@Override
		public void run() {
			processEntity();
			writeToFile(finalList);

		}

		private void processEntity() {

			// get all the walks
			List<String> tmpList = new ArrayList<String>();
			String queryStr = walkQuery.replace("$ENTITY$", "<" + entity + ">");

			executeQuery(queryStr, tmpList);

			Random rand = new Random();
			for (int i = 0; i < numberWalks; i++) {
				if (tmpList.size() < 1)
					break;
				int randomNum = rand.nextInt(tmpList.size());
				if (randomNum > tmpList.size() - 1)
					randomNum = tmpList.size() - 1;
				finalList.add(tmpList.get(randomNum));

				tmpList.remove(randomNum);
			}

			// get all the direct properties
			queryStr = directPropsQuery.replace("$ENTITY$", "<" + entity + ">");
			executeQuery(queryStr, finalList);

		}

		public void executeQuery(String queryStr, List<String> walkList) {
			Query query = QueryFactory.create(queryStr);
			dataset.begin(ReadWrite.READ);
		
			QueryExecution qe = QueryExecutionFactory.create(query, model);	
			ResultSet resultsTmp = qe.execSelect();
			String entityShort = entity.replace("http://dbpedia.org/resource/",
					"dbr:");
			ResultSet results = ResultSetFactory.copyResults(resultsTmp);
			qe.close();
			dataset.end();
		
			while (results.hasNext()) {
				QuerySolution result = results.next();
				String singleWalk = entityShort + "->";
				// construct the walk from each node or property on the path
				for (String var : results.getResultVars()) {
					try {
						// clean it if it is a literal
					/*	if (result.get(var) != null
								&& result.get(var).isLiteral()) {
							String val = result.getLiteral(var).toString();
							val = val.replace("\n", " ").replace("\t", " ")
									.replace("->", "");
							singleWalk += val + "->";
						} else */if (result.get(var) != null) {
							singleWalk += result
									.get(var)
									.toString()
									.replace("http://dbpedia.org/resource/",
											"dbr:")
									.replace("http://dbpedia.org/ontology/",
											"dbo:")
									.replace(
											"http://www.w3.org/1999/02/22-rdf-syntax-ns#",
											"rdf:")
									.replace(
											"http://www.w3.org/2000/01/rdf-schema#",
											"rdfs:").replace("->", "")
									+ "->";
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				walkList.add(singleWalk);
			}

		}
	}

	public static void main(String[] args) throws NumberFormatException, IOException {
		System.out
				.println("USAGE:  repoLocation outputFile nmWalks dpWalks nmThreads inputFile");
		WalkGenerator generator = new WalkGenerator();
		generator.generateWalks(args[0], args[1], Integer.parseInt(args[2]),
				Integer.parseInt(args[3]), Integer.parseInt(args[4]),
				Integer.parseInt(args[5]), Integer.parseInt(args[6]),args[7]);
	}
}
