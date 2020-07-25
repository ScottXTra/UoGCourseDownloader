import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
class courseSection{
	String term;
	boolean open;
	String sectionTitle;
	String location;
	String meetingInformation;
	String professor;
	String availableAndCapacity;	
	String credits;
	String academicLevel;
	
}
public class Scraper {
	static String BASE_COOKIE = "LASTTOKEN=69; BIGipServerWEBADVISOR=106893740.20480.0000; path=/; Httponly; Secure";
	static String SESSION_COOKIE =""; 
	//Downloads the data from The University of Guelph Webadvisor server
	public static ArrayList<courseSection> getAllSections() throws IOException
	{
		//Get main cookie
        URL obj = new URL("https://webadvisor.uoguelph.ca/WebAdvisor/WebAdvisor?TOKENIDX=69");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.addRequestProperty("Cookie", BASE_COOKIE);
        con.setInstanceFollowRedirects( false );
        SESSION_COOKIE = BASE_COOKIE +"; "+ con.getHeaderFields().get("Set-Cookie").get(1);
        System.out.println("Getting base cookie...");
        obj = new URL("https://webadvisor.uoguelph.ca/WebAdvisor/WebAdvisor?TOKENIDX=69&type=M&constituency=WBST&pid=CORE-WBST");
        con = (HttpURLConnection) obj.openConnection();
        con.addRequestProperty("Cookie", SESSION_COOKIE);
        con.setInstanceFollowRedirects( true );
        SESSION_COOKIE = BASE_COOKIE +"; "+ con.getHeaderFields().get("Set-Cookie").get(1);
        System.out.println("Loading main page...");
        obj = new URL("https://webadvisor.uoguelph.ca/WebAdvisor/WebAdvisor?TOKENIDX=69&CONSTITUENCY=WBST&type=P&pid=ST-WESTS12A");
        con = (HttpURLConnection) obj.openConnection();
        con.addRequestProperty("Cookie", SESSION_COOKIE);
        con.setInstanceFollowRedirects( true );
        SESSION_COOKIE = BASE_COOKIE +"; "+ con.getHeaderFields().get("Set-Cookie").get(1);
        System.out.println("Requesting all courses...");
        String urlParameters  = "VAR1=F20&DATE.VAR1=&DATE.VAR2=&LIST.VAR1_CONTROLLER=LIST.VAR1&LIST.VAR1_MEMBERS=LIST.VAR1*LIST.VAR2*LIST.VAR3*LIST.VAR4&LIST.VAR1_MAX=5&LIST.VAR2_MAX=5&LIST.VAR3_MAX=5&LIST.VAR4_MAX=5&LIST.VAR1_1=&LIST.VAR2_1=&LIST.VAR3_1=&LIST.VAR4_1=&LIST.VAR1_2=&LIST.VAR2_2=&LIST.VAR3_2=&LIST.VAR4_2=&LIST.VAR1_3=&LIST.VAR2_3=&LIST.VAR3_3=&LIST.VAR4_3=&LIST.VAR1_4=&LIST.VAR2_4=&LIST.VAR3_4=&LIST.VAR4_4=&LIST.VAR1_5=&LIST.VAR2_5=&LIST.VAR3_5=&LIST.VAR4_5=&VAR7=05%3A00&VAR8=&VAR3=&VAR6=&VAR21=&VAR9=&RETURN.URL=https%3A%2F%2Fwebadvisor.uoguelph.ca%2FWebAdvisor%2FWebAdvisor%3FTOKENIDX%3D9151692409%26type%3DM%26constituency%3DWBST%26pid%3DCORE-WBST&SUBMIT_OPTIONS=";
		byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8 );
		obj = new URL("https://webadvisor.uoguelph.ca/WebAdvisor/WebAdvisor?TOKENIDX=69&SS=1&APP=ST&CONSTITUENCY=WBST" );
		con = (HttpURLConnection) obj.openConnection();
		con.setDoOutput( true );
		con.setInstanceFollowRedirects( false );
		con.setRequestMethod( "POST" );
		con.setRequestProperty("Cookie",SESSION_COOKIE);
		try( DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {wr.write( postData );}catch (Exception e){}
		SESSION_COOKIE = BASE_COOKIE +"; "+ con.getHeaderFields().get("Set-Cookie").get(1);
		System.out.println("Downloading all courses...");
		obj = new URL("https://webadvisor.uoguelph.ca/WebAdvisor/WebAdvisor?TOKENIDX=69&SS=2&APP=ST&CONSTITUENCY=WBST" );
		con = (HttpURLConnection) obj.openConnection();
		con.setDoOutput( true );
		con.setInstanceFollowRedirects( false );
		con.setRequestProperty("Cookie",SESSION_COOKIE);
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        parser(response.toString());
        return null;
        
	}
	private static void parser(String rawData) throws IOException{
		System.out.println("Parsing data...");
		Document doc = Jsoup.parse(rawData);
		Element mainTable = doc.getElementsByTag("table").get(2);
		List<Element> rows = mainTable.getElementsByTag("tr");
		ArrayList<courseSection> rtn = new ArrayList<courseSection>();
		for(int i = 2; i < rows.size(); i++) {
			courseSection tmpSection = new courseSection();
			tmpSection.sectionTitle = rows.get(i).getElementById("SEC_SHORT_TITLE_"+(i-1)).text().replace("\t", "");
			tmpSection.location = rows.get(i).getElementById("SEC_LOCATION_"+(i-1)).text().replace("\t", "");;
			tmpSection.meetingInformation = rows.get(i).getElementsByAttributeValue("name","SEC.MEETING.INFO_"+(i-1)).val().replace("\t", "");;
			tmpSection.professor = rows.get(i).getElementById("SEC_FACULTY_INFO_"+(i-1)).text().replace("\t", "");;
			tmpSection.availableAndCapacity = rows.get(i).getElementById("LIST_VAR5_"+(i-1)).text().replace("\t", "");;
			tmpSection.credits = rows.get(i).getElementById("SEC_MIN_CRED_"+(i-1)).text().replace("\t", "");;
			tmpSection.academicLevel = rows.get(i).getElementById("SEC_ACAD_LEVEL_"+(i-1)).text().replace("\t", "");;
			rtn.add(tmpSection);
		}
		writeToDisk(rtn);
	}
	//
	public static void writeToDisk(ArrayList<courseSection> sections) throws IOException {
		System.out.println("Writing data to disk...");
		FileWriter csvWriter = new FileWriter("sections.tsv");
		
		csvWriter.append("sectionTitle");
		csvWriter.append("\t");
		csvWriter.append("location");
		csvWriter.append("\t");
		csvWriter.append("meetingInformation");
		csvWriter.append("\t");
		csvWriter.append("professor");
		csvWriter.append("\t");
		csvWriter.append("availableAndCapacity");
		csvWriter.append("\t");
		csvWriter.append("credits");
		csvWriter.append("\t");
		csvWriter.append("academicLevel");
		csvWriter.append("\n");
		
		for(courseSection tmp : sections) {
			csvWriter.append(tmp.sectionTitle);
			csvWriter.append("\t");
			csvWriter.append(tmp.location);
			csvWriter.append("\t");
			csvWriter.append(tmp.meetingInformation);
			csvWriter.append("\t");
			csvWriter.append(tmp.professor);
			csvWriter.append("\t");
			csvWriter.append(tmp.availableAndCapacity);
			csvWriter.append("\t");
			csvWriter.append(tmp.credits);
			csvWriter.append("\t");
			csvWriter.append(tmp.academicLevel);
			csvWriter.append("\n");
		}
		csvWriter.flush();
		csvWriter.close();
	}
}
