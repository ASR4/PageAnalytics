package com.project.PageAnalysis;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.project.Client.Client;

public class Analytics {
	String accessToken = "";
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
	
	public JSONObject getFacebookPageFeedData(pageId, accessToken, numOfStatuses){
		
		//eBay/posts/?fields=message,link,created_time,type,name,id,comments.limit(0).summary(true),shares,reactions.limit(0).summary(true)&limit=100&access_token=921691361299872|9155a6296f33008e9bfcca3e6536db00

		String base = "https://graph.facebook.com/v2.6"; //CHECK THE VERSION NUMBER
		String node = "/" + pageId + "/posts";
		String fields = "/?fields=message,link,created_time,type,name,id,comments.limit(0).summary(true),shares,reactions.limit(0).summary(true)";
		String parameters = "&limit=" + numOfStatuses + "&access_token=" + accessToken;

		String url = base + node + fields + parameters;

		HttpResponse response = requestUntilSucceed(url);
		
		return convertResponseToJson(response);
	}

	public JSONObject getReactionsForStatus(statusId, accessToken){

		//185499393135_10155696494578136/?fields=reactions.type(LIKE).limit(0).summary(total_count).as(like),reactions.type(LOVE).limit(0).summary(total_count).as(love),reactions.type(WOW).limit(0).summary(total_count).as(wow),reactions.type(HAHA).limit(0).summary(total_count).as(haha),reactions.type(SAD).limit(0).summary(total_count).as(sad),reactions.type(ANGRY).limit(0).summary(total_count).as(angry)&access_token=21691361299872|9155a6296f33008e9bfcca3e6536db00

		String base = "https://graph.facebook.com/v2.6"; //CHECK THE VERSION NUMBER
		String node = "/" + statusId;
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
	
	public ArrayList<String> processFacebookPageFeedStatus(JSONObject post, String accessToken){
		
		return null;
	}
}
