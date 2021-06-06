
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import java.net.*;
public class Server {

	ServerSocket ss;
	Socket s;
	//DataInputStream din;
	DataOutputStream dout;

	private ObjectInputStream inputStream;

	RunLength.rlHuffmanResult result;
	ArrayList<RunLength.rlHuffmanResult> results = new ArrayList<RunLength.rlHuffmanResult>();
	Object received;
	private long totalTime = 0;
	private long totalSizeUncompressed = 0;
	private long totalSizeCompressed = 0;
	
	public static void main(String[] args) {
		new Server();

	}
	public Server() {
		try {
			ss = new ServerSocket(3333);
			s= ss.accept();
			//din = new DataInputStream(s.getInputStream());
			dout= new DataOutputStream(s.getOutputStream());
			inputStream = new ObjectInputStream(s.getInputStream());
			
			HuffmanEncoder encoder = new HuffmanEncoder();
			RunLength runLength = new RunLength();
			runLength.rlCompress(encoder.compress(" "));						//I just call these to load the libraries of these classes, otherwise they will be loaded when compressing the first line and the loading time will be added to the compression time of t
			
			listenforData();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void listenforData() {
		while(true) {
			try {
				/*while(inputStream.available()==0) {

					try {
						Thread.sleep(1);
					}catch(InterruptedException e) {
						e.printStackTrace();
					}
				}*/
				try {
					received = inputStream.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

				if (received instanceof RunLength.rlHuffmanResult)
				{
					result = (RunLength.rlHuffmanResult) received;
					DecompressedLine dcResult = decompress(result);
					System.out.println("Encoded text:  "+ result.current.encodedData);
					System.out.println("Decompressed text:  " + dcResult.line);
					System.out.println("Decompression Time is "+ dcResult.time+" ns; "+(double)dcResult.time/1000000+" ms");
					System.out.println("Decompression Ratio is "+ (double)(dcResult.line.length()*8)/result.current.encodedData.length() + ":1\n");
					totalSizeCompressed += result.current.encodedData.length();
					totalSizeUncompressed += dcResult.line.length()*8;
					totalTime += dcResult.time;
				}

				else if (received instanceof String)
				{
					if (received.equals("quit")) break;
					if (received.equals("file"))
					{
						System.out.println("File mode");
						File compressedFile = new File("compressed.txt");
						BufferedWriter bwCompressed = new BufferedWriter(new FileWriter(compressedFile));
						try {
							received = inputStream.readObject();
							while (!received.equals("done"))			//Creating the file from raw encoded data
							{
								if (received instanceof RunLength.rlHuffmanResult) {
									result = (RunLength.rlHuffmanResult) received;
									results.add(result);
									bwCompressed.write(result.current.encodedData + "\n");
									received = inputStream.readObject();
								}
								else if (received.equals("empty")) 
									{
									bwCompressed.newLine();
									received = inputStream.readObject();
									}
							}
						} catch (ClassNotFoundException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						bwCompressed.close();

						File decompressedFile = new File("decompressed.txt");
						BufferedWriter bwDecompressed = new BufferedWriter(new FileWriter(decompressedFile));
						
						DecompressedLine output;
						for (RunLength.rlHuffmanResult i : results) 
						{
							totalSizeCompressed += i.current.encodedData.length();
							output = decompress(i);
							bwDecompressed.write(output.line + " in " + output.time + "ns (" + (double)output.time/1000000 + "ms). Decompression ratio: " + (double)(output.line.length()*8)/i.current.encodedData.length() + ":1\n");
							totalSizeUncompressed += output.line.length()*8;
							totalTime += output.time;
						}

						bwDecompressed.newLine();
						bwDecompressed.write("Total decompression time: " + totalTime + "ns (" + (double)totalTime/1000000 + "ms)\n");
						bwDecompressed.write("Total compression ratio: " + (double)totalSizeUncompressed/totalSizeCompressed + ":1");
						bwDecompressed.close();
						break;
					}
				}
				//String dataIn = din.readUTF();
				//byte b = din.readByte();
				//byte[] b = din.readAllBytes();
				//String dataIn = new String(b);
				//System.out.print((char) b);
				dout.writeUTF("Received from Server");

			} catch (IOException e) {
				e.printStackTrace();
				break;
			}

		}
		try {
			inputStream.close();
			dout.close();
			s.close();
			ss.close();
			System.out.println("Total decompression time: " + totalTime + "ns (" + (double)totalTime/1000000 + "ms)");
			System.out.println("Total compression ratio: " + (double)totalSizeUncompressed/totalSizeCompressed + ":1");
			System.out.println("Connection ended");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private DecompressedLine decompress(RunLength.rlHuffmanResult compressed)
	{
		HuffmanEncoder encoder = new HuffmanEncoder();
		RunLength runLength = new RunLength();
		long startTime = System.nanoTime();
		HuffmanEncoder.HuffmanEncodedResult temp = runLength.rlDecompress(compressed);
		String s = encoder.decompress(temp);
		long endTime = System.nanoTime();
		long timeElapsed = endTime - startTime;
		return new DecompressedLine(s, timeElapsed);
	}

	static class DecompressedLine
	{
		public DecompressedLine(String line, long time) {
			this.line = line;
			this.time = time;
		}
		String line;
		long time;
	}

}

