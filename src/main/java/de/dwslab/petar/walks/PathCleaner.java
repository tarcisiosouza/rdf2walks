package de.dwslab.petar.walks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PathCleaner {

	public static void cleanPaths(String inputFolder, String outputFolder) {

		File inputFol = new File(inputFolder);
		String headLine = "";
		
		for (File f : inputFol.listFiles()) {
			if (f.getName().contains("page-links"))
				continue;
			BufferedReader br = null;
			Writer writer = null;
			try {
				Map<String, Integer> seenPaths = new HashMap<String, Integer>();
				InputStream fileStream = new FileInputStream(
						f.getAbsolutePath());
				InputStream gzipStream = new GZIPInputStream(fileStream);
				Reader decoder = new InputStreamReader(gzipStream, "UTF-8");
				writer = new OutputStreamWriter(new GZIPOutputStream(
						new FileOutputStream(outputFolder + "/" + f.getName(),
								false)), "utf-8");

				br = new BufferedReader(decoder);

				String line = "";

				int counter = 0;

				while ((line = br.readLine()) != null) {
					counter++;
					
					if (counter % 1000 == 0)
					{
						System.out.println("Line nm: " + counter);
						//writer.write("\n");
					}
					String newLine = "";
					try {
						String parts[] = line.split("->");
						String lastPart = parts[parts.length - 1];
						if (lastPart.length() > 0
								&& (!lastPart.contains("dbo:")
										|| !lastPart.contains("dbr:")
										|| lastPart.contains("@")
										|| lastPart.contains("^^") || parts[parts.length - 2]
										.contains("wikiPageExternalLink"))) {
							// for (int i = 0; i < parts.length - 2; i++) {
							// newLine += parts[i] + "->";
							// }
							
							newLine = line.substring(0, line.lastIndexOf("->"));
							newLine = newLine.substring(0,
									newLine.lastIndexOf("->"));
							if (lastPart.contains("^^"))
							{
								newLine = newLine+ " " + lastPart.substring(0,lastPart.lastIndexOf("^")-1) + " ";
							}
							
							if (lastPart.contains("@"))
							{
								newLine = newLine + " " + lastPart.substring(0,lastPart.lastIndexOf("@")) + " ";
							}	

						} else {
							newLine = line;
						}
						if (!seenPaths.containsKey(newLine)) {
							seenPaths.put(newLine, 1);
							newLine = newLine.replace("http://xmlns.com/foaf/0.1/name ", "");
							newLine = newLine.replace("http://xmlns.com/foaf/0.1/homepage", "");
							newLine = newLine.replace("http://www.w3.org/2002/07/owl#differentFrom", "different from ");
							
							newLine = newLine.replace("http://xmlns.com/foaf/0.1/nick", "nick");
							newLine = newLine.replace("rdfs:seeAlso", "");

						
							newLine = newLine.replace("->", " ");
							newLine = newLine.replace("  ", " ");
							
					/*		StringTokenizer tokenLine = new StringTokenizer (newLine);
							String current = "";
							newLine = "";
							while (tokenLine.hasMoreElements())
							{
								current = tokenLine.nextToken();
							if (current.contains("dbo:"))
							{
								current = current.replace("dbo:", "");
								char[] linechar = current.toCharArray();
								current = "";
								for (int i=0;i<linechar.length;i++)
								{
									if (Character.isUpperCase(linechar[i]))
										newLine = newLine + " " + Character.toLowerCase(linechar[i]);
									else
										newLine = newLine + linechar[i];
								}
								
								newLine = newLine + " ";
							}
							else if (current.contains("dbr:"))
							{
								if (current.contains("\\("))
								{
									current = current.replace("\\(", " ");
									current = current.replace("\\)", "");
								}
								
								String copyDbr;
								
								if (current.contains("_"))
									copyDbr = current.replaceAll("_", " ");
								else
									copyDbr = current;
								
								StringTokenizer tokencopyDbr = new StringTokenizer (copyDbr);
								while (tokencopyDbr.hasMoreElements())
								{
									String str = tokencopyDbr.nextToken();
									
									newLine = newLine + str + " ";
								}
								
								if (current.contains("_"))
									newLine = newLine + current + " ";
								
							}
							}
							
							newLine = newLine.replace("dbr:", "");
							*/
							//newLine = newLine.replace("_", " ");
							newLine = newLine.replace("->", " ");
							newLine = newLine.replace("  ", " ");
						//	System.out.println(newLine);

							/*if (!headLine.isEmpty())
							{
								if (!newLine.contains(headLine))
								{
									writer.write("\n");
									StringTokenizer tokenLines = new StringTokenizer (newLine);
									headLine = tokenLines.nextToken();
								}
							}
							else
							{
								StringTokenizer tokenLines = new StringTokenizer (newLine);
								headLine = tokenLines.nextToken();
							}*/
							//newLine = relationPerLineConversion(newLine);
							//writer.write(newLine.replace("->", " ") + "\n");
						/*	newLine = newLine.replace("\\(", "");
							newLine = newLine.replace("\\)", "");
							newLine = newLine.replace("\\,", "");*/
							newLine = newLine.replace("  ", " ");
							writer.write(newLine + " ");
							newLine = "";
						//	System.out.println(newLine);
						}

					} catch (Exception e) {
						// TODO: handle exception
					}
					
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}
	}

	private static String relationPharseConvertion (String str)
	{
		StringTokenizer token = new StringTokenizer (str);
		String newStr = "";
		int count = 0;
		while (token.hasMoreElements())
		{
			String current = token.nextToken();
			if (current.contains("dbo:"))
			{
				newStr = newStr + "_" + current + "_";
				count++;
			}
			else
			{
				newStr = newStr + current;
				count++;
			}
			if (count==3)
			{
				newStr = newStr + " " + current;
				count = 1;
			}
		}
		return newStr;
	}
	
	private static String relationPerLineConversion (String str)
	{
		StringTokenizer token = new StringTokenizer (str);
		String newStr = "";
		int count = 0;
		while (token.hasMoreElements())
		{
			String current = token.nextToken();
			if (current.contains("dbr:"))
			{
				if (newStr.isEmpty())
					newStr = newStr + current;
				else
					newStr = newStr + " " + current;
				    count++;
			}
			else
			{
				newStr = newStr + " " + current;
			}
			
			if (count==2)
			{
				if (token.hasMoreTokens())
				{
					newStr = newStr + "\n" + current;
					count = 1;
				}
			}
		}
		return newStr;
	}
	
	public static void main(String[] args) {

	//	cleanPaths(args[0], args[1]);
		 cleanPaths("/home/souza/rdf2vec/walkslcquadNew/",
		 "/home/souza/rdf2vec/output/");
	}
}
