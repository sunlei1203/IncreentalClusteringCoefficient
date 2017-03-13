package com.sun;
import java.util.ArrayList;
import java.util.HashMap;

public class estimatorMC {
	private int lengthOfPath;
	private int countChangedEdgeInStoredPath = 0;
	private double averageClusterCoefficient;
	private double globalClusterCoefficient;
	private double phiL;
	private double psiL;
	private double phiG;
	private double psiG;
	private ArrayList<Integer> randomWalkPath;
	private HashMap<Integer, ArrayList<Integer>> nodePositionInRWPath;

	public estimatorMC() {
		this.lengthOfPath = 0;
		this.averageClusterCoefficient = 0;
		this.globalClusterCoefficient = 0;
		this.phiL = 0;
		this.psiL = 0;
		this.randomWalkPath = new ArrayList<Integer>();
		this.nodePositionInRWPath = new HashMap<Integer, ArrayList<Integer>>();
	}

	public double getPhiL() {
		return phiL;
	}

	public double getPsiL() {
		return psiL;
	}

	public estimatorMC(int lengthOfPath) {
		this.lengthOfPath = lengthOfPath;
		this.averageClusterCoefficient = 0;
		this.globalClusterCoefficient = 0;
		this.phiL = 0;
		this.psiL = 0;
		this.randomWalkPath = new ArrayList<Integer>();
		this.nodePositionInRWPath = new HashMap<Integer, ArrayList<Integer>>();
	}

	/**
	 * 完成一步随机游走，将新的被访问节点的Id存入随机游走路径和节点在随机游走路径中位置的速查表
	 * 
	 * @param graph
	 *            无向连通图
	 * @param currentId
	 *            这一步随机游走的起点Id
	 * @return 新的当前节点Id，也是这一步随机游走的终点
	 */
	private int randomWalk(Graph graph, int currentId) {
		ArrayList<Integer> neighborList = graph.getNeighborList(currentId);
		int nextId = neighborList.get(java.util.concurrent.ThreadLocalRandom
				.current().nextInt(neighborList.size()));
		// System.out.println("From "+ currentId + " to " + nextId );

		this.randomWalkPath.add(nextId);
		if (this.nodePositionInRWPath.get(nextId) == null) {
			this.nodePositionInRWPath.put(nextId, new ArrayList<Integer>());
		}
		this.nodePositionInRWPath.get(nextId).add(
				this.randomWalkPath.size() - 1);

		return nextId;
	}

	/**
	 * 计算平均聚集系数
	 * 
	 * @param graph
	 *            无向连通图
	 * @return 聚集系数的估计值
	 */
	public double estimateAverageClusterCoefficient(Graph graph) {
		countChangedEdgeInStoredPath = 0;
		int currentId = java.util.concurrent.ThreadLocalRandom.current()
				.nextInt(graph.getNodeNum());
		while (graph.getNeighborList(currentId) == null) {
			currentId = java.util.concurrent.ThreadLocalRandom.current()
					.nextInt(graph.getNodeNum());
		}
		for (int i = 0; i < 2; i++) {
			currentId = randomWalk(graph, currentId);
			psiL += (double) 1 / graph.getDegree(currentId);
		}

		for (int i = 2; i < this.lengthOfPath; i++) {
			currentId = randomWalk(graph, currentId);
			psiL += (double) 1 / graph.getDegree(currentId);
			if (graph.isAdjacent(randomWalkPath.get(i - 2),
					currentId) == 1) {
				phiL += (double) 1
						/ (graph.getDegree(randomWalkPath.get(i - 1)) - 1);
			}
		}

		// System.out.println("phi:"+phi + ", psi:"+psi);
		this.averageClusterCoefficient = (phiL * lengthOfPath)
				/ (psiL * (lengthOfPath - 2));

		return this.averageClusterCoefficient;
	}

	/**
	 * 任一边增加时，更新平均聚集系数；事先保存的随机游走路径不改变
	 * 
	 * @param graph
	 *            更新后的图数据
	 * @param srcNodeId
	 *            增加的边的起点
	 * @param desNodeId
	 *            增加的边的终点
	 * @return 更新后的平均聚集系数
	 */
	public double incEstimateAverageClusterCoefficientEdgeAdd(
			Graph graph, int srcNodeId, int desNodeId) {
		ArrayList<Integer> srcPositionsInRWPath = this.nodePositionInRWPath
				.get(srcNodeId);
		ArrayList<Integer> desPositionsInRWPath = this.nodePositionInRWPath
				.get(desNodeId);
		if (srcPositionsInRWPath == null && desPositionsInRWPath == null) {
			return averageClusterCoefficient;
		} else {
			countChangedEdgeInStoredPath++;
			/* System.out.println("HIT"); */
			// 获取重新开始随机游走的起点
			int currentPosition = this.lengthOfPath;
			if (srcPositionsInRWPath != null) {
				currentPosition = srcPositionsInRWPath.get(0);
			}
			if (desPositionsInRWPath != null) {
				currentPosition = Math.min(currentPosition,
						desPositionsInRWPath.get(0));
			}

			// System.out.println("Edge Changed at "+ currentPosition);
			// 减去无效的路径在结果中的影响
			int countSub = 0;
			int countAdd = 0;
			for (int i = currentPosition; i < this.lengthOfPath; i++) {
				int currentId = this.randomWalkPath.get(i);
				int currentDegree = graph.getDegree(currentId);
				// 此时获得的度是增加了一条边以后的，因此如果正好是增量边的端点的话在t-1时刻，度是t时刻的度-1
				if (currentId == srcNodeId || currentId == desNodeId) {
					currentDegree = graph.getDegree(currentId) - 1;
				}

				psiL -= (double) 1 / currentDegree;
				// System.out.println("Psi("+this.randomWalkPath.get(i)+") is removed.");

				if (i != 0 && i < this.lengthOfPath - 2) {
					countSub++;
					if (graph.isAdjacent(
							randomWalkPath.get(i - 1),
							randomWalkPath.get(i + 1)) == 1) {
						// System.out.println("Phi("+this.randomWalkPath.get(i)+") is removed.");
						phiL -= (double) 1 / (currentDegree - 1);
					}
				}
			}
			// this.showRandomWalkPath();
			// System.out.println(this.phi);
			// System.out.println(this.psi);
			// 清除掉要被替换掉的路径，currentPosition对应的节点不清除
			for (int i = this.lengthOfPath - 1; i > currentPosition; i--) {
				int removedId = this.randomWalkPath.remove(i);
				// System.out.println(removedId+" is removed.");
				this.nodePositionInRWPath.get(removedId).remove((Object) i);
				if (this.nodePositionInRWPath.get(removedId).isEmpty()) {
					this.nodePositionInRWPath.remove(removedId);
				}
			}
			// this.showRandomWalkPath();
			// 从起点处开始随机游走，补全后面的路径

			psiL += (double) 1
					/ graph.getDegree(randomWalkPath.get(currentPosition));
			int currentId = this.randomWalkPath.get(currentPosition);
			for (int i = currentPosition + 1; i < this.lengthOfPath; i++) {
				currentId = randomWalk(graph, currentId);
				psiL += (double) 1 / graph.getDegree(randomWalkPath.get(i));
				if (i > currentPosition && i != this.lengthOfPath - 1 && i > 1) {
					countAdd++;
					// System.out.println(randomWalkPath.get(i -
					// 2)+","+randomWalkPath.get(i));

					if (graph.isAdjacent(
							randomWalkPath.get(i - 2), currentId) == 1) {
						// randomWalkPath.get(i)) == 1) {
						phiL += (double) 1
								/ (graph.getDegree(randomWalkPath.get(i - 1)) - 1);
						if (graph.getDegree(randomWalkPath.get(i - 1)) < 2) {
							System.out.println("current position: " + i
									+ ",currentId: " + currentId);
							System.out.println(randomWalkPath.get(i - 2) + ","
									+ currentId + ","
									+ randomWalkPath.get(i - 1));
							System.out.println(graph.getDegree(randomWalkPath
									.get(i - 1)));
							this.showRandomWalkPath();
							/*for (int j = currentPosition; j <= i; j++) {
								System.out.print(randomWalkPath.get(j) + "->");
							}*/
							System.exit(0);
						}
					}
				}
			}
			if (countSub != countAdd) {
				System.out.println("SUB:" + countSub + ",ADD:" + countAdd);
			}
			// this.showRandomWalkPath();
			// System.out.println(this.phi);
			// System.out.println(this.psi);
		}
		this.averageClusterCoefficient = (phiL * lengthOfPath)
				/ (psiL * (lengthOfPath - 2));
		return averageClusterCoefficient;
	}

	/**
	 * 计算全局聚集系数
	 * 
	 * @param graph
	 *            无向连通图
	 * @return 聚集系数的估计值
	 */
	public double estimateGlobalClusterCoefficient(Graph graph) {
		countChangedEdgeInStoredPath = 0;
		int currentId = java.util.concurrent.ThreadLocalRandom.current()
				.nextInt(graph.getNodeNum());
		while (graph.getNeighborList(currentId) == null) {
			currentId = java.util.concurrent.ThreadLocalRandom.current()
					.nextInt(graph.getNodeNum());
		}

		for (int i = 0; i < 2; i++) {
			currentId = randomWalk(graph, currentId);
			psiG += graph.getDegree(currentId) - 1;
		}

		for (int i = 2; i < this.lengthOfPath; i++) {
			currentId = randomWalk(graph, currentId);
			psiG += graph.getDegree(currentId) - 1;
			if (graph.isAdjacent(randomWalkPath.get(i - 2),
					currentId) == 1) {
				phiG += graph.getDegree(randomWalkPath.get(i - 1));
			}
		}

		// System.out.println("phi:"+phi + ", psi:"+psi);
		this.globalClusterCoefficient = (phiG * lengthOfPath)
				/ (psiG * (lengthOfPath - 2));

		return this.globalClusterCoefficient;
	}

	/**
	 * 任一边增加时，更新全局聚集系数；事先保存的随机游走路径不改变
	 * 
	 * @param graph
	 *            更新后的图数据
	 * @param srcNodeId
	 *            增加的边的起点
	 * @param desNodeId
	 *            增加的边的终点
	 * @return 更新后的平均聚集系数
	 */
	public double incEstimateGlobalClusterCoefficientEdgeAdd(
			Graph graph, int srcNodeId, int desNodeId) {
		ArrayList<Integer> srcPositionsInRWPath = this.nodePositionInRWPath
				.get(srcNodeId);
		ArrayList<Integer> desPositionsInRWPath = this.nodePositionInRWPath
				.get(desNodeId);
		if (srcPositionsInRWPath == null && desPositionsInRWPath == null) {
			return globalClusterCoefficient;
		} else {
			countChangedEdgeInStoredPath++;
			/* System.out.println("HIT"); */
			// 获取重新开始随机游走的起点
			int currentPosition = this.lengthOfPath;
			if (srcPositionsInRWPath != null) {
				currentPosition = srcPositionsInRWPath.get(0);
			}
			if (desPositionsInRWPath != null) {
				currentPosition = Math.min(currentPosition,
						desPositionsInRWPath.get(0));
			}

			// System.out.println("Edge Changed at "+ currentPosition);
			// 减去无效的路径在结果中的影响
			int countSub = 0;
			int countAdd = 0;
			for (int i = currentPosition; i < this.lengthOfPath; i++) {
				int currentId = this.randomWalkPath.get(i);
				int currentDegree = graph.getDegree(currentId);
				// 此时获得的度是增加了一条边以后的，因此如果正好是增量边的端点的话在t-1时刻，度是t时刻的度-1
				if (currentId == srcNodeId || currentId == desNodeId) {
					currentDegree = graph.getDegree(currentId) - 1;
				}

				psiG -= currentDegree -1;
				// System.out.println("Psi("+this.randomWalkPath.get(i)+") is removed.");

				if (i != 0 && i < this.lengthOfPath - 2) {
					countSub++;
					if (graph.isAdjacent(
							randomWalkPath.get(i - 1),
							randomWalkPath.get(i + 1)) == 1) {
						// System.out.println("Phi("+this.randomWalkPath.get(i)+") is removed.");
						phiG -= currentDegree ;
					}
				}
			}
			// this.showRandomWalkPath();
			// System.out.println(this.phi);
			// System.out.println(this.psi);
			// 清除掉要被替换掉的路径，currentPosition对应的节点不清除
			for (int i = this.lengthOfPath - 1; i > currentPosition; i--) {
				int removedId = this.randomWalkPath.remove(i);
				// System.out.println(removedId+" is removed.");
				this.nodePositionInRWPath.get(removedId).remove((Object) i);
				if (this.nodePositionInRWPath.get(removedId).isEmpty()) {
					this.nodePositionInRWPath.remove(removedId);
				}
			}
			// this.showRandomWalkPath();
			// 从起点处开始随机游走，补全后面的路径

			psiG += graph.getDegree(randomWalkPath.get(currentPosition))-1;
			int currentId = this.randomWalkPath.get(currentPosition);
			for (int i = currentPosition + 1; i < this.lengthOfPath; i++) {
				currentId = randomWalk(graph, currentId);
				psiG += graph.getDegree(randomWalkPath.get(i)) -1;
				if (i > currentPosition && i != this.lengthOfPath - 1 && i > 1) {
					countAdd++;
					// System.out.println(randomWalkPath.get(i -
					// 2)+","+randomWalkPath.get(i));

					if (graph.isAdjacent(
							randomWalkPath.get(i - 2), currentId) == 1) {
						// randomWalkPath.get(i)) == 1) {
						phiG += graph.getDegree(randomWalkPath.get(i - 1)) ;
						if (graph.getDegree(randomWalkPath.get(i - 1)) < 2) {
							System.out.println("current position: " + i
									+ ",currentId: " + currentId);
							System.out.println(randomWalkPath.get(i - 2) + ","
									+ currentId + ","
									+ randomWalkPath.get(i - 1));
							System.out.println(graph.getDegree(randomWalkPath
									.get(i - 1)));
							this.showRandomWalkPath();
							/*for (int j = currentPosition; j <= i; j++) {
								System.out.print(randomWalkPath.get(j) + "->");
							}*/
							System.exit(0);
						}
					}
				}
			}
			if (countSub != countAdd) {
				System.out.println("SUB:" + countSub + ",ADD:" + countAdd);
			}
			// this.showRandomWalkPath();
			// System.out.println(this.phi);
			// System.out.println(this.psi);
		}
		this.globalClusterCoefficient = (phiG * lengthOfPath)
				/ (psiG * (lengthOfPath - 2));

		return this.globalClusterCoefficient;
	}
	
	public void showRandomWalkPath() {
/*		for (int i = 0; i < this.randomWalkPath.size() - 1; i++) {
			System.out.print(this.randomWalkPath.get(i) + "->");
		}*/
		System.out.println(this.randomWalkPath.get(this.randomWalkPath.size() - 1));
		if (this.randomWalkPath.size() > this.lengthOfPath) {
			System.err.println("Random Walk Path overflow "
					+ (this.randomWalkPath.size() - this.lengthOfPath));
			System.exit(0);
		}
	}

	private void updateRandomWalkPath(Graph graph, int xSrc,
			boolean bForward) {
		if (bForward == true) {
			for (int i = xSrc; i < this.randomWalkPath.size() - 1; i++) {
				int currentId = randomWalkPath.get(i);
				if (i == xSrc) {
					psiL -= (double) 1 / (graph.getDegree(currentId) + 1)
							/ this.lengthOfPath;
					phiL -= (double) 1 / (graph.getDegree(currentId) + 1)
							/ (this.lengthOfPath - 2);
				} else {
					psiL -= (double) 1 / (graph.getDegree(currentId))
							/ this.lengthOfPath;
					phiL -= (double) 1 / (graph.getDegree(currentId))
							/ (this.lengthOfPath - 2);
				}
				try {
					currentId = randomWalk(graph, currentId);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				int position = i;
				if (graph.isAdjacent(
						randomWalkPath.get(position - 1),
						randomWalkPath.get(position + 1)) == 1) {
					phiL -= (double) 1 / (graph.getDegree(currentId) - 2)
							/ (this.lengthOfPath - 2);
					phiL += (double) 1 / (graph.getDegree(currentId) - 1)
							/ (this.lengthOfPath - 2);
				}
			}
		} else {
              
		}

	}

	public int getCountChangedEdgeInStoredPath() {
		return countChangedEdgeInStoredPath;
	}

}
