package multithread;

import java.io.*;
import java.net.*;
import java.util.Scanner;

class MultiServer {
    public static void main(String argv[]) throws Exception {
        // create a scanner so we can read the command-line input
        Scanner scanner = new Scanner(System.in);
        //  prompt for server's port num
        System.out.print("\nEnter server port num: ");

        // get their input as a String
        String portNumStr = scanner.next();
        int portNum = Integer.parseInt(portNumStr);
        System.out.print("Port num read: " + portNum + "\n");
        ServerSocket welcomeSocket = new ServerSocket(portNum);

        while(true) {
            Socket connectionSocket = welcomeSocket.accept();
            System.out.print("before creation of the thread\n");
            // thread creation passing the established socket as arg
            ServerThread theThread = new ServerThread(connectionSocket);

            // start of the thread
            theThread.start();
        }
    }
}
