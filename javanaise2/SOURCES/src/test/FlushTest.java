package test;

import java.io.Serializable;

import jvn.JvnObject;
import jvn.JvnServerImpl;

public class FlushTest {

    public static class TestObject implements Serializable {
        private static final long serialVersionUID = 1L;

        private final String value;

        public TestObject(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

    public static void main(String[] args) throws Exception {
        JvnServerImpl server = JvnServerImpl.jvnGetServer();

        System.out.println("Création du premier objet...");
        JvnObject first = server.jvnCreateObject(new TestObject("objet-1"));

        first.jvnUnLock();
        server.jvnRegisterObject("objet-1", first);

        System.out.println("Création du deuxième objet...");
        JvnObject second = server.jvnCreateObject(new TestObject("objet-2"));

        second.jvnUnLock();

        System.out.println("Test terminé.");
        server.jvnTerminate();
    }
}