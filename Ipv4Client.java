/*	
/	Author:		Kean Jafari
*/

import java.util.*;
import java.io.*;
import java.net.*;
import java.lang.Object;


public class Ipv4Client {

	static short headerChecksum = 0;	
	static int packetSize = 0;
	
	public static void main(String[] args) {
		try {	
			Socket socket = new Socket("codebank.xyz", 38003);
			for (int packetDataLength = 2; packetDataLength <= 4096;) {
				System.out.println("Data length: " + packetDataLength);
				sendPacket(socket, packetDataLength);
				packetDataLength *= 2;
				headerChecksum = 0;
				packetSize = 0;
			}
		} catch (Exception e) {e.printStackTrace();}
		
	}
	

	// creates a byte array, given a size, and fills it with 0
	public static byte[] createPacket (int size) {
		byte[] packet;

		//Creates byte arrays, row by row, to later merge and create
		//final packet
		
		byte[] first = setFirstRow(size);
		byte[] second = setSecondRow();
		byte[] third = setThirdRow();
		byte[] fourth = setFourthRow();
		byte[] fifth = setFifthRow();
		byte[] data = new byte[size];

		//Determines total size of byte array for packet and instantiates
		//proper packet size
		packetSize = first.length + second.length + third.length +
					fourth.length + fifth.length + size;

		//adds all rows of packet together
		packet = addPacketRows(first, second, third, fourth, fifth, data);

		//calculates checksum
		getChecksum(packet, size);

		//re-enters rows into packet, with correct checksum
		byte[] newThird = setThirdRow();

		//adds packet rows again
		byte[] finalPacket = addPacketRows(first, second, newThird, fourth, fifth, data);
		
		return finalPacket;
	}
	
	public static byte[] setFirstRow(int size) {
		// Version and IHL 
		int version = 4;
		int ihl = 5;
		int tos = 0;

		//Total length in octets?
		int length = 20 + size;

		// adding two 4 bit values to form 1 byte
		int firstByte = version * 16 + ihl;

		//Bit shifting
		byte lengthHigh = (byte) ((length >> 8) & 0xFF);
		byte lengthLow = (byte) (length & 0xFF);
		

		byte[] penis = new byte[2];
		penis[0] = lengthHigh;
		penis[1] = lengthLow;

		//Creates row
		byte[] row = new byte[4];
		row[0] = (byte) firstByte;
		row[1] = (byte) tos;
		row[2] = lengthHigh;
		row[3] = lengthLow;

		return row;
	}

	public static byte[] setSecondRow() {
			byte[] row = new byte[4];
			int id = 0;
			int offset = 0;
			int flag = 2;

			//Flag Offset (bitshift 5 0's)
			flag = flag * 32;

			//Prepare row to return to packet
			row[0] = (byte) id;
			row[1] = 0;
			row[2] = (byte) flag;
			row[3] = (byte) 0;
			return row;
	}

	public static byte[] setThirdRow() {
		
		int ttl = 50;				//time to live = 50 seconds 
		int protocol = 6;			//TCP = 6, UDP = 17
		
		byte[] row = new byte[4];

		row[0] = (byte) ttl;
		row[1] = (byte) protocol;
		row[2] = (byte)((headerChecksum & 0xFF00) >>> 8);
		row[3] = (byte)((headerChecksum & 0x00FF));
	   	//row[2] = 0;
	   	//row[3] = 0;
	   	return row;
	}	

	public static byte[] setFourthRow() {
		//172.217.5.206" -- Google's address
		byte[] row = new byte[4];
		row[0] = (byte) 172;
		row[1] = (byte) 217;
		row[2] = (byte) 5;
		row[3] = (byte) 206;



		return row;
	}

	public static byte[] setFifthRow() {
		// 52.37.88.154	-- Codebank.xyz's IP Address
		byte[] row = new byte[4];
		row[0] = (byte) 52;
		row[1] = (byte) 37;
		row[2] = (byte) 88;
		row[3] = (byte) 154;


		return row;
	}

	// Generates random numbers to fill in the data
	public static byte[] setDataRow(int size) {
		byte[] row = new byte[size];
		Random rand = new Random();
		for (int i = 0; i < size; i++)
			row[i] = (byte)rand.nextInt(255);
		return row;
	}

	// Copied and Modified from EX3
	// Calculates checksum of the package, updates global static variable
	public static void getChecksum(byte[] packet, int size) {
		//Calculates the checksum
		int length = packetSize - size;
		int i = 0;
	   	long total = 0;
	   	long sum = 0;

	    // add to sum and bit shift
	   	while (length > 1) {
	    	sum = sum + ((packet[i] << 8 & 0xFF00) | ((packet[i+1]) & 0x00FF));
	    	i = i + 2;
	    	length = length - 2;

	    	// splits byte into 2 words, adds them.
	    	if ((sum & 0xFFFF0000) > 0) {
	    		sum = sum & 0xFFFF;
	    		sum++;
	    	}
	    }

	    // calculates and adds overflowed bits, if any
		if (length > 0) {
    		sum += packet[i] << 8 & 0xFF00;
			if ((sum & 0xFFFF0000) > 0) {
				sum = sum & 0xFFFF;
				sum++;
			}
    	}

	   	total = (~((sum & 0xFFFF)+(sum >> 16))) & 0xFFFF;
	   	headerChecksum = (short) total;
	}

	// Concatinates the rows of the byte arrays for the packet
	public static byte[] addPacketRows(byte[] first, byte[] second,
		byte[] third, byte[] fourth, byte[] fifth, byte[] data) {


		byte[] packet = new byte[packetSize];
		// ADDS PACKET ROWS	
		int counter = 0;
		for(int i = 0; i < first.length; i++) {
			packet[counter] = first[i];
			counter++;
		}
		for(int i = 0; i < second.length; i++) {
			packet[counter] = second[i];
			counter++;
		}
		for(int i = 0; i < third.length; i++) {
			packet[counter] = third[i];
			counter++;
		}
		for(int i = 0; i < fourth.length; i++) {
			packet[counter] = fourth[i];
			counter++;
		}
		for(int i = 0; i < fifth.length; i++) {
			packet[counter] = fifth[i];
			counter++;
		}
		return packet;

	}

	// Communicates with server, sends packet
	public static void sendPacket(Socket socket, int packetDataLength) {
		try {
			OutputStream os = socket.getOutputStream();
			byte[] stream = createPacket(packetDataLength);
			os.write(stream);
			getResponse(socket);
		} catch (Exception e) { e.printStackTrace(); } 
	}

	//Recieves response from server and prints.
	public static void getResponse(Socket socket) {
		try { 
			InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            BufferedReader br = new BufferedReader(isr);

			System.out.println(br.readLine());
		} catch (Exception e) { e.printStackTrace(); }
	}
}