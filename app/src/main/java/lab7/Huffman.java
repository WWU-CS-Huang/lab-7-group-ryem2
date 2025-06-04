package lab7;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Huffman{

    public static class HuffmanNode implements Comparable<HuffmanNode>{
        char character;
        int freq;
        HuffmanNode left, right;

        HuffmanNode(char character, int freq){
            this.character = character;
            this.freq = freq;
        }

        HuffmanNode(char character, int freq, HuffmanNode left, HuffmanNode right){
            this.character = character;
            this.freq = freq;
            this.left = left;
            this.right = right;
        }

        boolean isLeaf(){
            return left==null && right==null;
        }

        @Override
        public int compareTo(HuffmanNode other){
            return Integer.compare(this.freq, other.freq);
        }
    }

    static Map<Character, Integer> countFrequencies(String input){
        Map<Character, Integer> freqMap = new HashMap<>();
        for (char c : input.toCharArray()) {
            freqMap.put(c, freqMap.getOrDefault(c, 0) + 1);
        }
        return freqMap;
    }

    static HuffmanNode buildTree(Map<Character,Integer> freqMap){
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        for (Map.Entry<Character, Integer> entry : freqMap.entrySet()){
            pq.add(new HuffmanNode(entry.getKey(), entry.getValue()));
        }

        while (pq.size()>1){
            HuffmanNode left = pq.poll();
            HuffmanNode right = pq.poll();
            HuffmanNode merged = new HuffmanNode('\0', left.freq + right.freq, left, right);
            pq.add(merged);
        }

        return pq.poll();
    }

    static void buildCodeMap(HuffmanNode node, String code, Map<Character, String> codeMap){
        if (node == null) return;
        if (node.isLeaf()){
            codeMap.put(node.character, code);
        }
        else{
            buildCodeMap(node.left, code +"0", codeMap);
            buildCodeMap(node.right, code + "1", codeMap);
        }
    }

    static Map<Character, String> buildEncodingTable(HuffmanNode root){
        Map<Character, String> codeMap = new HashMap<>();
        buildCodeMap(root, "", codeMap);
        return codeMap;
    }

    static String encode (String input, Map<Character, String> codeMap){
        StringBuilder sb = new StringBuilder();
        for (char c : input.toCharArray()){
            sb.append(codeMap.get(c));
        }
        return sb.toString();
    }

    static String decode(String encoded, HuffmanNode root){
        StringBuilder sb = new StringBuilder();
        HuffmanNode current = root;

        for (char bit : encoded.toCharArray()){
            current = (bit == '0') ? current.left : current.right;
            if (current.isLeaf()){
                sb.append(current.character);
                current = root;
            }
        }

        return sb.toString();
    }

    public static void main(String[] args){
        if (args.length != 1){
            System.err.println("Please us 'gradle runn --args \"filename\"");
            System.exit(1);
        }

        String input;
        try{
            input = Files.readString(Path.of(args[0]));
        } catch (IOException e){
            System.err.println("Error reading file: " + e.getMessage());
            return;
        }

        Map<Character, Integer> freqMap = countFrequencies(input);
        HuffmanNode root = buildTree(freqMap);
        Map<Character, String> codeMap = buildEncodingTable(root);

        String encoded = encode(input, codeMap);
        String decoded = decode(encoded, root);

        if (input.length() < 100) {
            System.out.println("Input string: " + input);
            System.out.println("Encoded string: " + encoded);
            System.out.println("Decoded string: " + decoded);
        }

        System.out.println("Decoded equals input: " + input.equals(decoded));
        System.out.println("Compression ratio: " + (encoded.length() / (input.length() * 8.0)));
    }
}