package uk.ac.sanger.motifxplorer.util;

public class MathUtil {
	public static double log2(double x) {
		return Math.log(x)/Math.log(2.0);
	}
	
	//Just wrapped this in here so I can supply a native implementation easily later on
	public static double log(double x) {
		return Math.log(x);
	}
	
	public static double exp(double x) {
		return Math.exp(x);
	}
	
	public static double exp2(double x) {
		return Math.pow(2,x);
	}
}
