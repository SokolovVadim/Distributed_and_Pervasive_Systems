package iterative;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

class IterativeServer {
    public static void main(String argv[]) throws Exception {
        // create a scanner so we can read the command-line input
        Scanner scanner = new Scanner(System.in);
        //  prompt for server's port num
        System.out.print("\nEnter server port num: ");

        // get their input as a String
        String portNumStr = scanner.next();
        int portNum = Integer.parseInt(portNumStr);
        System.out.print("Port num read: " + portNum + "\n");

        // create a "listening socket" on the specified port
        ServerSocket welcomeSocket = new ServerSocket(portNum);

        while(true) {
			/*	accept is a blocking call
				once a new connection arrived, it creates
				a new "established socket"	*/
            Socket connectionSocket = welcomeSocket.accept();

            // input stream from the socket initialization
            BufferedReader inFromClient =
                    new BufferedReader(
                            new InputStreamReader(connectionSocket.getInputStream()));

            // output stream to the socket initialization
            DataOutputStream outToClient =
                    new DataOutputStream(connectionSocket.getOutputStream());

            // read a line (that terminates with \n) from the client
            String clientMsg1 = inFromClient.readLine();
            String clientMsg2 = inFromClient.readLine();

            int firstNum = Integer.parseInt(clientMsg1);
            int secondNum = Integer.parseInt(clientMsg2);
            System.out.print("First num: " + firstNum + "\n");
            System.out.print("Second num: " + secondNum + "\n");

            // wait for 10 seconds
            // Thread.sleep(10000);

            int sum = firstNum + secondNum;
            // send the response to the client
            outToClient.writeBytes(sum + "\n");
        }
    }
}
