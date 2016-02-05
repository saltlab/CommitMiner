package ca.ubc.ece.salt.pangor.git;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import ca.ubc.ece.salt.pangor.analysis.Commit.Type;
import ca.ubc.ece.salt.pangor.batch.GitProjectAnalysis;
import ca.ubc.ece.salt.pangor.batch.GitProjectAnalysisException;

/**
 * Represents a Git project and some metrics
 */
public class GitProject {
	protected final Logger logger = LogManager.getLogger(GitProject.class);

	/** The Git instance. **/
	protected Git git;

	/** The repository instance. **/
	protected Repository repository;

	/** The repository name. **/
	protected String projectID;

	/** The repository homepage. **/
	protected String projectHomepage;

	/** The URI **/
	protected String URI;

	/** The number of commits that merged two branches. **/
	protected Integer mergeCommits;

	/** The total number of commits inspected. **/
	protected Integer totalCommits;

	/** The number of commit authors (uniquely identified by their emails) **/
	protected Integer numberAuthors;

	/** The number of javascript files **/
	protected Integer numberOfFiles;

	/** The number of javascript lines of code **/
	protected Integer numberOfLines;

	/** The number of downloads over the last month **/
	protected Integer downloadsLastMonth = -1;

	/** The number of stargazers on GitHub **/
	protected Integer stargazers = -1;

	/** Dates of last (most recent) and first commit **/
	protected Date lastCommitDate, firstCommitDate;

	/** The regex which identifies bug fixing commits. **/
	protected String commitMessageRegex;

	/**
	 * Constructor that is used by our static factory methods.
	 * @param commitMessageRegex
	 */
	protected GitProject(Git git, Repository repository, String URI, String commitMessageRegex) {
		this.git = git;
		this.repository = repository;
		this.URI = URI;
		this.commitMessageRegex = commitMessageRegex;

		try {
			this.projectID = getGitProjectName(URI);
			this.projectHomepage = getGitProjectHomepage(URI);
		} catch (GitProjectAnalysisException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructor that clones another Git Project. Particularly useful for
	 * subclasses
	 */
	protected GitProject(GitProject project, String commitMessageRegex) {
		this(project.git, project.repository, project.URI, commitMessageRegex);
	}

	/*
	 * Getters for the metrics
	 */
	public String getName() {
		return this.projectID;
	}

	public String getURI() {
		return this.URI;
	}

	public Integer getTotalCommits() {
		if (this.totalCommits == null)
			getCommitPairs();

		return this.totalCommits;
	}

	public Integer getBugFixingCommits() {
		if (this.mergeCommits == null || this.totalCommits == null)
			getCommitPairs();

		return this.mergeCommits - this.totalCommits;
	}

	public Integer getNumberAuthors() {
		if (this.numberAuthors == null)
			getCommitPairs();

		return this.numberAuthors;
	}

	public Date getLastCommitDate() {
		if (this.lastCommitDate == null)
			getCommitPairs();

		return this.lastCommitDate;
	}

	public Date getFirstCommitDate() {
		if (this.firstCommitDate == null)
			getCommitPairs();

		return this.firstCommitDate;
	}


	public Integer getNumberOfFiles() {
		if (this.numberOfFiles == null)
			getFilesMetrics();

		return this.numberOfFiles;
	}

	public Integer getNumberOfLines() {
		if (this.numberOfFiles == null)
			getFilesMetrics();

		return numberOfLines;
	}

	public Integer getDownloadsLastMonth() {
		return this.downloadsLastMonth;
	}

	public void setDownloadsLastMonth(Integer downloadsLastMonth) {
		this.downloadsLastMonth = downloadsLastMonth;
	}

	public Integer getStargazers() {
		// Not a github project
		if (!this.URI.contains("github.com"))
			return -1;

		// Value not cached
		if (this.stargazers == -1) {

			try {
				GitHub github = GitHub.connectAnonymously();

				// Really dirty way of getting username/reponame from URI
				GHRepository repository = github.getRepository(this.URI.split("github\\.com/")[1].split("\\.git")[0]);
				this.stargazers = repository.getWatchers();
			} catch (IOException e) {
				System.err.println("Error while accessing GitHub API: " + e.getMessage());
				return -1;
			}
		}

		return this.stargazers;
	}

	/**
	 * Uses the command line tool "ohcount" to get the number of javascript
	 * files and the number of javascript lines of code on the repository
	 */
	protected void getFilesMetrics() {
		Runtime runtime = Runtime.getRuntime();
		Process process;

		String[] command = { "/bin/sh", "-c", "ohcount " + repository.getDirectory().getParent().toString()
				+ " | grep javascript | tr -s ' ' | cut -d ' ' -f 2,3" };

		try {
			/* Check if ohcount is available */
			process = runtime.exec("which ohcount");
			process.waitFor();
			if (process.exitValue() != 0)
				throw new RuntimeException("Could not find ohcount command tool. Perphaphs not installed?");


			/* Run command */
			process = runtime.exec(command);
			process.waitFor();

			/* Get the output */
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String[] output = bufferedReader.readLine().split(" ");

			this.numberOfFiles = Integer.parseInt(output[0]);
			this.numberOfLines = Integer.parseInt(output[1]);

			bufferedReader.close();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();

			this.numberOfFiles = 0;
			this.numberOfLines = 0;
		}
	}

	/**
	 * Extracts revision identifier pairs from bug fixing commits. The pair
	 * includes the bug fixing commit and the previous (buggy) commit.
	 *
	 * @param git The project git instance.
	 * @param repository The project git repository.
	 * @param buggyRevision The hash that identifies the buggy revision.
	 * @param bugFixingRevision The hash that identifies the fixed revision.
	 * @throws IOException
	 * @throws GitAPIException
	 */
	protected List<Triple<String, String, Type>> getCommitPairs() {
		List<Triple<String, String, Type>> bugFixingCommits = new LinkedList<Triple<String, String, Type>>();
		int mergeCommits = 0, commitCounter = 0;

		Set<String> authorsEmails = new HashSet<>();

		Date lastCommitDate = null;
		Date firstCommitDate = null;

		/*
		 * Call git log command
		 */
		Iterable<RevCommit> commits;
		try {
			commits = git.log().call();
		} catch (GitAPIException e) {
			e.printStackTrace();
			return bugFixingCommits;
		}

		/* Starts with the most recent commit and goes back in time. */
		for (RevCommit commit : commits) {

			/*
			 * Add author to authors list
			 */
			PersonIdent authorIdent = commit.getAuthorIdent();
			authorsEmails.add(authorIdent.getEmailAddress());

			/* Try to infer the commit type from the commit message. */
			Type commitMessageType = Type.OTHER;
			String message = commit.getFullMessage();
			commitCounter++;

			/* Merge pattern */
			Pattern pEx = Pattern.compile("merge", Pattern.CASE_INSENSITIVE);
			Matcher mEx = pEx.matcher(message);

			/* Bug fixing commit pattern */
			Pattern pBFC = Pattern.compile(this.commitMessageRegex, Pattern.CASE_INSENSITIVE);
			Matcher mBFC = pBFC.matcher(message);

			if(mEx.find()) commitMessageType = Type.MERGE;
			else if(mBFC.find()) commitMessageType = Type.BUG_FIX;

			/*
			 * If the commit message contains one of our fix keywords, label it as a bug fixing commit.
			 */
			if(commit.getParentCount()  > 0) {
				bugFixingCommits.add(Triple.of(commit.getParent(0).name(), commit.name(), commitMessageType));
			}

			/*
			 * First commit on iteration is most recent one (what we call "last")
			 */
			if (commitCounter == 1)
				lastCommitDate = authorIdent.getWhen();

			/*
			 * Store the date of this commit. When iteration is over, we have
			 * the date for first one
			 */
			firstCommitDate = authorIdent.getWhen();

		}

		/* Keep track of the number of commits and other metrics for reporting. */
		this.mergeCommits = mergeCommits;
		this.totalCommits = commitCounter;
		this.numberAuthors = authorsEmails.size();
		this.lastCommitDate = lastCommitDate;
		this.firstCommitDate = firstCommitDate;

		return bugFixingCommits;
	}


	/**
	 * Extracts the git project name from the URI.
	 *
	 * @param uri The uri (e.g., https://github.com/karma-runner/karma.git)
	 * @return The project name.
	 */
	protected static String getGitProjectName(String uri) throws GitProjectAnalysisException {
		/* Get the name of the project. */
		Pattern namePattern = Pattern.compile("([^/]+)\\.git");
		Matcher matcher = namePattern.matcher(uri);

		if (!matcher.find()) {
			throw new GitProjectAnalysisException("Could not find the .git name in the URI.");
		}

		return matcher.group(1);
	}

	/**
	 * Extracts the project home page from the URI.
	 *
	 * @param uri The uri (e.g., https://github.com/karma-runner/karma.git)
	 * @return The project home page.
	 */
	protected static String getGitProjectHomepage(String uri) throws GitProjectAnalysisException {
		return uri.substring(0, uri.lastIndexOf(".git"));
	}

	/**
	 * Creates the directory for the repository given the URI and the base
	 * directory to store all repositories.
	 *
	 * @param uri The uri (e.g., https://github.com/karma-runner/karma.git)
	 * @param directory The directory for the repositories.
	 * @return The folder to clone the project into.
	 * @throws GitProjectAnalysisException
	 */
	protected static File getGitDirectory(String uri, String directory) throws GitProjectAnalysisException {
		return new File(directory, getGitProjectName(uri));
	}


	/*
	 * Static factory methods
	 */

	/**
	 * Creates a new GitProject instance from a git project directory.
	 *
	 * @param directory The base directory for the project.
	 * @param commitMessageRegex The regular expression that a commit message
	 * 		  needs to match in order to be analyzed.
	 * @return An instance of GitProjectAnalysis.
	 * @throws GitProjectAnalysisException
	 */
	public static GitProject fromDirectory(String directory, String commitMessageRegex) throws GitProjectAnalysisException {
		Git git;
		Repository repository;

		try {
			repository = new RepositoryBuilder().findGitDir(new File(directory)).build();
			git = Git.wrap(repository);
		} catch (IOException e) {
			throw new GitProjectAnalysisException("The git project was not found in the directory " + directory + ".");
		}

		return new GitProject(git, repository, repository.getConfig().getString("remote", "origin", "url"), commitMessageRegex);
	}

	/**
	 * Creates a new GitProject instance from a URI.
	 *
	 * @param uri The remote .git address.
	 * @param directory The directory that stores the cloned repositories.
	 * @param commitMessageRegex The regular expression that a commit message
	 * 		  needs to match in order to be analyzed.
	 * @return An instance of GitProjectAnalysis.
	 * @throws GitAPIException
	 * @throws TransportException
	 * @throws InvalidRemoteException
	 */
	public static GitProject fromURI(String uri, String directory, String commitMessageRegex)
			throws GitProjectAnalysisException, InvalidRemoteException, TransportException, GitAPIException {
		Git git;
		Repository repository;
		File gitDirectory = GitProjectAnalysis.getGitDirectory(uri, directory);

		/* If the directory exists, all we need to do is pull changes. */
		if (gitDirectory.exists()) {
			try {
				repository = new RepositoryBuilder().findGitDir(gitDirectory).build();
				git = Git.wrap(repository);
			} catch (IOException e) {
				throw new GitProjectAnalysisException(
						"The git project was not found in the directory " + directory + ".");
			}

			/*
			 * Check that the remote repository is the same as the one we were
			 * given.
			 */
			StoredConfig config = repository.getConfig();
			if (!config.getString("remote", "origin", "url").equals(uri)) {
				throw new GitProjectAnalysisException(
						"The directory " + gitDirectory + " is being used by a different remote repository.");
			}

			/* Pull changes. */
			PullCommand pullCommand = git.pull();
			PullResult pullResult = pullCommand.call();

			if (!pullResult.isSuccessful()) {
				throw new GitProjectAnalysisException("Pull was not succesfull for " + gitDirectory);
			}
		}
		/* The directory does not exist, so clone the repository. */
		else {
			CloneCommand cloneCommand = Git.cloneRepository().setURI(uri).setDirectory(gitDirectory);
			git = cloneCommand.call();
			repository = git.getRepository();
		}

		GitProject gitProject = new GitProject(git, repository, uri, commitMessageRegex);

		return gitProject;
	}
}
