import java.io.Serializable;
import java.util.ArrayList;
import java.util.Base64;


public class RunLength {

	public static void main(String[] args)
	{
		String test = "01100110011010111000001011101001110101010111111101001011010000010001101110111110000011111010101011100101001110000111110011000110101011011111000011001101011100100011011101111101101110111010001000";
		
		RunLength rlEncoder = new RunLength();
		String numbers = toHex(test);
		HuffmanEncoder encoder = new HuffmanEncoder();
		HuffmanEncoder.HuffmanEncodedResult result= encoder.compress(numbers);
	    System.out.println("Compressed Result : "+result.encodedData);
	    System.out.println("Decompressed Result : "+toBitStream(encoder.decompress(result)));
//		System.out.println("Compression Ratio is "+ (double)(numbers.length())/result.encodedData.length() + ":1");
	}
	
	public rlHuffmanResult rlCompress(HuffmanEncoder.HuffmanEncodedResult input)
	{
		String numbers = toHex(input.encodedData);
		HuffmanEncoder encoder = new HuffmanEncoder();
		rlHuffmanResult result = new rlHuffmanResult();
		result.oldRoot = input.root;
		result.current = encoder.compress(numbers);
		return result;
	}
	
	public HuffmanEncoder.HuffmanEncodedResult rlDecompress(rlHuffmanResult input)
	{
		HuffmanEncoder encoder = new HuffmanEncoder();
		String bitStream = toBitStream(encoder.decompress(input.current));
		HuffmanEncoder.HuffmanEncodedResult result = new HuffmanEncoder.HuffmanEncodedResult(bitStream, input.oldRoot);
		return result;
	}
	
	public static String toHex(String input)
	{
		ArrayList<Integer> numbers = new ArrayList<Integer>();
		String hex = new String();
		numbers.add(0);
		int previous = '0';
		for (int i = 0; i < input.length(); i++)
		{
			if ((input.charAt(i)) == previous)
				{
				if (numbers.get(numbers.size() - 1) >= 64) throw new ArithmeticException("Too much repetition!");			//Just testing with up to 16 repetitions at a time because the huffman encoder we have is set up to encode each individual character
				numbers.set(numbers.size() - 1, numbers.get(numbers.size() - 1) + 1);
				}
			else
			{
				//hex += Integer.toHexString(numbers.get(numbers.size() - 1));
				
				int temp = numbers.get(numbers.size()-1);
				//String temps = Integer.toString(temp);
				//hex += Base64.getEncoder().encodeToString(temps.getBytes());
				if (temp <= 25) {
					char tempchar = (char) (temp+65); 
					String tempstring = Character.toString(tempchar);
					hex +=  tempstring;
				}else if (temp >25 && temp<=51) {
					char tempchar= (char) (temp+ 71);
					String tempstring = Character.toString(tempchar);
					hex +=  tempstring;
				}else if (temp >51) {
					char tempchar= (char) (temp - 4);
					String tempstring = Character.toString(tempchar);
					hex +=  tempstring;
					
				}
				
				
				numbers.add(1);
				previous = input.charAt(i);
			}
		}
		//hex += Integer.toHexString(numbers.get(numbers.size() - 1));
		int temp = numbers.get(numbers.size()-1);
		//String temps = Integer.toString(temp);
		//hex += Base64.getEncoder().encodeToString(temps.getBytes());
		if (temp <= 25) {
			char tempchar = (char) (temp+65); 
			String tempstring = Character.toString(tempchar);
			hex +=  tempstring;
		}else if (temp >25 && temp<=51) {
			char tempchar= (char) (temp+ 71);
			String tempstring = Character.toString(tempchar);
			hex +=  tempstring;
		}else if (temp >51) {
			char tempchar= (char) (temp - 4);
			String tempstring = Character.toString(tempchar);
			hex +=  tempstring;
			
		}
		return hex;
	}
	
	public static String toBitStream(String input)
	{
		char[] chars = input.toCharArray();
		String output = new String();
		Boolean zero = true;
		for (int i = 0; i < chars.length; i++)
		{
			int temp = (int) chars[i];
			int maxtemp=0;
			if (temp >= 65  &&  temp <= 90) {
				 maxtemp = temp-65; 
			}else if (temp >=97 && temp<=122) {
				maxtemp = temp- 71;
			}else if (temp >=48 && temp <=57) {
				maxtemp= temp + 4;
			}
			if (zero) for (int j = 0; j < maxtemp; j++) output += "0";
			else for (int j = 0; j < maxtemp; j++) output += "1";
			zero = !zero;
		}
		return output;
	}
	
	static class rlHuffmanResult implements Serializable
	{
		HuffmanEncoder.Node oldRoot;
		HuffmanEncoder.HuffmanEncodedResult current;
	}
}
