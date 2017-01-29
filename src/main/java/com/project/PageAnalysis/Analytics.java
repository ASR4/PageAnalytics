package com.project.PageAnalysis;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.project.Client.Client;

public class Analytics {
	
	String numOfPosts = "100";
	Client client = new Client();
	DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
	Date dateobj = new Date();
	
	public HttpResponse requestUntilSucceed(String url) throws ClientProtocolException, IOException{
		boolean failure = true;
		HttpResponse response = null;
		while(failure){
			response = client.apiResponse(url);
			if(response.getStatusLine().getStatusCode() == 200){
				failure = false;
			}else{
				System.out.println("Error in fetching URL : " + url + " Date/Time : " + dateobj);
				System.out.println("Retrying");
			}
		}
		return response;
	}
	
	public JSONObject convertResponseToJson(HttpResponse response) throws ParseException, IOException, JSONException{
		String responseString = EntityUtils.toString(response.getEntity());
		JSONObject json = new JSONObject(responseString);
		
		return json;
	}
	
	public JSONObject getFacebookPageFeedData(String pageId, String accessToken, String numOfPosts) throws ClientProtocolException, IOException, ParseException, JSONException{
		
		//eBay/posts/?fields=message,link,created_time,type,name,id,comments.limit(0).summary(true),shares,reactions.limit(0).summary(true)&limit=100&access_token=921691361299872|9155a6296f33008e9bfcca3e6536db00
		//eBay/posts/?fields=message,link,created_time,type,name,id,comments.limit(0).summary(true),shares,reactions.limit(0).summary(true)&limit=100&access_token=921691361299872|9155a6296f33008e9bfcca3e6536db00
		String base = "https://graph.facebook.com/v2.8"; //CHECK THE VERSION NUMBER
		String node = "/" + pageId + "/posts";
		String fields = "/?fields=message,link,created_time,type,name,id,comments.limit(0).summary(true),shares,reactions.limit(0).summary(true)";
		String parameters = "&limit=" + numOfPosts + "&access_token=" + accessToken;

		String url = base + node + fields + parameters;

		HttpResponse response = requestUntilSucceed(url);
		
		return convertResponseToJson(response);
	}

	public JSONObject getReactionsForPost(String postId, String accessToken) throws ClientProtocolException, IOException, ParseException, JSONException{

		//185499393135_10155696494578136/?fields=reactions.type(LIKE).limit(0).summary(total_count).as(like),reactions.type(LOVE).limit(0).summary(total_count).as(love),reactions.type(WOW).limit(0).summary(total_count).as(wow),reactions.type(HAHA).limit(0).summary(total_count).as(haha),reactions.type(SAD).limit(0).summary(total_count).as(sad),reactions.type(ANGRY).limit(0).summary(total_count).as(angry)&access_token=21691361299872|9155a6296f33008e9bfcca3e6536db00

		String base = "https://graph.facebook.com/v2.8"; //CHECK THE VERSION NUMBER
		String node = "/" + postId;
		String reactions = "/?fields=reactions.type(LIKE).limit(0).summary(total_count).as(like)" +
	            ",reactions.type(LOVE).limit(0).summary(total_count).as(love)" +
	            ",reactions.type(WOW).limit(0).summary(total_count).as(wow)" +
	            ",reactions.type(HAHA).limit(0).summary(total_count).as(haha)" +
	            ",reactions.type(SAD).limit(0).summary(total_count).as(sad)" +
	            ",reactions.type(ANGRY).limit(0).summary(total_count).as(angry)";
	    String parameters = "&access_token=" + accessToken;
	    
	    String url = base + node + reactions + parameters;

	    HttpResponse response = requestUntilSucceed(url);
	    
	    return convertResponseToJson(response);       
	}
	
	public ArrayList<String> processFacebookPageFeedPosts(JSONObject post, String accessToken) throws java.text.ParseException, JSONException, ClientProtocolException, ParseException, IOException{
		ArrayList<String> result = new ArrayList<String>();

		String id = post.getString("id");
		String message = "";
		String link = "";
		
		if(post.has("message")){
			message = post.getString("message");
		}
		
		if(!post.getString("link").isEmpty()){
			link = post.getString("link");
		} 
		
		String type = post.getString("type");
		String name = "";
		if(post.has("name")){
			name = post.getString("name");
		}

		String originalDateTime = post.getString("created_time");
		String[] dateTime = originalDateTime.split("T");
		String originalDate = dateTime[0];
		String[] time = dateTime[1].split("\\+");
		String originalTime = time[0];

		String publishDateTime = originalDate + " " +  originalTime;

		//SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    //Date date = format.parse(modifiedDateTime);

		//to get num of reactions
		String numOfReactions = String.valueOf(post.getJSONObject("reactions").getJSONObject("summary").getInt("total_count"));			
		String numOfComments = String.valueOf(post.getJSONObject("comments").getJSONObject("summary").getInt("total_count"));
		String numOfShares = "";
		
		if(post.has("shares")){
			numOfShares = String.valueOf(post.getJSONObject("shares").getInt("count"));
		}else{
			numOfShares = "0";
		}
		
		String likes = "";
		String loves = "";
		String wows = "";
		String hahas = "";
		String sads = "";
		String angrys = "";
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date publishDateTimeStandard = format.parse(publishDateTime);
		//Time from when reactions started on FB
		Date startDateTimeStandard = format.parse("2016-02-24 00:00:00");
		
		if(publishDateTimeStandard.after(startDateTimeStandard)){
			JSONObject reactions = getReactionsForPost(id, accessToken);
			
			if(reactions.getJSONObject("like") == null){
				likes = "0";
			}else{
				likes = String.valueOf(reactions.getJSONObject("like").getJSONObject("summary").getInt("total_count"));
			}
			loves = getTotalNumOfReactions("love", reactions);
	    	wows = getTotalNumOfReactions("wow", reactions);
	    	hahas = getTotalNumOfReactions("haha", reactions);
	    	sads = getTotalNumOfReactions("sad", reactions);
	    	angrys = getTotalNumOfReactions("angry", reactions);
		}else{
			likes = numOfReactions;
		}
		result.add(id);
		result.add(message);
		result.add(name);
		result.add(type);
		result.add(link);
		result.add(publishDateTime);
		result.add(numOfReactions);
		result.add(numOfComments);
		result.add(numOfShares);
		result.add(likes);
		result.add(loves);
		result.add(wows);
		result.add(hahas);
		result.add(sads);
		result.add(angrys);

		return result;
	}

	public String getTotalNumOfReactions(String reactionType, JSONObject reactions) throws JSONException{
		if(reactions.getJSONObject(reactionType) == null){
			return "0";
		}else{
			return String.valueOf(reactions.getJSONObject(reactionType).getJSONObject("summary").getInt("total_count"));
		}
	}
	
	public void scrapeFacebookPageFeedStatus(String pageId, String accessToken){
		//Delimiter used in CSV file
	    String COMMA_DELIMITER = ",";
	    String NEW_LINE_SEPARATOR = "\n";
	    
	    //CSV file header
	    String FILE_HEADER = "id,message,name,type,link,publishedDateTime,numOfReactions,numOfComments,numOfShares,likes,loves,wows,hahas,sads,angrys";

	    FileWriter fileWriter = null;
	    String fileName = "fb_data";
	    boolean hasNextPage = true;
	    int numProcessed = 0;

	    try{
	    	fileWriter = new FileWriter(fileName);
	    	//Write the CSV file header
	        fileWriter.append(FILE_HEADER);
			//Add a new line separator after the header
	        fileWriter.append(NEW_LINE_SEPARATOR);
	        JSONObject json = getFacebookPageFeedData(pageId, accessToken, numOfPosts);

	        while(hasNextPage){
	        	JSONArray data = json.getJSONArray("data");
	        	for(int i = 0; i < data.length(); i++){
	        		JSONObject post = data.getJSONObject(i);
	        		ArrayList<String> list = processFacebookPageFeedPosts(post,accessToken);
	        		for(String l : list){
	        			fileWriter.append(l);
	        			fileWriter.append(COMMA_DELIMITER);
	        		}
	        		fileWriter.append(NEW_LINE_SEPARATOR);
	        		numProcessed++;
	        		//For printing purposes
	        		DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
	        		Calendar calobj = Calendar.getInstance();
	        		System.out.println(post.getString("id") + "Num processed : " + numProcessed +  " Processed at: " + df.format(calobj.getTime()));
	       	 	}

	       	 	if(json.has("paging")){
	       	 		json = convertResponseToJson(requestUntilSucceed(json.getJSONObject("paging").getString("next")));
	       	 	}else{
	       	 		hasNextPage = false;
	       	 	}

	        }

	    }catch (Exception e) {
	            System.out.println("Error in CsvFileWriter !!!");
	            e.printStackTrace();
	    } finally {
	        try {
	        fileWriter.flush();
	        fileWriter.close();
	        } catch (IOException e) {
	            System.out.println("Error while flushing/closing fileWriter !!!");
	            e.printStackTrace();
	        }
	    }

	}
	
	public static void main(String[] args){
		Analytics obj = new Analytics();
		String accessToken = null;
		try {
			accessToken = "921691361299872" + URLEncoder.encode("|", "UTF-8") + "9155a6296f33008e9bfcca3e6536db00";
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String pageId = "eBay";
		obj.scrapeFacebookPageFeedStatus(pageId, accessToken);
	}
}
