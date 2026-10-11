package irc;

import jvn.Annotations.JvnRead;
import jvn.Annotations.JvnWrite;

public interface ISentence {
    
	@JvnWrite 
    void write(String text);
    
    @JvnRead  
    String read();
}
