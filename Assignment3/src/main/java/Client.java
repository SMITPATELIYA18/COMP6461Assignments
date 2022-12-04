import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.nio.channels.SelectionKey.OP_READ;

public class Client {
    public static List<Long> receivedPackets = new ArrayList<>();
    public static int timeout = 3000;
    public static int ackCount = 0;
    public final String HTTPFS = "httpfs";
    public final String router_host = "localhost";
    public final int router_port = 3000;
    public long sequenceNum = 0;

    public static void main(String[] args) throws IOException, URISyntaxException {
        Client client = new Client();
        String input;
        if (args.length == 0) {
            int flag = 0;
            do {
                System.out.print("Enter the File Transfer Command (0 for Exit): ");
                Scanner sc = new Scanner(System.in);
                input = sc.nextLine();
                if (input.equals("0")) {
                    flag = 1;
                } else {
                    client.checkCommand(input);
                }
            }
            while (flag != 1);
        } else {
            client.checkCommand(String.join(" ", args));
        }
    }

    private static void resend(DatagramChannel channel, Packet p,
                               SocketAddress routerAddress) throws
            IOException {
        channel.send(p.toBuffer(), routerAddress);
//        System.out.println("Sequence Number "+p.getSequenceNumber()+" and  Resending \"" + new String(p.getPayload()) + "\"" +
//                " to router at " + routerAddress);
//        System.out.println(new String(p.getPayload()));
        if (new String(p.getPayload()).equals("Received")) {
            ackCount++;
        }

        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, OP_READ);
        selector.select(timeout);

        Set<SelectionKey> keys = selector.selectedKeys();
        if (keys.isEmpty() && ackCount < 10) {
            System.out.println("-------No response after timeout ---- Sending " +
                    "again------");
            System.out.println("Sequence Number "+p.getSequenceNumber()+" and  Resending \"" + new String(p.getPayload()) + "\"" +
                " to router at " + routerAddress);
            resend(channel, p, routerAddress);
        }
    }

    private void checkCommand(String query) throws IOException, URISyntaxException {
        List<String> parameters = Arrays.asList(query.split(" "));
        String url = null, postData = null;
        if (parameters.contains("-d") && parameters.contains("-f")) {
            System.out.println("Have entered '-d' and '-f in a command.'");
            System.out.println();
            return;
        }
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i).equals(HTTPFS) && i == 0) {

            } else if ((parameters.get(i).equals("get") || parameters.get(i)
                    .equals("post")) && i == 1) {

            } else if (parameters.get(i).startsWith("http://")) {
                url = parameters.get(i);
            } else if (parameters.get(i).equals("-h")) {
                if (!parameters.get(i + 1).contains(":")) {
                    System.out.println("There is problem with 'Headers'");
                    System.out.println();
                    return;
                }
                i++;
            } else if (parameters.get(i).equals("-d")) {
                String tempData = parameters.get(i + 1);
                postData = tempData.replaceAll("\"", "");
                postData = postData.replaceAll("'", "");
                i++;
            } else if (parameters.get(i).equals("-f")) {
                StringBuilder fileData = new StringBuilder();
                try {
                    File dataFile = new File(parameters.get(i + 1));
                    BufferedReader fileReader = new BufferedReader(new FileReader(dataFile));
                    String tempFileData;
                    while ((tempFileData = fileReader.readLine()) != null) {
                        tempFileData = tempFileData.replaceAll(" ", "~");
                        fileData.append(tempFileData);
                        fileData.append("\n");
                    }
                    postData = fileData.toString().replaceAll("\"", "");
                    postData = postData.replaceAll("\'", "");
//                    if (fileData.toString().contains("'")) {
//                        postData = fileData.toString().replace("'", "");
//                    } else
//                        postData = fileData.toString();
//                    if (fileData.toString().contains("\"")) {
//                        postData = fileData.toString().replace("'\"", "");
//                    } else
//                        postData = fileData.toString();
                    fileReader.close();
                } catch (Exception e) {
                    System.out.println("Enter Correct File");
                    return;
                }
                i++;
            } else if(parameters.get(i).equals("-v")){

            }
            else {
                System.out.println("Error!");
                return;
            }
        }
        query = "";
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i).equals("-d")) {
                query += "-d ";
                query += postData + " ";
                i++;
            } else if (parameters.get(i).equals("-f")) {
                query += "-d ";
                query += postData + " ";
                i++;
            } else {
                query += parameters.get(i) + " ";
            }
        }
        query = query.trim();

        String server_host = new URI(url).getHost();
        int server_port = new URI(url).getPort();

        SocketAddress routerAddress = new InetSocketAddress(router_host, router_port);
        InetSocketAddress serverAddress = new InetSocketAddress(server_host, server_port);
        handshake(routerAddress, serverAddress);
        runClient(routerAddress, serverAddress, query);
    }

    private void handshake(SocketAddress routerAddress, InetSocketAddress serverAddress) throws
            IOException {
        try (DatagramChannel channel = DatagramChannel.open()) {
            String msg = "Hi from client";
            sequenceNum++;
            Packet p = new Packet.Builder().setType(0).setSequenceNumber(sequenceNum)
                    .setPortNumber(serverAddress.getPort())
                    .setPeerAddress(serverAddress.getAddress())
                    .setPayload(msg.getBytes()).create();
            channel.send(p.toBuffer(), routerAddress);

            System.out.println("Sequence Number "+sequenceNum+" and  Sending \"" + msg + "\"" +
                    " to router at " + routerAddress);

            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.register(selector, OP_READ);

            selector.select(timeout);

            Set<SelectionKey> keys = selector.selectedKeys();
            if (keys.isEmpty()) {
                System.out.println("-------No response after timeout ---- Sending " +
                        "again------");
                System.out.println("Sequence Number "+sequenceNum+" and  Resending for " +
                        "\"" + msg + "\" to router at " + router_host);
                resend(channel, p, routerAddress);
            }

            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            Packet resp = Packet.fromBuffer(buf);
            String routerPayload = new String(resp.getPayload());
            System.out.println("---- Received---- "+routerPayload);
            receivedPackets.add(resp.getSequenceNumber());
            keys.clear();
        }
    }

    private void runClient(SocketAddress routerAddr, InetSocketAddress serverAddr,
                           String query) throws IOException {
        try (DatagramChannel channel = DatagramChannel.open()) {
            sequenceNum++;
            Packet p = new Packet.Builder()
                    .setType(0)
                    .setSequenceNumber(sequenceNum)
                    .setPortNumber(serverAddr.getPort())
                    .setPeerAddress(serverAddr.getAddress())
                    .setPayload(query.getBytes())
                    .create();
            channel.send(p.toBuffer(), routerAddr);

            System.out.println("Sequence Number "+sequenceNum+" and  Sending \"" + query + "\"" +
                    " to router at " + routerAddr);

            // Try to receive a packet within timeout.
            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.register(selector, OP_READ);
            selector.select(5000);

            Set<SelectionKey> keys = selector.selectedKeys();
            if (keys.isEmpty()) {
                System.out.println("-------No response after timeout ---- Sending " +
                        "again------");
                System.out.println("Sequence Number "+sequenceNum+" and  Resending for \"" + query +
                        "\" to router at " + router_host);
                resend(channel, p, routerAddr);
            }

            // We just want a single response.
            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            SocketAddress router = channel.receive(buf);
            buf.flip();


            Packet resp = Packet.fromBuffer(buf);
            System.out.println("Packet: " + resp);
            System.out.println("Router: " + router);
            String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
//            System.out.println("Payload: " + payload);


            if (!receivedPackets.contains(resp.getSequenceNumber())) {

                receivedPackets.add(resp.getSequenceNumber());
                System.out.println("\nResponse from Server : \n");
                printOnConsole(payload,query);

                // Sending ACK for the received of the response
                sequenceNum++;
                //String msg = "Hello World";
                Packet ackp = new Packet.Builder()
                        .setType(0)
                        .setSequenceNumber(sequenceNum)
                        .setPortNumber(serverAddr.getPort())
                        .setPeerAddress(serverAddr.getAddress())
                        .setPayload(query.getBytes())
                        .create();
                channel.send(p.toBuffer(), routerAddr);

                System.out.println("Sequence Number "+sequenceNum+" and  Sending ACK for \"" + query +
                        "\" to router at " + router_host);

                // Try to receive a packet within timeout.
                channel.configureBlocking(false);
                selector = Selector.open();
                channel.register(selector, OP_READ);
                //logger.info("Waiting for the response");
                selector.select(5000);

                keys = selector.selectedKeys();
                if (keys.isEmpty()) {
                    System.out.println("-------No response after timeout ---- Sending " +
                            "again------");
                    System.out.println("Sequence Number "+sequenceNum+" and  Sending ACK for \"" + query +
                            "\" to router at " + router_host);
                    resend(channel, ackp, routerAddr);
                }

                buf.flip();
                keys.clear();

                sequenceNum++;
                Packet pClose = new Packet.Builder().setType(0).setSequenceNumber(sequenceNum)
                        .setPortNumber(serverAddr.getPort())
                        .setPeerAddress(serverAddr.getAddress())
                        .setPayload("Ok".getBytes()).create();
                channel.send(pClose.toBuffer(), routerAddr);
                System.out.println("Sequence Number "+sequenceNum+" and  Sending \"Ok\"" +
                        " to router at " + routerAddr);
                System.out.println("Connection closed..!");
            }

            keys.clear();
        }
    }

    private static void printOnConsole(String payload, String query) {
        String reposnseCode = payload.split("\n")[0].split(" ")[1];
        if (reposnseCode.equals("404")) {
            System.out.println("The server has not found anything matching the Request-URI.");
            System.out.println(payload);
            return;
        }
        if (reposnseCode.equals("200")) {
            System.out.println("The request has succeed.");
//        	System.out.println(serverResponse.getHeaders());
//        	return;
        }
        if (reposnseCode.equals("201")) {
            System.out.println("The request has succeeded and data is overwrite in requested" +
                    " file.");
//        	System.out.println(serverResponse.getHeaders());
//        	return;
        }
        if (reposnseCode.equals("202")) {
            System.out.println("The request has been fulfilled and resulted in a new " +
                    "resource being created.");
//        	System.out.println(serverResponse.getHeaders());
//        	return;

        }
        if (reposnseCode.equals("203")) {
            System.out.println("The request has succeed.");
//        	System.out.println(serverResponse.getHeaders());
//        	return;
        }
        if (query.contains("-v")) {
            System.out.println(payload);
        } else {
            String[] arrayPayload = payload.split("\n");
            int breakLine = 0;
            for (int i = 0; i < arrayPayload.length; i++) {
                if (arrayPayload[i].equals(""))
                    breakLine = i;
            }
            for (int i = breakLine + 1; i < arrayPayload.length; i++) {
                System.out.println(arrayPayload[i]);
            }
        }
    }

}
