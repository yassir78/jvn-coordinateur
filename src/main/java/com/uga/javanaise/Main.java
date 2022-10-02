package com.uga.javanaise;

import jvn.JvnCoordImpl;

import java.util.logging.Logger;

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
