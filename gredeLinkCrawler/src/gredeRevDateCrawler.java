import java.io.*;
import java.util.Scanner;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class gredeRevDateCrawler {

	//Root URL
	static String urlNoDoc = "http://rdweb01/Portal/Documents/Default.aspx?DocID=";
	
	public static void main(String[] args) throws IOException {		
		
		//Create csv File writer and affix header
		FileWriter writer = new FileWriter("output.csv");
		writer.append("Document ID, Document Title, Obsolete, Month, Day, Year \n");
		
		//take user input to determine how many documents should be scanned
		Scanner scanner = new Scanner(System.in);
		System.out.print("Enter maximum document ID: ");
		
		//wait for user input
		int maxDocID = scanner.nextInt();
		
		System.out.println("\n");
		
		//Iterate over all document IDs to pull out metadata
		for(int docID = 1000; docID <= maxDocID; docID++)
		
			try {
				
				//combine root url with destination document
				String docIDstring = Integer.toString(docID);
				String urlConcatenated = urlNoDoc + docIDstring;
				
				//connect to document via HTTP get request
				Document doc = Jsoup.connect(urlConcatenated).get();
				
				//pull out just the table with the main data
				//ignoring the html metadata
				Element outerTable = doc.select("table").first();
				
				
				//if the procedure is blank but still returns, skip it
				if(outerTable == null) {
					VWI vwi = new VWI(docID, "BLANK", 0, 0, 0, false);
					//vwiList.add(test);
					addToFile(writer, vwi);
					vwi.print();
					continue;
				}
				
				//pull a list of Table Row HTML elements
				Elements rows = doc.getElementsByTag("tr");				
				
				//get text from first tr row
				String header = rows.get(0).text();
				
				
				//split row to pull out title
				String[] data = header.split("Revision Date");
				String title = data[0];
				boolean obsolete = title.toLowerCase().contains("obsolete");
				
				//split date portion to find date string
				String[] dateArraySplitBySpaces = data[1].split(" ");
							
				//make sure the rev date isn't empty
				try {
					
					//save date string xx/xx/xxxx
					String dateString = dateArraySplitBySpaces[2];
					
					//separate the dateString by slashes to pull out day, month, year
					String[] dateArraySplitBySlashes = dateString.split("/");
					
					//create new VWI object and store
					VWI vwi = new VWI(
							docID,
							title, 
							Integer.parseInt(dateArraySplitBySlashes[0]), 
							Integer.parseInt(dateArraySplitBySlashes[1]), 
							Integer.parseInt(dateArraySplitBySlashes[2]),
							obsolete);
					addToFile(writer, vwi);			
					vwi.print();
					
					
				} catch (IndexOutOfBoundsException e) {
					//case where rev date is null and can't be parsed 
					//create new VWI object and store
					VWI vwi = new VWI(docID, title, 0, 0, 0, obsolete);
					addToFile(writer, vwi);		
					vwi.print();
				}
						
				
			} catch (IOException e) {
				//case where document returns with HTTP error
				//create new VWI object and store
				VWI vwi = new VWI(
						docID,
						"PAGE DID NOT RETURN", 
						0,
						0,
						0,
						false);
				addToFile(writer, vwi);			
				vwi.print();
			}
		
		//close the FileWriter
		writer.flush();
		writer.close();
		
		//write to the console to display completion status
		System.out.println("\n*** COMPLETE ***");
		
		
	} //end main method
	
	
	//write VWI metadata to csv file
	public static void addToFile(FileWriter writer, VWI vwi) throws IOException {
		writer.append(Integer.toString(vwi.getDocID()));
		writer.append(",");
		writer.append("\"" + vwi.getTitle() + "\"");
		writer.append(",");
		writer.append(vwi.getObsolete());
		writer.append(",");
		writer.append(Integer.toString(vwi.getMonth()));
		writer.append(",");
		writer.append(Integer.toString(vwi.getDay()));
		writer.append(",");
		writer.append(Integer.toString(vwi.getYear()));
		writer.append("\n");
		
	}
	
} //end class
