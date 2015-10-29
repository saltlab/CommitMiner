import java.util.List;
import java.util.LinkedList;

/**
 * The entry point for the HR system.
 */
public class HRSystem {

	/**
	 * Test the UserFP class.
	 */
	public static void main(String[] args) {

		/* Store a list of users. */
		List<UserFP> users = new LinkedList<UserFP>();

		/* Make some users. */
		users.add(new UserFP("Hello", "Joe", 533386800));
		users.add(new UserFP("Hello", "Suzy", 530673851));
		users.add(new UserFP("Hello", "Bob", 540212400));
		users.add(new UserFP("Hello", "Elanor", 539323200));
		users.add(new UserFP("Hello", "Sam", 541051200));
		users.add(new UserFP("Hello", "Jane", 544843490));

		/* Make the birthday list. */
		long[] birthdays = new long[users.size()]; 
		for(int i = 0; i < users.size(); i++) {
			birthdays[i] = users.get(i).getBirthday();
		}

		/* Print a greeting and age report for each user. */
		for(UserFP user : users) {
			System.out.println(user.getGreetingMessage());
			System.out.println(user.getRelativeAgeMessage(birthdays));
		}

	}

}
