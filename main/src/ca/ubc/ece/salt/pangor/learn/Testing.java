package ca.ubc.ece.salt.pangor.learn;


public class Testing {

	public static void main(String[] args) {

		EvaluationResult[][] results = new EvaluationResult[2][3];
		results[0][0] = new EvaluationResult(null, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1);
		results[0][1] = new EvaluationResult(null, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2, 0.2);
		results[0][2] = new EvaluationResult(null, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3, 0.3);
		results[1][0] = new EvaluationResult(null, 0.1, 0.1, 0.1, 0.1, 0.1, 0.05, 0.1);
		results[1][1] = new EvaluationResult(null, 0.2, 0.2, 0.2, 0.2, 0.2, 0.1, 0.2);
		results[1][2] = new EvaluationResult(null, 0.3, 0.3, 0.3, 0.3, 0.3, 0.2, 0.3);
		printRChart(results);

	}

	/**
	 * Prints an R script to the console. When run, the script generates a
	 * set of graphs showing the clusterig results.
	 */
	private static void printRChart(EvaluationResult[][] results) {

		if(results.length == 0) {
			System.out.println("No results.");
			return;
		}

		String script = "#!/usr/bin/Rscript\n";
		script += "pdfFile <- \"tuning.pdf\"\n";
		script += "cairo_pdf(filename=pdfFile, width=7, height=7)\n";
		script += "par(mfrow = c(3,1))\n";
		script += "par(oma = c(4,1,1,1))\n";
		script += "\n";
		script += printChart(results, Testing::getEpsilon, Testing::getInspected);
		script += "\n";
		script += "par(fig=c(0,1,0,1),oma=c(0,0,0,0),mar=c(0,0,0,0),new=TRUE)\n";
		script += "plot(0,0,type=\"n\", bty=\"n\",xaxt=\"n\",yaxt=\"n\")\n";
		script += "legend(\"bottom\",inset=.04,c(\"Language\",\"Statements\",\"Nodes\"),lty=1,bty=\"n\",col=c(\"red\",\"black\",\"green\"),pch=c(0,1,3),horiz=TRUE)\n";
		script += "\n";
		script += "dev.off()";

		System.out.println(script);

		printChart(results, Testing::getEpsilon, Testing::getInspected);
	}

	public static String printChart(EvaluationResult[][] results, Data ydata, Data xdata) {
		String script = "";
		script += "chartName <- \"Inspected vs Epsilon\"\n";
		script += "\n";
		script += String.format("xData <- c(%s)\n", xdata.getData(results[0]));
		script += String.format("yData <- c(%s)\n", ydata.getData(results[0]));
		script += "plot(xData,yData,main=chartName,xlab=\"epsilon\",ylab=\"Inspected\",ylim=c(0,0.3),xlim=c(0.1,5.9),yaxp=c(0,1,10),xaxp=c(0.1,5.9,29),yaxs=\"i\",xaxs=\"i\",type=\"l\")\n";

		for(int i = 0; i < results.length; i++) {
			EvaluationResult[] dataSetResult = results[i];
			script += String.format("yData <- c(%s)\n", getInspected(dataSetResult));
			script += "lines(xData,yData,type=\"l\",lwd=1,lty=1,col=\"green\",pch=0)\n";
		}

		return script;
	}

	public interface Data {
		String getData(EvaluationResult[] dataSetResult);
	}

	/**
	 * @return A comma separated list of epsilon values for the x-axis.
	 */
	public static String getEpsilon(EvaluationResult[] dataSetResult) {
			String xData = "";
			for(int j = 0; j < dataSetResult.length; j++) {
				xData += dataSetResult[j].epsilon;
				if(j < dataSetResult.length - 1) xData += ",";
			}
			return xData;
	}

	/**
	 * @return A comma separated list of inspected values for the y-axis.
	 */
	public static String getInspected(EvaluationResult[] dataSetResult) {
			String yData = "";
			for(int j = 0; j < dataSetResult.length; j++) {
				yData += dataSetResult[j].inspected;
				if(j < dataSetResult.length - 1) yData += ",";
			}
			return yData;
	}

}
