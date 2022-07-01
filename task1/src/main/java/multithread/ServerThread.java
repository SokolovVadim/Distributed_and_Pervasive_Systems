package multithread;

import java.io.*;
import java.net.*;

class ServerThread extends Thread {
    private Socket connectionSocket = null;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;

    // the constructor argument is an established socket
    public ServerThread(Socket s) {
        System.out.print("server thread\n");
        connectionSocket = s;
        try {
            inFromClient =
                    new BufferedReader(
                            new InputStreamReader(connectionSocket.getInputStream()));
            outToClient =
                    new DataOutputStream(connectionSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        System.out.print("in thread run\n");
        try {
            // read a line (that terminates with \n) from the client
            String clientMsg1 = inFromClient.readLine();
            String clientMsg2 = inFromClient.readLine();

            int firstNum = Integer.parseInt(clientMsg1);
            int secondNum = Integer.parseInt(clientMsg2);
            System.out.print("First num: " + firstNum + "\n");
            System.out.print("Second num: " + secondNum + "\n");
            int sum = firstNum + secondNum;
            // send the response to the client
            outToClient.writeBytes(sum + "\n");
            /*clientSentence = inFromClient.readLine();
            capitalizedSentence = clientSentence.toUpperCase() + '\n';
            outToClient.writeBytes(capitalizedSentence);*/
            //connectionSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
