package twitter;
import java.util.LinkedList;
import java.util.List;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import excel.ProfileDr;

public class SearchTwitter {
	List<LinkedList<ResponseList<User>>> allSearchResults;
	
	public SearchTwitter() {
		allSearchResults = new LinkedList<LinkedList<ResponseList<User>>>();
	}
	public void search(ProfileDr query, int pageLimit) { 
		try {
			Twitter twitter = new TwitterFactory().getInstance(); // Twitter4j Setup OAuth
			
			twitter.setOAuthConsumer("hhceDx7pPpmN5d2fdvuAQJrGf", 
					"Q1NYcmQFprf9kSd7Dm8IrAnK807UrzZuOPX8t00BXa6b3kPgzo");
			AccessToken accessToken = new AccessToken("53612356-v5w0OvhsU0TLgQ3975Soc4yUsp9TIbF19NVkNLrEF", 
					"ZXjvPyiadzbbEtyYDVo9Uim4KexcLWjn7msesRcuoyjtH");
			twitter.setOAuthAccessToken(accessToken);
			
			/* Current name being searched
			 * Tried "Jungmi L" instead of "Jungmi Lee", but results proved unreliable
			 */
			String firstName = query.getFirstName();
			String lastName = query.getLastName();
			
			// stores results of user search (3 or 10 pages)
			LinkedList<ResponseList<User> > searchResults = new LinkedList<ResponseList<User>>();
			
			for (int page = 0; page < pageLimit; page++) {
				ResponseList<User> users = twitter.searchUsers(firstName + " " + lastName, page);
				searchResults.add(users);
				
				if (users.size() < 20) break; // search results were less than pageLimit's setting
			}
			
			allSearchResults.add(searchResults);
		} catch (TwitterException te) {
			te.printStackTrace();
			System.exit(-1);
		}
	}
	public List<LinkedList<ResponseList<User>>> getSearchResults() {
		return allSearchResults;
	}
}