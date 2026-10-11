package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import jvn.Annotations.JvnRead;
import jvn.Annotations.JvnWrite;

public class JvnProxy implements InvocationHandler {

	private final JvnObject jo;
    
	private JvnProxy(JvnObject jo) { 
    	this.jo = jo; 
    }

    public static Object newInstance(String name, Serializable obj) throws JvnException {
        JvnServerImpl js = JvnServerImpl.jvnGetServer();
        JvnObject jo = js.jvnLookupObject(name);
        if (jo == null) {
            jo = js.jvnCreateObject(obj);
            jo.jvnUnLock();
            js.jvnRegisterObject(name, jo);
        }
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(), new JvnProxy(jo));
    }

    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        if (m.isAnnotationPresent(JvnRead.class))       jo.jvnLockRead();
        else if (m.isAnnotationPresent(JvnWrite.class)) jo.jvnLockWrite();
        try {
            return m.invoke(jo.jvnGetObjectState(), args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        } finally {
            if (m.isAnnotationPresent(JvnRead.class) || m.isAnnotationPresent(JvnWrite.class))
                jo.jvnUnLock();
        }
    }
}