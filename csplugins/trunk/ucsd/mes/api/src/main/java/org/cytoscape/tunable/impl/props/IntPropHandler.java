

package org.cytoscape.tunable.impl.props;

import java.lang.reflect.*;
import java.util.*;
import org.cytoscape.tunable.*;

public class IntPropHandler implements PropHandler {

	Field f;
	Object o;
	String propKey;

	public IntPropHandler(Field f, Object o, Tunable t) {
		this.f = f;
		this.o = o;
		propKey = t.namespace() + "." + f.getName();	
	}

	
	public Properties getProps() {
		Properties p = new Properties();
		try {
		p.put( propKey, Integer.toString(f.getInt(o)) );
		} catch (IllegalAccessException iae) {
			iae.printStackTrace();
		}
		return p;
	}
		
	public void setProps(Properties p) {
		try {
		if ( p.containsKey( propKey ) ) {
			String val = p.getProperty( propKey );
			if ( val != null )
				f.setInt(o, Integer.valueOf(val));
		}
		} catch (IllegalAccessException iae) {
			iae.printStackTrace();
		}
	}
}
