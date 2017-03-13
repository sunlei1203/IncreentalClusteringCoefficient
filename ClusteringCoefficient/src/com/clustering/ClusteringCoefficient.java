package com.clustering;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusteringCoefficient {
	Map<Integer,List<Integer>> map = new HashMap<Integer,List<Integer>>();
//	int totalNode = 0;
	int totalTriangle = 0;   //统计总的三角形个数（计算全局聚类系数）
	double totalLocal = 0.0;  //为了计算局部聚类系数的平均值
	double degreeAnd = 0.0;    //计算每个节点度的乘积之和（degree*(degree-1)）
 	public void preProcessing(String dirPath){
		/*从文件中读入图放到HashMap中*/
		File dirfile = new File(dirPath);
		if(!dirfile.exists()){
			System.err.println("file not exists!");
			return;
		}
//		String filename = dirPath.substring(dirPath.lastIndexOf("/"));
//		filename = filename.replace("/", "");
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
				if(map==null||!map.containsKey(sNode)){
					List<Integer> val = new ArrayList<Integer>();
					val.add(dNode);
					map.put(sNode, val);
				}else{
					ArrayList<Integer> valList = (ArrayList<Integer>) map.get(sNode);
					valList.add(dNode);
					map.put(sNode, valList);
				}
				//2017-2-26 new add
				if(map==null||!map.containsKey(dNode)){
					List<Integer> val = new ArrayList<Integer>();
					val.add(sNode);
					map.put(dNode, val);
				}else{
					ArrayList<Integer> valList = (ArrayList<Integer>) map.get(dNode);
					valList.add(sNode);
					map.put(dNode, valList);
				}
			}
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		System.out.println("the total node is : "+ map.size());
//		testHashMap();
	}
 	/*测试hashMap*/
 	/*public void testHashMap(){
 		for(Map.Entry<Integer, List<Integer>> entry:map.entrySet()){
 			System.out.println(entry.getKey()+" "+entry.getValue());
 		}
 	}*/
	public void mainProcessing(){
		/*主要的函数操作部分*/
		for(Map.Entry<Integer, List<Integer>> entry:map.entrySet()){
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
		double aveLocalCC = aveLocalCofficient(totalLocal,map.size());
		double globalCC = countglobalCC();
		System.out.println("the Local clustering coefficient is :"+aveLocalCC);
		System.out.println("the Global clustering coefficient is :"+globalCC);
	}
	
	public double oneLocalCofficient(int adjEdges,int degree){
		/*统计，并计算局部和全局聚类系数*/
		if(degree==0||degree==1) return 0;
		return 2.0*adjEdges/(degree*(degree-1));
	}
	
	public double aveLocalCofficient(double totalLocal,int num){
		return totalLocal/num;
	}
	public double countglobalCC(){
		/*计算全局和全局聚类系数*/
		return 2.0*totalTriangle/degreeAnd;
	}
	public int searchEdge(List<Integer> valList){
		/*计算某个节点的邻居节点之间的边数*/
		if(valList == null||valList.size()==0) return 0;
		int count = 0;
		for(int i=0;i<valList.size();i++){
			int curNode = valList.get(i);
			ArrayList<Integer> list =(ArrayList<Integer>) map.get(curNode);
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
	
    public static void main(String[] args) {
    	if(args.length!=1){
    		System.err.println("Execute : GraphPrepare <PathGraphFile>");
    		return;
    	}
    	String dirPath = args[0]; 
    	ClusteringCoefficient obj = new ClusteringCoefficient();
    	obj.preProcessing(dirPath);
    	obj.mainProcessing();
	}
}
