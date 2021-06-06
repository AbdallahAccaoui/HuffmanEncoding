import java.net.*;
import java.util.Scanner;


import java.io.*;
public class Client {
	Socket s;
	DataInputStream din;
	//DataOutputStream dout;
	
	private ObjectOutputStream outputStream;

	private long totalTime = 0;
	private long totalSizeUncompressed = 0;
	private long totalSizeCompressed = 0;
	
	public static void main(String[] args) {
		new Client();
		
	}
	public Client() {
		
		try {
			s = new Socket("localhost",3333);
			din = new DataInputStream(s.getInputStream());
			//dout= new DataOutputStream(s.getOutputStream());
			
			outputStream = new ObjectOutputStream(s.getOutputStream());
			
			HuffmanEncoder encoder = new HuffmanEncoder();
			RunLength runLength = new RunLength();
			runLength.rlCompress(encoder.compress(" "));						//I just call these to load the libraries of these classes, otherwise they will be loaded when compressing the first line and the loading time will be added to the compression time of t
			
			System.out.println("Type the message to be sent. You can also type 'file' to read from a text file, or 'quit' to end the connection.");
			
			listenforInput();
		} catch (UnknownHostException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	public void listenforInput() {
		Scanner console = new Scanner (System.in);
		while(true) {
			while(!console.hasNextLine()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			String input= console.nextLine();
		    //	byte[] b = input.getBytes();
			if (input.isEmpty()) input = " ";			//Replacing empty lines with spaces to avoid null-pointer exceptions
			
			if(input.toLowerCase().equals("quit")) try {		//Checking for a connection ending
					outputStream.writeObject("quit");
					break;
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			if (input.toLowerCase().contentEquals("file")) try {		//Checking if we are in file mode
					outputStream.writeObject("file");
					File inputFile = new File("input.txt");
					File outputFile = new File ("encoded.txt");
					BufferedReader br = new BufferedReader(new FileReader(inputFile)); 
					BufferedWriter bw = new BufferedWriter (new FileWriter(outputFile));
					String st;
					CompressedLine output;
					while ((st = br.readLine()) != null) 
					{
						if (st.isEmpty()) st = " ";					//Empty space to avoid null pointers
						totalSizeUncompressed += st.length()*8;					//We could just be using inputFile.length() but it'd have some extra data such as line breaks which will affect its size
						output = compressLine(st);
						outputStream.writeObject(output.line);
						
						try (
							ObjectOutputStream outputclient = new ObjectOutputStream(new FileOutputStream("clientencoded.bin",true));
							
						){
							outputclient.writeObject(output.line);
						}
						totalSizeCompressed += output.line.current.encodedData.length();
						totalTime += output.time;
						bw.write(output.line.current.encodedData + " in " + output.time + "ns (" + (double)output.time/1000000+"ms). Compression ratio: " + (double)(st.length()*8)/output.line.current.encodedData.length() + ":1\n");
					}
					outputStream.writeObject("done");
					bw.newLine();
					bw.write("Total compression time: " + totalTime + "ns (" + (double)totalTime/1000000 + "ms)\n");
					bw.write("Total compression ratio: " + (double)totalSizeUncompressed/totalSizeCompressed + ":1");
					br.close();
					bw.close();
					break;
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			else try {
				//dout.write(b);
				//dout.writeUTF(input);
				CompressedLine output = compressLine(input);
			    outputStream.writeObject(output.line);
			    totalTime += output.time;
			    totalSizeUncompressed += input.length()*8;
			    totalSizeCompressed += output.line.current.encodedData.length();
			    
			    System.out.println("Encoded Result : " + output.line.current.encodedData);
				System.out.println("Compression Time is "+ output.time+" ns; "+ (double)output.time/1000000+" ms");
				System.out.println("Compression Ratio is "+ (double)(input.length()*8)/output.line.current.encodedData.length() + ":1");
				//dout.writeBytes(result.encodedData);
				while(din.available() == 0) {
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				String reply = din.readUTF();
				System.out.println(reply);
				System.out.println();
			} catch (IOException e) {
				e.printStackTrace();
				break;
			}
			
		}
		try {
			outputStream.close();
			din.close();
			//dout.close();
			s.close();
			System.out.println("Total compression time: " + totalTime + "ns (" + (double)totalTime/1000000 + "ms)");
			System.out.println("Total compression ratio: " + (double)totalSizeUncompressed/totalSizeCompressed + ":1");
			System.out.println("Connection ended");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	private CompressedLine compressLine(String input)
	{
		HuffmanEncoder encoder = new HuffmanEncoder();
		RunLength runLength = new RunLength();
		long startTime = System.nanoTime();
		HuffmanEncoder.HuffmanEncodedResult result= encoder.compress(input);
		RunLength.rlHuffmanResult  rlResult = runLength.rlCompress(result);
		long endTime = System.nanoTime();
		long timeElapsed = endTime - startTime;
		return new CompressedLine(rlResult, timeElapsed);
	}
	
	static class CompressedLine
	{
		public CompressedLine(RunLength.rlHuffmanResult  line, long time) {
			this.line = line;
			this.time = time;
		}
		
		RunLength.rlHuffmanResult line;
		long time;
	}
}
