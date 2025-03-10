package com.shure.surdes.survey.constant;

import java.util.ArrayList;
import java.util.List;

/**
 * MBTI 16人格
 * @author color
 *
 */
public class MBTI16Type {

	/** 按顺序4 */
	public final static List<String> CHARACTER_4_TYPE = new ArrayList<>();
	
	/** 按顺序8 */
	public final static List<String> CHARACTER_8_TYPE = new ArrayList<>();
	
	/** DISC4个类型 */
	public final static List<String> DISC_4_TYPE = new ArrayList<>();
	
	static {
		CHARACTER_4_TYPE.add("EI"); //21
		CHARACTER_4_TYPE.add("NS"); //26
		CHARACTER_4_TYPE.add("FT"); //24
		CHARACTER_4_TYPE.add("JP"); //22
		
		CHARACTER_8_TYPE.add("I");
		CHARACTER_8_TYPE.add("P");
		CHARACTER_8_TYPE.add("J");
		CHARACTER_8_TYPE.add("T");
		CHARACTER_8_TYPE.add("F");
		CHARACTER_8_TYPE.add("N");
		CHARACTER_8_TYPE.add("S");
		CHARACTER_8_TYPE.add("E");
		
		DISC_4_TYPE.add("D");
		DISC_4_TYPE.add("I");
		DISC_4_TYPE.add("S");
		DISC_4_TYPE.add("C");
	}
}
