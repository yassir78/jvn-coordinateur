package com.uga.javanaise;


import java.util.logging.Logger;
import jvn.JvnCoordImpl;

public class Main {

    static Logger logger = Logger.getLogger(Main.class.getName());
    public static void main(String[] args) {
        try {
            JvnCoordImpl.getInstance();
            logger.info("Javanaise started");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
