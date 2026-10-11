package jvn;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public @interface Annotations {
	
	// JvnRead.java
	@Retention(RetentionPolicy.RUNTIME) 
	@Target(ElementType.METHOD)
	public @interface JvnRead {}
	
	// JvnWrite.java
	@Retention(RetentionPolicy.RUNTIME) 
	@Target(ElementType.METHOD)
	public @interface JvnWrite {}
}
