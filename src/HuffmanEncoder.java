import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/*
 * As shown, the Huffman Algorithm had consistently compressed the data. 
 * However, for the smaller text files, the Lempel-Ziv Algorithm expanded
 * the data instead of compressing it. As the file size increased, it performed
 * compression on the data. However, its compression ratios were still much higher
 * compared to that of the Huffman Algorithm. For most text files, the Huffman 
 * Algorithm compressed texts faster. 
 * reference: https://www.researchgate.net/publication/304297426_Comparison_of_Huffman_Algorithm_and_Lempel-Ziv_Algorithm_for_audio_image_and_text_compression
 * 
 */

/*
 * Huffman encoding steps
 * First step: build a frequency table
 * Second step: build a tree using priority queue
 * Third Step: Map each character to binary encoding
 * Fourth Step: Encoding aspect
 */
public class HuffmanEncoder {
	private static final int ALPHABET_SIZE = 256;
	public static void main(String[] args) {
		String test="Hello world";
		/*int[] ft= buildFrequencyTable(test);
		Node n = buildHuffmanTree(ft);
		System.out.println(Arrays.toString(ft));
		System.out.println(n.print());
		Map<Character, String> lookup= buildLookupTable(n);
		System.out.println(lookup);
		*/
		HuffmanEncoder encoder = new HuffmanEncoder();
		HuffmanEncodedResult result= encoder.compress(test);
		System.out.println(result.encodedData);
		System.out.println(encoder.decompress(result));
	}
	public HuffmanEncodedResult compress(final String data) {
		//1st step
		int[] ft= buildFrequencyTable(data);
		
		//2nd step
		Node root= buildHuffmanTree(ft);
		
		//3rd step
		Map<Character, String> lookupTable= buildLookupTable(root);
		
		//4th step
		return new HuffmanEncodedResult(generateEncodedData(data,lookupTable),root);
	}
	
	public String decompress(final HuffmanEncodedResult result) {
		StringBuilder resultBuilder = new StringBuilder();
		Node current = result.getRoot();
		int i=0;
		while(i<result.getEncodedData().length()) {
			
			while (!current.isLeaf()) {
				char bit = result.getEncodedData().charAt(i);
				if(bit=='1') {
					current= current.rightChild;
				}else if(bit=='0') {
					current=current.leftChild;
				}else {
					throw new IllegalArgumentException("Invalid bit in message! "+bit); 
				}
				i++;
			}
			resultBuilder.append(current.character);
			current=result.getRoot();
		}
		return resultBuilder.toString();
	}
	
	private static String generateEncodedData(String data, Map<Character, String> lookupTable) {
		StringBuilder builder = new StringBuilder();
		for(char character: data.toCharArray()) {
			builder.append(lookupTable.get(character));
		}
		return builder.toString();
	}
	private static Map<Character,String> buildLookupTable(Node root){
		Map<Character, String> lookupTable= new HashMap<>();
		buildLookupTableImpl(root,"",lookupTable);
		
		return lookupTable;
		
	}
	private static void buildLookupTableImpl(Node node, String s,
											Map<Character,String> lookupTable)
	{
		if(!node.isLeaf()) {
			buildLookupTableImpl(node.leftChild,s+'0', lookupTable);
			buildLookupTableImpl(node.rightChild,s+'1', lookupTable);
			
		}else {
			lookupTable.put(node.character, s);
		}
	}
	
	private static Node buildHuffmanTree(int []freq) {
		PriorityQueue<Node> priorityQueue = new PriorityQueue<>();
		for(char i=0; i< ALPHABET_SIZE;i++) {
			if(freq[i]>0) {
				priorityQueue.add(new Node(i, freq[i],null,null));
			}
		}
		if (priorityQueue.size()==1) {
			priorityQueue.add(new Node('\0',1,null,null));
		}
		while(priorityQueue.size() > 1) {
			Node left= priorityQueue.poll();
			Node right= priorityQueue.poll();
			Node parent= new Node('\0', left.frequency+right.frequency,left,right);
			priorityQueue.add(parent);
		}
		
		return priorityQueue.poll();
	}
	
	private static int[] buildFrequencyTable(final String data) {
		int[] freq = new int[ALPHABET_SIZE];
		for(char character: data.toCharArray()) {
			freq[character]++;
		}
		
		return freq;
	}
	
	
	
	static class Node implements Comparable<Node>, Serializable{ 
		private char character;
		private int frequency;
		private Node leftChild;
		private Node rightChild;
		private Node(char character,int frequency,
					Node leftChild, Node rightChild)
		{
			this.character=character;
			this.frequency= frequency;
			this.leftChild=leftChild;
			this.rightChild=rightChild;
		}
		boolean isLeaf() {
			return this.leftChild==null && this.rightChild==null;
		}
		@Override
		public int compareTo(Node o) {
			int frequencyComparison = Integer.compare(this.frequency, o.frequency);
			if(frequencyComparison != 0) {
				return frequencyComparison;
			}
			return Integer.compare(this.character, o.character);
		}
		public String print() {
			return "["+this.character+"; "+this.frequency+"]";
		}
	}
	static class HuffmanEncodedResult implements Serializable{ 
		Node root;
		String encodedData;
		
		HuffmanEncodedResult(String encodedData, Node root){
			this.encodedData= encodedData;
			this.root=root;
		}
		public Node getRoot() {
			return this.root;
		}
		public String getEncodedData() {
			return this.encodedData;
		}
	}
}
