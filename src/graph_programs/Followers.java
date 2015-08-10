package graph_programs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

import twitter4j.*;
import twitter4j.auth.AccessToken;

public class Followers implements Serializable {	
	/**
	 * For serializing and ensuring class safety when de-serializing
	 */
	private static final long serialVersionUID = 1L;
	/* returns long[] of IDs from the seed text file.
	 * returns first 5000 ids first, if that many. If more, uses cursor to get next ~5000 ids.
	 * repeat until hasNext() returns false, then moves on to next name. 
	 * null == no more ids.
	 */
	int currentSeedHandleNum = 1;
	long nextCursor = -1;
	
	transient int showUsersRateLimit;
	transient int getFollowIDsRateLimit;
	transient int lookupUsersRateLimit;
	
	int lookupLimit;
	
	public void debug_print() {
		System.out.println("currSeedHandleNum = " + Integer.toString(currentSeedHandleNum));
		System.out.println("nextCursor = " + Long.toString(nextCursor));
		System.out.println("showUsersRateLimit = " + Integer.toString(showUsersRateLimit));
		System.out.println("lookupLimit = " + Integer.toString(lookupLimit));
	}
	
	public Followers() {
		initializeRates();
		lookupLimit = -1;
	}
	
	// 4 sleeps per hr * x hrs // hanging problem: doesn't run overnight
	public Followers(int getUsersL) {
		initializeRates();
		lookupLimit = getUsersL; 
	}

	public long[] getIDs() {
		Twitter twitter = new TwitterFactory().getInstance();
		
		twitter.setOAuthConsumer("KOmMvDbWuOkg7IzGhn5DT6NLH", 
				"dalWrw35UOFAD2N2um4pxpj7C04QUzYSvEO1nrM0CU8xwxxznB");
		AccessToken accessToken = new AccessToken("53612356-eWirp6zij2dvHe4KS88bWnMLnMMdU1lUxKjCf5HJN", 
				"BQBSk6p9Jm3aWhRydfK0hgj0TD8GnZYzZy3Mvfsa7Oen0");
		twitter.setOAuthAccessToken(accessToken);
		
		String s = getNextName(currentSeedHandleNum);
		if (s.equals("")) return null;
		
		try {
//			System.out.println("\t" + getFollowIDsRateLimit);
			
			checkSleep("showUser");
			checkSleep("getFollowerIDs");
			
			User user = twitter.showUser(s);
			IDs followerIDs = twitter.getFollowersIDs(user.getId(), nextCursor, 5000);
			
			showUsersRateLimit--;
			getFollowIDsRateLimit--;
			
			if (followerIDs.hasNext()) nextCursor = followerIDs.getNextCursor();
			else {
				currentSeedHandleNum++;
				nextCursor = -1;
			}
			
			return followerIDs.getIDs();
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public LinkedList<long[]> parseIDMass(long[] ids) {
		LinkedList<long[]> parsedIDs = new LinkedList<long[]>();
		int i;
		
		for (i = 0; i < ids.length / 100; i++) {
			parsedIDs.add(Arrays.copyOfRange(ids, 0 + (i * 100), 100 + (i * 100)));
		}
		
		if (ids.length % 100 != 0) parsedIDs.add(Arrays.copyOfRange(ids, i * 100, i * 100 + (ids.length % 100)));
		
		return parsedIDs;
	}
	
	public ResponseList<User> getUsersArray(long[] ids) {
		Twitter twitter = new TwitterFactory().getInstance();
		
		twitter.setOAuthConsumer("KOmMvDbWuOkg7IzGhn5DT6NLH", 
				"dalWrw35UOFAD2N2um4pxpj7C04QUzYSvEO1nrM0CU8xwxxznB");
		AccessToken accessToken = new AccessToken("53612356-eWirp6zij2dvHe4KS88bWnMLnMMdU1lUxKjCf5HJN", 
				"BQBSk6p9Jm3aWhRydfK0hgj0TD8GnZYzZy3Mvfsa7Oen0");
		twitter.setOAuthAccessToken(accessToken);
		
		try {
			checkSleep("lookupUsers");
			ResponseList<User> r = twitter.lookupUsers(ids);
			lookupUsersRateLimit--;
			
			return r;
		} catch (TwitterException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	// Seed file with 4 usernames for starting base of followers
	String getNextName(int num) {
		FileInputStream ip = null;
		File file = new File("seed.txt");
		String handle = new String();
		
		if (!file.exists()) {
			System.out.println("Error-getuserid: could not open file");
			System.exit(-1);
		}
		
		try {
			ip = new FileInputStream(file);
		
			for (int i = 0; i < num; i++) {
				handle = new String();	// get string from file
				
				if (ip.available() != 0) {	// if num is past end of file, don't try to read
					while ((char) ip.read() != '@'); // seed file's format: name @handle
					char c = 0;
					while (ip.available() != 0 && (c = (char) ip.read()) != '\n')	// if on last line, stop when available == 0
						if (Character.isLetterOrDigit(c) || c == '_') handle += c;	// Twitter handle rule: alphanum or underscore
					
//					System.out.print(handle);
				}
			}
			ip.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return handle;
	}
	
	public void initializeRates() {
		Twitter twitter = new TwitterFactory().getInstance();
		
		twitter.setOAuthConsumer("KOmMvDbWuOkg7IzGhn5DT6NLH", 
				"dalWrw35UOFAD2N2um4pxpj7C04QUzYSvEO1nrM0CU8xwxxznB");
		AccessToken accessToken = new AccessToken("53612356-eWirp6zij2dvHe4KS88bWnMLnMMdU1lUxKjCf5HJN", 
				"BQBSk6p9Jm3aWhRydfK0hgj0TD8GnZYzZy3Mvfsa7Oen0");
		twitter.setOAuthAccessToken(accessToken);
		
		try {
			Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus("users");
			RateLimitStatus showUsersRL = rateLimitStatus.get("/users/show/:id");
			RateLimitStatus lookupUsersRL = rateLimitStatus.get("/users/lookup");
			
			rateLimitStatus = twitter.getRateLimitStatus("followers");
			RateLimitStatus getFollowIDsRL = rateLimitStatus.get("/followers/ids");
			
			showUsersRateLimit = showUsersRL.getRemaining();
			lookupUsersRateLimit = lookupUsersRL.getRemaining();
			getFollowIDsRateLimit = getFollowIDsRL.getRemaining();
		} catch (TwitterException e) {
			System.out.println("Ratelimits in getuserid: initialization fail.");
			System.exit(-1);
		}
	}
	
	int getTimeTilReset(String s) {
		Twitter twitter = new TwitterFactory().getInstance();
		
		twitter.setOAuthConsumer("KOmMvDbWuOkg7IzGhn5DT6NLH", 
				"dalWrw35UOFAD2N2um4pxpj7C04QUzYSvEO1nrM0CU8xwxxznB");
		AccessToken accessToken = new AccessToken("53612356-eWirp6zij2dvHe4KS88bWnMLnMMdU1lUxKjCf5HJN", 
				"BQBSk6p9Jm3aWhRydfK0hgj0TD8GnZYzZy3Mvfsa7Oen0");
		twitter.setOAuthAccessToken(accessToken);
		
		try {
			if (s.equals("showUser")) {
				Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus("users");
				RateLimitStatus showUsersRL = rateLimitStatus.get("/users/show/:id");
				
				return showUsersRL.getSecondsUntilReset() * 1000 + 60 * 1000;
			}
			else if (s.equals("lookupUsers")) {
				Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus("users");
				RateLimitStatus lookupUsersRL = rateLimitStatus.get("/users/lookup");
				
				return lookupUsersRL.getSecondsUntilReset() * 1000 + 60 * 1000;
			}
			else if (s.equals("getFollowerIDs")) {
				Map<String, RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus("followers");
				RateLimitStatus getFollowIDsRL = rateLimitStatus.get("/followers/ids");
				
				return getFollowIDsRL.getSecondsUntilReset() * 1000 + 60 * 1000;
			}
			else {
				System.out.println("Time Reset-String not recognized.");
				System.exit(-1);
			}
		} catch(TwitterException e) {
			System.out.println("Error is in Time Reset function");
			e.printStackTrace();
			System.exit(-1);
		}
		
		return 0;
	}
	
	// Avoids exceeding rate limit, needed b/c if a method fails to give ids,
	// then search should cease because no users to search
	private void checkSleep(String string) {
		int rate = 999; // default val should be high, not low, to avoid sleep
		if (string == "showUser") rate = showUsersRateLimit;
		else if (string == "getFollowerIDs") rate = getFollowIDsRateLimit;
		else if (string == "lookupUsers") rate = lookupUsersRateLimit;
		else {
			System.err.println("Unrecognized method in Followers, Fix this");
			System.exit(-1);
		}
		
		if (rate <= 0) {
			System.out.println("~~~~~~~~~~~Sleeping~~~~~~~~~~~~~");
			
			try {
				Thread.sleep(getTimeTilReset(string));
			} catch (InterruptedException e) {
				e.printStackTrace();
				System.exit(-1);
			}
			
			initializeRates();
		}
	}
}
