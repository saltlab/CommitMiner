import java.util.List;
import java.util.LinkedList;

/**
 * The entry point for the HR system.
 */
public class HRSystem {

	/**
	 * Test the User class.
	 */
	public static void main(String[] args) {

		/* Store a list of users. */
		List<User> users = new LinkedList<User>();

		/* Make some users. */
		users.add(new User("Hello", "Joe", 533386800));
		users.add(new User("Hello", "Suzy", 530673851));
		users.add(new User("Hello", "Bob", 540212400));
		users.add(new User("Hello", "Elanor", 539323200));
		users.add(new User("Hello", "Sam", 541051200));
		users.add(new User("Hello", "Jane", 544843490));

		/* Make the birthday list. */
		long[] birthdays = new long[users.size()]; 
		for(int i = 0; i < users.size(); i++) {
			birthdays[i] = users.get(i).birthday();
		}

		/* Print a greeting and age report for each user. */
		for(User user : users) {
			System.out.println(user.getGreetingMessage());
			System.out.println(user.getRelativeAgeMessage(birthdays));
		}

	}

}
