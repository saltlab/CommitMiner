/**
 * Stores the data for one user.
 */
public class User {

	private String greeting;
	private String user;
	private long birthday;

	public User(String greeting, String user, long birthday) {
		this.greeting = greeting;
		this.user = user;
		this.birthday = birthday;
	}

	/**
	 * Returns a user-specific greeting message.
	 */
	public String getGreetingMessage() {
		return this.greeting + " " + this.getUserName() + "!";
	}

	/**
	 * Returns a message about the user's age relative to the other users.
	 */
	public String getRelativeAgeMessage(long[] users) {
		return "Out of " + users.length + " users, " + this.getUserName() + " is the " + this.getRelativeAge(users) + "th youngest user.";		
	}

	/**
	 * @return the username of this user.
	 */
	public String getUserName() {
		return this.user;
	}

	/**
	 * @return a timestamp of the user's birthday.
	 */
	public long getBirthday() {
		return this.birthday;
	}

	/**
	 * Determines the age of the user relative to the age of the other users.
	 * @param users a sorted list of users' birthdays.
	 * @return the number of users that are younger than {@code user} + 1.
	 */
	public int getRelativeAge(long[] users) {

		/* Sort the users birthdays. */
		sort(users);

		/* Find the correct location for the user. */
		for(int i = 0; i < users.length; i++) {

			/* Return if users[i] is the same age or older. */
			if(users[i] >= this.birthday) return i + 1;

		}

		/* This user is the youngest user. */
		return users.length + 1;

	}

	/**
	 * Sorts an array of longs with insertion sort. Useful for sorting birthdays.
	 */
	public static void sort(long[] array) {

		/* Check for bad input and base cases. */
		if(array == null || array.length <= 1) return;

		/* Check each value. */
		for(int i = 1; i < array.length; i++) {

			/* Swap the value backwards until it is sorted. */
			for(int j = i; j > 0 && array[j] < array[j - 1]; j--) {
				swap(array, j, j-1);
			}

		}

	}

	/**
	 * Swap the value at index i with the value at index j
	 */
	private static void swap(long[] array, int i, int j) {
		long tmp = array[i];
		array[i] = array[j];
		array[j] = tmp;
	}

	/**
	 * Test the User class.
	 */
	public static void main(String[] args) {
		long[] birthdays = new long[] {533386800,540212400,539323200,541051200,530673851,544843490};
		User user = new User("Hello", "Quinn", 530673851);
		System.out.println(user.getGreetingMessage());
		System.out.println(user.getRelativeAgeMessage(birthdays));
	}

}
