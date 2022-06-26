package iterative;

import java.io.*;
import java.net.*;
import java.util.Scanner;

class Client {
    public static void main(String argv[]) throws Exception {

        // create a scanner so we can read the command-line input
        Scanner scanner = new Scanner(System.in);

        //  prompt for server's ip address
        System.out.print("Enter server ip address: ");

        // get their input as a String
        String ipAddress = scanner.next();
        System.out.print("Ip read: " + ipAddress);

        //  prompt for server's port num
        System.out.print("\nEnter server port num: ");

        // get their input as a String
        String portNumStr = scanner.next();
        int portNum = Integer.parseInt(portNumStr);
        System.out.print("Port num read: " + portNum);

        System.out.print("\nEnter numbers with a space between them: ");

        int firstNum = Integer.parseInt(scanner.next());
        int secondNum = Integer.parseInt(scanner.next());
        System.out.print("First num: " + firstNum + "\n");
        System.out.print("Second num: " + secondNum + "\n");

        Socket clientSocket = new Socket(ipAddress, portNum);

        // output stream towards socket initialization
        DataOutputStream outToServer =
                new DataOutputStream(clientSocket.getOutputStream());

        // input stream from socket initialization
        BufferedReader inFromServer =
                new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));

        // send the line to the server
        outToServer.writeBytes(firstNum + "\n" + secondNum + "\n");

        // read the response from the server
        String msgFromServer = inFromServer.readLine();
        System.out.println("Calculated sum: " + msgFromServer);

        int sum = Integer.parseInt(msgFromServer);
        clientSocket.close();
    }
}
