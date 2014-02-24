package com.winraguini.lyngoapp;

import java.util.Comparator;

class IgnoreCaseComparator implements Comparator<String> {
	  public int compare(String strA, String strB) {
	    return strA.compareToIgnoreCase(strB);
	  }
	}