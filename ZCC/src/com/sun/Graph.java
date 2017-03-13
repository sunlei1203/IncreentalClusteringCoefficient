package com.sun;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
	private int nodeNum;
	private Map<Integer, List<Integer>> node_neighbor;
//	private BufferedReader in;
    public Graph(){
    	this.nodeNum = 0;
    	this.node_neighbor = new HashMap<Integer,List<Integer>>();
    }
    public Graph(String filename,int nodeNum){
    	this.nodeNum = nodeNum;
    	this.node_neighbor = new HashMap<Integer,List<Integer>>();
    	initGraph(filename);
    }
    public int getNodeNum(){
    	return this.nodeNum;
    }
    public void initGraph(String filename){
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
				if(node_neighbor==null||!node_neighbor.containsKey(sNode)){
					List<Integer> val = new ArrayList<Integer>();
					val.add(dNode);
					node_neighbor.put(sNode, val);
				}else{
					ArrayList<Integer> valList = (ArrayList<Integer>) node_neighbor.get(sNode);
					valList.add(dNode);
					node_neighbor.put(sNode, valList);
				}
				//2017-2-26 new add
				if(node_neighbor==null||!node_neighbor.containsKey(dNode)){
					List<Integer> val = new ArrayList<Integer>();
					val.add(sNode);
					node_neighbor.put(dNode, val);
				}else{
					ArrayList<Integer> valList = (ArrayList<Integer>) node_neighbor.get(dNode);
					valList.add(sNode);
					node_neighbor.put(dNode, valList);
				}
			}
			br.close();
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		System.out.println("the total node is : "+ node_neighbor.size());
    }
    public ArrayList<Integer> getNeighborList(int nodeId){
    	if(node_neighbor.containsKey(nodeId)){
    		return (ArrayList<Integer>)node_neighbor.get(nodeId);
    	}else return null;
    }
    public int getDegree(int nodeId){
    	if(node_neighbor.containsKey(nodeId)){
    		return node_neighbor.get(nodeId).size();
    	}else return 0;
    }
    public Map<Integer, List<Integer>> getMap(){
    	return this.node_neighbor;
    }
    public int isAdjacent(int x1,int x2){
    	if(node_neighbor.containsKey(x1)){
    		if(node_neighbor.get(x1).contains(x2)) return 1;
    		else return 0;
    	}
    	return 0;
    }
    public void addEdge(int srcNode,int desNode){
    	if(!node_neighbor.containsKey(srcNode)){
    		ArrayList<Integer> oneArrayList = new ArrayList<Integer>();
    		oneArrayList.add(desNode);
    		node_neighbor.put(srcNode, oneArrayList);       
    	}else{
    		node_neighbor.get(srcNode).add(desNode);
    	}
    	if(!node_neighbor.containsKey(desNode)){
    		ArrayList<Integer> oneArrayList = new ArrayList<Integer>();
    		oneArrayList.add(srcNode);
    		node_neighbor.put(desNode, oneArrayList);       
    	}else{
    		node_neighbor.get(desNode).add(srcNode);
    	}
    }
	public double computeRealAveCC(){
		/*主要的函数操作部分*/
		int totalTriangle = 0;   //统计总的三角形个数（计算全局聚类系数）
		double totalLocal = 0.0;  //为了计算局部聚类系数的平均值
		double degreeAnd = 0.0;    //计算每个节点度的乘积之和（degree*(degree-1)）
		for(Map.Entry<Integer, List<Integer>> entry:node_neighbor.entrySet()){
			int srcId = entry.getKey();
			ArrayList<Integer> valList = (ArrayList<Integer>)entry.getValue();
			int adjEdges = searchEdge(valList);
			totalTriangle+=adjEdges;
			int valSize = valList.size();
			degreeAnd += valSize*(valSize-1);
			double onelocalCC = oneLocalCofficient(adjEdges,valSize);
			totalLocal += onelocalCC;		
		}
		//求图中所有局部聚类系数的平均值
		double aveLocalCC = aveLocalCofficient(totalLocal,node_neighbor.size());
//		double globalCC = countglobalCC(totalTriangle,degreeAnd);
//		System.out.println("the Local clustering coefficient is :"+aveLocalCC);
//		System.out.println("the Global clustering coefficient is :"+globalCC);
		return aveLocalCC;
	}
	
	public double oneLocalCofficient(int adjEdges,int degree){
		/*统计，并计算局部和全局聚类系数*/
		if(degree==0||degree==1) return 0;
		return 2.0*adjEdges/(degree*(degree-1));
	}
	
	public double aveLocalCofficient(double totalLocal,int num){
		return totalLocal/num;
	}
	/*public double countglobalCC(int totalTriangle,double degreeAnd){
		计算全局和全局聚类系数
		return 2.0*totalTriangle/degreeAnd;
	}*/
	public int searchEdge(List<Integer> valList){
		/*计算某个节点的邻居节点之间的边数*/
		if(valList == null||valList.size()==0) return 0;
		int count = 0;
		for(int i=0;i<valList.size();i++){
			int curNode = valList.get(i);
			ArrayList<Integer> list =(ArrayList<Integer>) node_neighbor.get(curNode);
			if(list==null||list.size()==0) continue;
			for(int j = i+1;j<valList.size();j++){
//				if(list==null||list.size()==0) break;
				if(list.contains(valList.get(j))){
					count++;
				}
			}
		}
		return count;
	}
}
