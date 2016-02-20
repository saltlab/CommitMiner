package ca.uab.ece.salt.pangor.learn.eval;

import org.apache.commons.lang3.StringUtils;

import ca.ubc.ece.salt.pangor.learn.EvaluationResult;


public class RLineChart {

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
	public static void printRChart(EvaluationResult[][] results) {

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
		script += printChart(results, new Epsilon(), new Inspected());
		script += "\n";
		script += printChart(results, new Epsilon(), new PatternRecall());
		script += "\n";
		script += printChart(results, new Epsilon(), new FMeasure());
		script += "\n";
		script += "par(fig=c(0,1,0,1),oma=c(0,0,0,0),mar=c(0,0,0,0),new=TRUE)\n";
		script += "plot(0,0,type=\"n\", bty=\"n\",xaxt=\"n\",yaxt=\"n\")\n";
		script += "legend(\"bottom\",inset=.04,c(\"Language\",\"Statements\",\"Nodes\"),lty=1,bty=\"n\",col=c(\"black\",\"red\",\"green\"),pch=c(0,1,3),horiz=TRUE)\n";
		script += "\n";
		script += "dev.off()";

		System.out.println(script);

	}

	private static String printChart(EvaluationResult[][] results, Data xdata, Data ydata) {
		String[] colours = {"black", "red", "green", "blue", "orange", "grey", "pink"};
		String script = "";
		script += String.format("chartName <- \"%s vs %s\"\n", xdata.getLabel(), ydata.getLabel());
		script += "\n";
		script += String.format("xData <- c(%s)\n", StringUtils.join(xdata.getData(results[0]), ','));
		script += String.format("yData <- c(%s)\n", StringUtils.join(ydata.getData(results[0]), ','));
		script += String.format("plot(xData,yData,main=chartName,xlab=\"%s\",ylab=\"%s\",xlim=c(%s,%s),ylim=c(%s,%s),xaxp=c(%s,%s,%s),yaxp=c(%s,%s,%s),yaxs=\"i\",xaxs=\"i\",type=\"l\")\n",
				xdata.getLabel(), ydata.getLabel(),
				xdata.getLim()[0], xdata.getLim()[1],
				ydata.getLim()[0], ydata.getLim()[1],
				xdata.getAxp()[0], xdata.getAxp()[1], xdata.getAxp()[2],
				ydata.getAxp()[0], ydata.getAxp()[1], ydata.getAxp()[2]);

		for(int i = 1; i < results.length; i++) {
			script += String.format("yData <- c(%s)\n", StringUtils.join(ydata.getData(results[i]), ','));
			script += String.format("lines(xData,yData,type=\"l\",lwd=1,lty=1,col=\"%s\",pch=0)\n", colours[i%colours.length]);
		}

		return script;
	}

}
