package com.twitter.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Base64;

import com.twitter.util.TwitterAPIConstant;

public class TwitterAPIService {
	private String createBasicAuth() throws UnsupportedEncodingException {
		String consumer_key = TwitterAPIConstant.consumer_key;
		String consumer_secret = TwitterAPIConstant.consumer_secret;

		consumer_key = URLEncoder.encode(consumer_key, "UTF-8");
		consumer_secret = URLEncoder.encode(consumer_secret, "UTF-8");

		String authorization_header_string = consumer_key + ":"
				+ consumer_secret;
		byte[] encoded = Base64.getEncoder().encode(
				authorization_header_string.getBytes());

		return new String(encoded); // converting byte to string
	}

	private HttpURLConnection createBearerTokenConnection() throws IOException {
		URL url = new URL(TwitterAPIConstant.authUrl);
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("Content-Type",
				"application/x-www-form-urlencoded;charset=UTF-8");
		connection.setRequestProperty("Authorization", "Basic "
				+ createBasicAuth());
		connection.setRequestMethod("POST");
		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		String formData = "grant_type=client_credentials";
		byte[] formDataInBytes = formData.getBytes("UTF-8");
		OutputStream os = connection.getOutputStream();
		os.write(formDataInBytes);
		os.close();
		return connection;
	}

	private HttpURLConnection createTwitterAPIConnection(int feedCount,
			String screenName) throws IOException {
		String apiUri = TwitterAPIConstant.apiUri;
		StringBuilder apiUrl = new StringBuilder();
		apiUrl.append(apiUri);
		apiUrl.append(TwitterAPIConstant.apiUriQueryParam_1 + screenName
				+ TwitterAPIConstant.apiUriQueryParam_2 + feedCount);
		URL url = new URL(apiUrl.toString());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestProperty("Authorization", "Bearer "
				+ retrieveAccessToken());
		connection.setDoOutput(true);
		connection.setRequestMethod("GET");
		connection.connect();
		return connection;
	}

	public String retrieveAccessToken() {
		StringBuffer accessToken = new StringBuffer();
		try {
			HttpURLConnection connection = new TwitterAPIService()
					.createBearerTokenConnection();

			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String inputLine;

			while ((inputLine = in.readLine()) != null) {
				accessToken.append(inputLine);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return accessToken.toString().substring(
				accessToken.toString().indexOf("access_token") + 15,
				accessToken.toString().length() - 2);
	}

	public String retrieveAPIFeeds(int feedCount, String screenName) {
		StringBuffer feedData = new StringBuffer();
		try {
			HttpURLConnection connection = createTwitterAPIConnection(
					feedCount, screenName);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				feedData.append(inputLine);
			}
			in.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(feedData.toString());
		return feedData.toString();
	}

	public static void main(String[] args) {
		new TwitterAPIService().retrieveAPIFeeds(10, "salesforce");
	}
}
