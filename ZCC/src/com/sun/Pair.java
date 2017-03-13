package com.sun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Pair {
    private int srcNode;
    private int desNode;
    private ArrayList<Pair> pairs;
    public Pair(String filename){
    	pairs = new ArrayList<Pair>();
    	initPair(filename);
    }
    public Pair(int src,int des){
    	srcNode = src;
    	desNode = des;
    }
    public int getSrcNode(){
    	return this.srcNode;
    }
    public int getDesNode(){
    	return this.desNode;
    }
    public ArrayList<Pair> getPairs(){
    	return this.pairs;
    }
    public void initPair(String filename){
    	File dirfile = new File(filename);
		if(!dirfile.exists()){
			System.err.println("file not exists!");
			return;
		}
		BufferedReader br;
		String line;
		try{
		    br = new BufferedReader(new FileReader(dirfile));
			while((line = br.readLine())!=null){
				if(line.startsWith("#")) continue;
				String[] node = line.split("\t");
				if(node.length!=2){
					System.out.println("line !=2!!");
					throw new RuntimeException("File Input Error!"+line+","+node.length+","+node[0]+","+"\n");	
				}
				int sNode = Integer.parseInt(node[0]);
				int dNode = Integer.parseInt(node[1]);
				pairs.add(new Pair(sNode,dNode));
			}
			br.close();
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
    }
}
