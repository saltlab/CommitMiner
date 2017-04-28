package commitminer.git;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Output the metrics of GitProjects on a text file. File stream and header are
 * created on constructor, method output(GitProject) is used to append rows and
 * closeStream() finalize its execution
 */
public class GitMetricsExtractorOutput {
	private PrintStream stream;

	public GitMetricsExtractorOutput(String filePath) {

		try {
			/*
			 * The path to the output folder may not exist. Create it if needed.
			 */
			File path = new File(filePath);
			path.getParentFile().mkdirs();

			stream = new PrintStream(new FileOutputStream(filePath));
			writeHeaders();
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}

	public void output(GitProject gitProject) {
		String name = gitProject.getName();
		String URI = gitProject.getURI();
		String totalCommits = gitProject.getTotalCommits().toString();
		String totalBugFixingCommits = gitProject.getBugFixingCommits().toString();
		String numberAuthors = gitProject.getNumberAuthors().toString();
		String numberFiles = gitProject.getNumberOfFiles().toString();
		String linesOfCode = gitProject.getNumberOfLines().toString();
		String stargazers = gitProject.getStargazers().toString();
		String downloadsLastMonth = gitProject.getDownloadsLastMonth().toString();
		String lastCommit = gitProject.getLastCommitDate().toString();
		String firstCommit = gitProject.getFirstCommitDate().toString();

		String row = String.join(",", name, URI, totalCommits, totalBugFixingCommits, numberAuthors, numberFiles,
				linesOfCode, stargazers, downloadsLastMonth, lastCommit, firstCommit);

		stream.println(row);
	}

	public void closeStream() {
		stream.close();
	}

	private void writeHeaders() {
		String header = String.join(",", "Name", "URI", "TotalCommits", "BugFixingCommits", "NumberAuthors",
				"NumberFiles", "LinesOfCode", "Stargazers", "DownloadsLastMonth", "LastCommit", "FirstCommit");

		stream.println(header);
	}

}
