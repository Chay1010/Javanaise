/***
 * Sentence class : used for representing the text exchanged between users 
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc;

import jvn.JvnRead;
import jvn.JvnWrite;


public class Sentence implements ISentence, java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String 	data;
  
	public Sentence() {
		data = new String("");
	}
	
	public void write(String text) {
		data = text;
	}
	public String read() {
		return data;	
	}
	
}
