package uk.ac.sanger.motifxplorer.ui.model;

import org.biojava.bio.symbol.Symbol;

/*
 * TODO: Grabbed this as is from MotifExplorer. Add the copyright, author, GPL licence info
 */
public class ResVal {
    private Symbol symbol;
    private double value;

    public final Symbol getToken() {
      return symbol;
    }

    public final double getValue() {
      return value;
    }

    public ResVal(Symbol sym, double val) {
      symbol = sym;
      value = val;
    }
  }