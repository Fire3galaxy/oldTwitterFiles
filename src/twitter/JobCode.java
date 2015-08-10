package twitter;

public class JobCode {
	int KEYNUM;
	int SHORTNUM;
	int SPECNUM;
	JobCode(int key, int shrt, int spec) {
		KEYNUM = key;
		SHORTNUM = shrt;
		SPECNUM = spec;
	}
//		JobCode subCode(JobCode parent) {
//			if (parent == new JobCode(1, 2, 3)) {
//				return new JobCode(3, 2, 1);
//			}
//			
//			return new JobCode(-1, -1, -1);
//		}
}