package backup;

import java.io.IOException;

import twitter.logAdmin;

class ForTest {
	public static void main(String[] args) {
		logAdmin adminlog = new logAdmin();
		for (int i = 0; i < 50; i++) adminlog.writeHandleLog("Jim Burns", "@NoBurnsForYou");
		
		adminlog.writeNumLog(50, 5);

		try {
			adminlog.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String s = "stringnull!";
		System.out.println(s.contains(null));
		
		return;
	}
}