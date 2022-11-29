import static java.nio.channels.SelectionKey.OP_READ;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Client {
    public final String HTTPFS = "httpfs";
    public final String router_host = "localhost";
    public final int router_port = 3000;
	public static long sequenceNum = 0;
	public static List<Long> receivedPackets = new ArrayList<>();
	public static int timeout = 3000;
	public static int ackCount = 0;

    public static void main(String[] args) {
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
            client.checkCommand(String.join(" ",args));
        }
    }

    private void checkCommand(String query) {
        List<String> parameters = Arrays.asList(query.split(" "));
        String url,postData = null;
        if (parameters.contains("-d") && parameters.contains("-f")) {
            System.out.println("Have entered '-d' and '-f in a command.'");
            System.out.println();
            return;
        }
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i).equals(HTTPFS) && i==0) {

            } else if ((parameters.get(i).equals("get") || parameters.get(i).equals("post")) && i==1) {

            } else if (parameters.get(i).startsWith("http://")) {
                url = parameters.get(i);
            } else if (parameters.get(i).equals("-h")) {
                if (!parameters.get(i+1).contains(":")) {
                    System.out.println("There is problem with 'Headers'");
                    System.out.println();
                    return;
                }
                i++;
            } else if (parameters.get(i).equals("-d")) {
                String tempData = parameters.get(i+1);
                if (tempData.contains("'")) {
                    postData = tempData.replace("'", "");
                }
                if (tempData.equals("\"")) {
                    postData = tempData.replace("'\"", "");
                }
                i++;
            } else if (parameters.get(i).equals("-f")) {
                StringBuilder fileData = new StringBuilder();
                try {
                    File dataFile = new File(parameters.get(i+1));
                    BufferedReader fileReader = new BufferedReader(new FileReader(dataFile));
                    String tempFileData;
                    while ((tempFileData = fileReader.readLine()) != null) {
                        fileData.append(tempFileData);
                    }
                    if (fileData.toString().contains("'")) {
                        postData = fileData.toString().replace("'", "");
                    }
                    if (fileData.toString().equals("\"")) {
                        postData = fileData.toString().replace("'\"", "");
                    }
                    fileReader.close();
                } catch (Exception e) {
                    System.out.println("Enter Correct File");
                    return;
                }
                i++;
            }
        }
        query = "";
        for (int i = 0; i < parameters.size(); i++) {
            if (parameters.get(i).equals("-d")) {
                query+="-d ";
                query+= postData+" ";
                i++;
            } else if(parameters.get(i).equals("-f")) {
                query+="-d ";
                query+= postData+" ";
                i++;
            } else {
                query+=parameters.get(i)+" ";
            }
        }
        query = query.trim();
        
        String server_host = new URL(query).getHost();
        int server_port = new URL(query).getPort();
        
		SocketAddress routerAddress = new InetSocketAddress(router_host, router_port);
		InetSocketAddress serverAddress = new InetSocketAddress(server_host, server_port);
		Handshake(routerAddress, serverAddress);9
		runClient(routerAddress, serverAddress);
    }
    
    private static void Handshake(SocketAddress routerAddress, InetSocketAddress serverAddress) {
    	try(DatagramChannel channel = DatagramChannel.open()) {
    		String msg = "Hello";
    		sequenceNum++;
			Packet p = new Packet.Builder().setType(0).setSequenceNumber(sequenceNum)
					.setPortNumber(serverAddress.getPort()).setPeerAddress(serverAddress.getAddress())
					.setPayload(msg.getBytes()).create();
			channel.send(p.toBuffer(), routerAddress);
			
			channel.configureBlocking(false);
			Selector selector = Selector.open();
			channel.register(selector, OP_READ);

			selector.select(timeout);

			Set<SelectionKey> keys = selector.selectedKeys();
			if (keys.isEmpty()) {
				System.out.println("No response after timeout\nSending again");
				resend(channel, p, routerAddress);
			}

			ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
			Packet resp = Packet.fromBuffer(buf);
			String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
			System.out.println(payload + " received..!");
			receivedPackets.add(resp.getSequenceNumber());
			keys.clear();
    	}
    }
    
    private static void resend(DatagramChannel channel, Packet p, SocketAddress routerAddress) throws IOException {
		channel.send(p.toBuffer(), routerAddress);
		System.out.println(new String(p.getPayload()));
		if (new String(p.getPayload()).equals("Received")) {
			ackCount++;
		}

		channel.configureBlocking(false);
		Selector selector = Selector.open();
		channel.register(selector, OP_READ);
		selector.select(timeout);

		Set<SelectionKey> keys = selector.selectedKeys();
		if (keys.isEmpty() && ackCount < 10) {

			System.out.println("No response after timeout\nSending again");
			resend(channel, p, routerAddress);
			}
	}
    
    private static void runClient(SocketAddress routerAddr, InetSocketAddress serverAddr) throws IOException {
        try(DatagramChannel channel = DatagramChannel.open()){
        	sequenceNumber++;
        	//String msg = "Hello World";
            Packet p = new Packet.Builder()
                    .setType(0)
                    .setSequenceNumber(sequenceNumber)
                    .setPortNumber(serverAddr.getPort())
                    .setPeerAddress(serverAddr.getAddress())
                    .setPayload(msg.getBytes())
                    .create();
            channel.send(p.toBuffer(), routerAddr);

            System.out.println("Sending \"{}\" to router at {}", msg, routerAddr);

            // Try to receive a packet within timeout.
            channel.configureBlocking(false);
            Selector selector = Selector.open();
            channel.register(selector, OP_READ);
            logger.info("Waiting for the response");
            selector.select(5000);

            Set<SelectionKey> keys = selector.selectedKeys();
            if(keys.isEmpty()){
            	System.out.println("No response after timeout");
                resend(channel,p,routerAddr)
            }

            // We just want a single response.
            ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
            SocketAddress router = channel.receive(buf);
            buf.flip();
            
         
            
            Packet resp = Packet.fromBuffer(buf);
            System.out.println("Packet: {}", resp);
            System.out.println("Router: {}", router);
            String payload = new String(resp.getPayload(), StandardCharsets.UTF_8);
            System.out.println("Payload: {}",  payload);

            
            if (!receivedPackets.contains(response.getSequenceNumber())) {

                receivedPackets.add(response.getSequenceNumber());
                System.out.println("\nResponse from Server : \n" + payload);

                // Sending ACK for the received of the response
                sequenceNumber++;
            	//String msg = "Hello World";
                Packet Ackp = new Packet.Builder()
                        .setType(0)
                        .setSequenceNumber(sequenceNumber)
                        .setPortNumber(serverAddr.getPort())
                        .setPeerAddress(serverAddr.getAddress())
                        .setPayload(msg.getBytes())
                        .create();
                channel.send(p.toBuffer(), routerAddr);

                System.out.println("Sending \"{}\" to router at {}", msg, routerAddr);

                // Try to receive a packet within timeout.
                channel.configureBlocking(false);
                Selector selector = Selector.open();
                channel.register(selector, OP_READ);
                logger.info("Waiting for the response");
                selector.select(5000);

                Set<SelectionKey> keys = selector.selectedKeys();
                if(keys.isEmpty()){
                	System.out.println("No response after timeout");
                    resend(channel,Ackp,routerAddr)
                }

                buf.flip();

                System.out.println("Connection closed..!");
                keys.clear();

                sequenceNumber++;
                Packet pClose = new Packet.Builder().setType(0).setSequenceNumber(sequenceNumber)
                        .setPortNumber(serverAddr.getPort()).setPeerAddress(serverAddr.getAddress())
                        .setPayload("Ok".getBytes()).create();
                channel.send(pClose.toBuffer(), routerAddr);
                System.out.println("OK sent");
            }
            
            keys.clear();
        }
    }
    
}
