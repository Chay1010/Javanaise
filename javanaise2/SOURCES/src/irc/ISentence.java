package irc;

import jvn.JvnRead;
import jvn.JvnWrite;

public interface ISentence {
    
	@JvnWrite 
    void write(String text);
    
    @JvnRead  
    String read();
}
