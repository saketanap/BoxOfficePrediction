package com.mit.dmpa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class PredictionAlgo {
	
	
	public List<Status> fetchTweets(String searchQuery, int nTweets) 
	{
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setOAuthConsumerKey("Y7Rx4oL24U0pmYSA4sjwF99w4");
		cb.setOAuthConsumerSecret("FwSg5qhtNmz40jp8OEBumqaqgsQjDrTdSOUjlRFp6yiNi59yr5");
		cb.setOAuthAccessToken("3140037746-qDR7FEpQzLmXDRc8Kd44wbDiKxXUPl5YBSB7lPw");
		cb.setOAuthAccessTokenSecret("FrXtp6D5hBHzECkM8QfTgYA8RQS4PJNRtA34jR0HV3d7I");

		Twitter twitter = new TwitterFactory(cb.build()).getInstance();
		Query query = new Query(searchQuery);
		int numberOfTweets = nTweets;
		long lastID = Long.MAX_VALUE;
		List<Status> tweets = new ArrayList<Status>();
		
		while (tweets.size () < numberOfTweets) 
		{
			if (numberOfTweets - tweets.size() > 100)
				query.setCount(100);
		    else 
		    	query.setCount(numberOfTweets - tweets.size());
		    try 
		    {
		    	QueryResult result = twitter.search(query);
		    	tweets.addAll(result.getTweets());
		    	
		    	System.out.println("Gathered " + tweets.size() + " tweets");
		    	
		    	for (Status t: tweets) 
		    		if(t.getId() < lastID) lastID = t.getId();
		    }

		    catch (TwitterException te) {
		    	System.out.println("Couldn't connect: " + te);
		    }; 
		    
		    query.setMaxId(lastID-1);
		}
		  
		return tweets;
	}
	
	public double scaleNumber(double number, double toMax, double toMin, double fromMax, double fromMin)
	{
		return (number - fromMin) * (toMax - toMin) / (fromMax - fromMin) + toMin;
		//return (number - 1) * (max - min) / (1 - 0) + min;
		
	}
	
	public String sentAnalysis(String msg)
	{
		String[] keywords = {"bad", "worse", "fail", "awful", "hate", "dislike"};
		
		for(int i = 0; i < keywords.length; i++)
		{
			if(msg.contains(keywords[i]))
			{
				return "neg";
			}
		}
		
		return "pos";
	}
	
	public static void main(String[] args) {
		
		final String movieName = "#baaghi";
		final int TOTAL_TWEETS = 1024;
		double alpha = 0.0;
		double thresholdValue = 0.0;
		double hype = 0.0;
		double filmReleaseScreenNo = 2000.0;
		double meanFullHouseCollection = 72000;
		double openingBoxOfficeCollection = 0.0;
		double scaleMax = 1.0;
		double scaleMin = 0.1;
		double maxFollowers = 0.0;
		double minFollowers = 0.0;
		
		
		PredictionAlgo algo = new PredictionAlgo();
		List<Status> tweets = algo.fetchTweets(movieName, TOTAL_TWEETS);
		SentimentClassifier sentClassifier = new SentimentClassifier();
		HashMap<String, Integer> distinctUsers = new HashMap<>();

		
		for (int i = 0; i < tweets.size(); i++) 
		{
		    Status t = (Status) tweets.get(i);
		    String user = t.getUser().getScreenName();
		    String msg = t.getText();
		    String sent = sentClassifier.classify(msg);
		    
		    System.out.println(i + " USER: " + user + " wrote: " + msg + " Sentiment: " +sent);
		    
		    if (!sent.equals("neg"))
		    {
		    	Integer defCount = distinctUsers.get(user);
		    	if(defCount != null)
		    		continue;
		    	
		    	int fCount = t.getUser().getFollowersCount();
		    	if(fCount == 0)
		    		fCount = 1;
		    	distinctUsers.put(user, fCount);
		    	maxFollowers = t.getUser().getFollowersCount();
		    	minFollowers = maxFollowers;
		    }
		}
		
		alpha = distinctUsers.size()/(double)TOTAL_TWEETS;

		for(Entry<String, Integer> m:distinctUsers.entrySet())
	    {  
	    	thresholdValue += m.getValue();
	    	
	    	if(m.getValue()> maxFollowers)
	    		maxFollowers = m.getValue();
	    	if(m.getValue()<minFollowers)
	    		minFollowers = m.getValue();
	    	
	    }
		
		
		System.out.println(maxFollowers+":"+minFollowers);
		
		thresholdValue /= distinctUsers.size();
		
		maxFollowers = (maxFollowers-thresholdValue)/maxFollowers;
		minFollowers = (minFollowers-thresholdValue)/minFollowers;
		
		for(Entry<String, Integer> m:distinctUsers.entrySet())
	    {  
	    	double sigma = 0.0;
	    	sigma = m.getValue() - thresholdValue;
	    	sigma /= m.getValue();
	    	
	    	double downScale = algo.scaleNumber(sigma, scaleMax, scaleMin, maxFollowers, minFollowers);	
	    	System.out.println("Sigma value:"+downScale);
	    	
	    	hype += (alpha + downScale)/2;    	
	    }
		
		hype /= distinctUsers.size();
		
		System.out.println();
		System.out.println("Distinct users with follower count:");
		for(Entry<String, Integer> m:distinctUsers.entrySet())
	    {  
	    	   System.out.println(m.getKey()+": "+m.getValue());
	    }
		
		System.out.println();
		System.out.println("Value Range:"+maxFollowers+":"+minFollowers);
		System.out.println("Alpha:"+alpha);
		System.out.println("Threshold:"+thresholdValue);
		System.out.println("Hype:"+hype);
		
		openingBoxOfficeCollection = filmReleaseScreenNo*hype*meanFullHouseCollection;
		
		System.out.println();
		System.out.println("Pridicted Box Office Collection:"+openingBoxOfficeCollection);
		
		
	}
}
