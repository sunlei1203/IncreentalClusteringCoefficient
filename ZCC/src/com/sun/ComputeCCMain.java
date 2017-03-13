package com.sun;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
public class ComputeCCMain {
    public static void main(String[] args) throws IOException {
    	if (args.length != 7) {
			System.err.println("Execute : ZCC <graphname> <nodenum> <initfilename> <incfilename> <samplerate-r> <isInc> <Experiment_times>"); //图的名字，节点数，待处理的init文件，待处理的inc文件，增量边的比率，增量/费增量计算(0:非增量，1：增量),实验次数，平均系数的真值，全局系数的真值
			return;
		}
    	String graphName = args[0];
    	int nodeNum = Integer.parseInt(args[1]);
    	String initfilename = args[2];
    	String incfilename = args[3];
    	double r = Double.parseDouble(args[4]);
    	int lengthOfRWPath = (int)(r*nodeNum);
    	int numExperiments = Integer.parseInt(args[6]);     //实验次数
    	double realLocalCC = 0;   //真实局部CC值
//    	double realGlobalCC = 0;   //真实全局CC值
    	Graph graph = new Graph(initfilename,nodeNum);  //将init文件(增量isInc=1)/非增量的图init文件(isInc=0)的图数据存到map数据结构中
    	Pair pair = new Pair(incfilename);
    	ArrayList<Pair> incPairs = pair.getPairs(); //增量部分的数据<srcNode,desNode>
    	estimatorMC[] estimatorList = new estimatorMC[numExperiments];
		double[] resList_Local = new double[numExperiments];
//		double[] resList_Global = new double[numExperiments];
		double rmse_L[] = new double[incPairs.size()]; //存放局部CC的rmse值（每增加一条边，计算一次rmse）
//		double rmse_G[] = new double[incPairs.size()];  //存放全局CC的rmse值（每增加一条边，计算一次rmse）
		double rmse_Local = 0;
//		double rmse_Global = 0;
		int countChangedEdgeInStoredPathForAllExperiment = 0;
		long startTime_L = 0;
//		long startTime_G = 0;
		long makespan_L = 0;
//		long makespan_G = 0;
    	//init处理,计算初始结果
		realLocalCC = graph.computeRealAveCC();
		for ( int i = 0; i < numExperiments; i++){
			estimatorList[i] = new estimatorMC(lengthOfRWPath);
			resList_Local[i] = estimatorList[i].estimateAverageClusterCoefficient(graph);
//			resList_Global[i] = estimatorList[i].estimateGlobalClusterCoefficient(graph);
			rmse_Local += Math.pow((double)resList_Local[i]/realLocalCC - 1, 2);
//			rmse_Global += Math.pow((double)resList_Global[i]/realGlobalCC - 1, 2);
		}
		rmse_Local = Math.sqrt(rmse_Local/numExperiments);
//		rmse_Global = Math.sqrt(rmse_Global/numExperiments);
		System.out.print("init rmse is: "+rmse_Local);
    	//判断增量计算/非增量计算
        if(args[5].equals("0")){     //利用非增量算法计算CC
        	for(int m=0;m<incPairs.size();m++){ //处理每一条增量边
        		Pair src_des = incPairs.get(m);
				int newSrc = src_des.getSrcNode();
        		int newDes = src_des.getDesNode();
        		graph.addEdge(newSrc, newDes);
        		realLocalCC = graph.computeRealAveCC();
        		double rmsel = 0;
//        		double rmseg = 0;
//        		double c = probabilityRecompute;
        		for(int i=0;i<numExperiments;i++){
        			startTime_L = System.currentTimeMillis();
					resList_Local[i] = estimatorList[i].estimateAverageClusterCoefficient(graph);
					makespan_L += System.currentTimeMillis()-startTime_L;
//					startTime_G = System.currentTimeMillis();
//					resList_Global[i] = estimatorList[i].estimateGlobalClusterCoefficient(graph);
//					makespan_G += System.currentTimeMillis()-startTime_G;
					//estimatorList[i].showRandomWalkPath();
					rmsel += Math.pow((double)resList_Local[i]/realLocalCC - 1, 2);	
//					rmseg += Math.pow((double)resList_Global[i]/realGlobalCC - 1, 2);	
        		}
        		rmse_L[m] = Math.sqrt(rmsel/numExperiments);
//        		rmse_G[m] = Math.sqrt(rmseg/numExperiments);
        	}
        	
        }else{                      //利用增量算法计算CC
        	for(int m=0;m<incPairs.size();m++){ //处理每一条增量边
        		Pair src_des = incPairs.get(m);
				int newSrc = src_des.getSrcNode();
        		int newDes = src_des.getDesNode();
        		graph.addEdge(newSrc, newDes);
        		realLocalCC = graph.computeRealAveCC();
        		double rmsel = 0;
//        		double rmseg = 0;
//        		double c = probabilityRecompute;
        		for(int i=0;i<numExperiments;i++){
        			startTime_L = System.currentTimeMillis();
					resList_Local[i] = estimatorList[i].incEstimateAverageClusterCoefficientEdgeAdd(graph, newSrc, newDes);
					makespan_L += System.currentTimeMillis()-startTime_L;
//					startTime_G = System.currentTimeMillis();
//					resList_Global[i] = estimatorList[i].incEstimateGlobalClusterCoefficientEdgeAdd(graph, newSrc, newDes);
//					makespan_G += System.currentTimeMillis()-startTime_G;
					//estimatorList[i].showRandomWalkPath();
					rmsel += Math.pow((double)resList_Local[i]/realLocalCC - 1, 2);	
//					rmseg += Math.pow((double)resList_Global[i]/realGlobalCC - 1, 2);	
        		}
        		rmse_L[m] = Math.sqrt(rmsel/numExperiments);
//        		rmse_G[m] = Math.sqrt(rmseg/numExperiments);
        	}
        }	
        saveRmseAsTxtFile(graphName,0,args[4],args[5],rmse_L,makespan_L);  //0:local
//        saveRmseAsTxtFile(graphName,1,args[4],args[5],rmse_G,makespan_G); //1:global
	}
    public static void saveRmseAsTxtFile(String graphName,int l_g,String r,String isInc,double[] rmse,long time) throws IOException{
    	BufferedWriter bWriter=null;
    	String filename = graphName+"_"+r; 
    	if(isInc.equals("0")) filename += "_NonInc";
    	else filename += "Inc";
    	if(l_g==0) filename +="_local";
    	else filename +="_global";
    	System.out.println(filename+" run time is : "+time);
    	try{
    		bWriter = new BufferedWriter(new FileWriter(filename));
            for(double val:rmse){
            	bWriter.write(val+"");
            	bWriter.newLine();	
            }
    	}catch(IOException e){
    		e.printStackTrace();
    	}finally{
    		bWriter.flush();
    		bWriter.close();
    	}
    	
    }
}
