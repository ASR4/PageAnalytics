package com.project.Client;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

public class Client {
public HttpResponse apiResponse(String url) throws ClientProtocolException, IOException{
	//final String USER_AGENT = "Mozilla/5.0";
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);
		
		//request.addHeader("User-Agent", USER_AGENT);
		HttpResponse response = client.execute(request);
		
		return response;
	}
}
