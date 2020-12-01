import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;

public class Example {
	public static void main(String [] args) throws IOException{
		/* Goal:
		 * 	max  f(x)
		 * 	s.t. c(x) <= b
		 */
		
		/*
		 * In this example
		 * f: Influence Maximization
		 * c: Routing Constrain (minimum spanning tree)
		 */
		
		
		//set budgt b
		double b = 10;
		//MaxInfluence and Constrain below inherit the abstract class Evaluation
		//you can create your own classes by inheriting Evaluation
		//when you inherit Evalution class, you need to implement method evaluate(int []) and get_n()
		//f is objective function (Influence spread)
		//c is constrain function (minimum spanning tree)
		MaxInfluence f = new MaxInfluence("data/BA_N200_M2_W01_Graph.txt", "data/BA_N200_M2_W01_Weight.txt");
		Constrain c = new Constrain("data/ER_N200_P001_Graph.txt", "data/ER_N200_P001_Weight.txt");
		c.set_visit_cost(0.1);
		//pass arguments to pomc
		POMC pomc = new POMC( (Evaluation) f, (Evaluation) c, b);
		//set iteration number, default n^3
		pomc.set_iter_num(10000);
		//run pomc
		Solution s = pomc.run_pomc();
		//print fitness
		System.out.println(s.get_fit());
		//print cost
		System.out.println(s.get_cost());
		//print solution
		for (int i = 0; i < f.get_n(); i ++)
			System.out.print(s.get_selected_ref()[i] + " ");
		System.out.println();
	}
}

class MaxInfluence extends Evaluation{
	public ArrayList<LinkedList<NodeRelation>> adj_list = new ArrayList<LinkedList<NodeRelation>>();
	private int node_num;
	private int repeat_times = 100;
	
	public int get_n(){
		return node_num;
	}
	
	public MaxInfluence(String graph_file_name, String weight_file_name) throws IOException{
		Graph graph = new Graph(graph_file_name, weight_file_name);
		this.node_num = graph.get_node_num();
		this.adj_list = graph.ajd_list;
	}
	
	public double evaluate(int [] selected){
		double influence = 0;
		for (int i = 0; i < repeat_times; i ++){
			influence += calculate_influence_once(selected);
		}
		return influence / repeat_times;
	}
	
	public double calculate_influence_once(int [] selected){
		int WHITE = 0;
		int BLACK = 1;
		int influence = 0;
		int [] color = new int[node_num];
		Random rand = new Random();
		for (int i = 0; i < node_num; i ++)
			color[i] = WHITE;
		
		Stack<Integer> stack = new Stack<Integer>();
		for (int i = 0; i < node_num; i ++)
			if (selected[i] == 1) {
				stack.push(new Integer(i));
				color [i] = BLACK;
			}
		influence += stack.size();
		
		while(!stack.isEmpty()){
			int v = stack.pop();
			LinkedList<NodeRelation> link = adj_list.get(v);
			for (NodeRelation relation : link) {
				int id = relation.get_end_id();
				double prob = relation.get_weight();
				if (color [id] == WHITE && prob > 0){
					if (rand.nextDouble() < prob){
						color [id] = BLACK;
						influence ++;
						stack.push(id);
					}
				}	
			}
		}	
		rand = null;
		return influence;
	}

}

class Constrain extends Evaluation{
	public ArrayList<LinkedList<NodeRelation>> adj_list = new ArrayList<LinkedList<NodeRelation>>();
	private int node_num;
	private double visit_cost = 0;
	
	public int get_n(){
		return node_num;
	}
	
	public void set_visit_cost(double visit_cost){
		this.visit_cost = visit_cost;
	}
	
	public Constrain(String graph_file_name, String weight_file_name) throws IOException{
		Graph graph = new Graph(graph_file_name, weight_file_name);
		this.node_num = graph.get_node_num();
		this.adj_list = graph.ajd_list;
	}
	
	public double evaluate(int [] selected){  // min span tree
		double total_weight = 0;
		int count = 0; //count num of nodes
		//as total node num of whole graph (selected.length) may be large
		//we encode the chosen nodes as 0,1,...
		//we use map_o2c and map_c2o to transfer original id and current id
		HashMap <Integer, Integer> map_o2c = new HashMap<Integer, Integer>();// original id to current id
		HashMap <Integer, Integer> map_c2o = new HashMap<Integer, Integer>();// current id to original id
		for (int i = 0; i < selected.length; i ++){
			if (selected[i] == 1) {
				map_o2c.put(i, count);
				map_c2o.put(count, i);
				count ++;
			}
		}
		if (count == 0) return  - 1;
		int [] c_pre_id = new int [count]; // id -> c_pre_id[id] is the shortest path to tree 
		double [] c_dis = new double [count]; // c_dis[current_id]=distance to tree
		//to find a node (crrent id is i) info in min span tree, you can visit tree.get(i)
		//but the info int tree.get(i) is origin id
		ArrayList <LinkedList<NodeRelation>> tree = new ArrayList<LinkedList<NodeRelation>>();
		for (int i = 0; i < count; i ++){
			tree.add(new LinkedList<>());
			c_dis [i] = Double.MAX_VALUE;
		}
		int c_last_add_node = 0;
		c_dis[c_last_add_node] = - 1; // -1 means added
		for (int i = 0; i < count - 1; i ++){
			int o_last_add_node = map_c2o.get(c_last_add_node);
			LinkedList<NodeRelation> list = adj_list.get(o_last_add_node);
			Iterator<NodeRelation> iter = list.iterator();  
			while (iter.hasNext()) {  
			    NodeRelation relation = iter.next();
			    if (map_o2c.containsKey(relation.get_end_id())){// we don't care the node unselected
			    	int id = map_o2c.get(relation.get_end_id());
				    double weight = relation.get_weight();
				    if (c_dis[id] >= 0 && weight < c_dis[id]){
				    	c_dis[id] = weight;
				    	c_pre_id [id] = c_last_add_node;
				    }
			    }
			    
			}
			double min_dis = Double.MAX_VALUE;
			int min_index = - 1;
			for (int j = 0; j < count; j ++){
				if (c_dis[j] >= 0 && c_dis[j] <= min_dis){
					min_dis = c_dis[j];
					min_index = j;
				}
			}
			if (min_dis == Double.MAX_VALUE)	return Double.MAX_VALUE;
			total_weight += min_dis;
			tree.get(c_pre_id[min_index]).add(new NodeRelation(map_c2o.get(c_pre_id[min_index]), map_c2o.get(min_index), c_dis[min_index]));
			c_last_add_node = min_index;
			c_dis [c_last_add_node] = - 1;
		}
		total_weight += count * visit_cost;
		return total_weight;
	}
}

class NodeRelation {

	private int start_id;
	private int end_id;
	private double weight;
	
	public NodeRelation(){}
	
	public NodeRelation(int start_id, int end_id, double weight){
		this.start_id = start_id;
		this.end_id = end_id;
		this.weight = weight;
	}
	public void set_weight(double w){
		this.weight = w;
	}
	
	public int get_statrt_id(){
		return this.start_id;
	}
	public int get_end_id(){
		return this.end_id;
	}
	
	public double get_weight(){
		return this.weight;
	}
	
	public NodeRelation copy(){
		return new NodeRelation(this.start_id, this.end_id, this.weight);
	}
}


class Graph {
	public int edge_num = 0;
	private int node_num = 0;  
	private String graph_file_name;
	private String weight_file_name;
	public ArrayList<LinkedList<NodeRelation>> ajd_list = new ArrayList<LinkedList<NodeRelation>>();
	
	public Graph(String graph_file_name, String weight_file_name) throws IOException{
		this.graph_file_name = graph_file_name;
		this.weight_file_name = weight_file_name;
		scan_file_to_get_egde_num_and_node_num();
		initialize_vertices();
	}
	
	public int get_node_num(){
		return this.node_num;
	}
	
	public int scan_file_to_get_egde_num_and_node_num() throws IOException{
		File file_graph = new File(graph_file_name);
		Scanner scan = new Scanner(file_graph);
		String [] str;
		int start_id, end_id;
		while(scan.hasNextLine()){
			str = scan.nextLine().trim().split("\\s+");
			if (str.length > 0){
				start_id = Integer.parseInt(str[0]);
				end_id = Integer.parseInt(str[1]);
				node_num = start_id > node_num ? start_id : node_num;
				node_num = end_id > node_num ? end_id : node_num;
				edge_num ++;
			}
		}
		scan.close();
		node_num ++; 

		return 0;
	}
	
	public void initialize_vertices() throws IOException{
		ajd_list = new ArrayList<LinkedList<NodeRelation>> (node_num);
		for (int i = 0; i < node_num; i ++){
			ajd_list.add(new LinkedList<NodeRelation>());
		}
		
		File file_graph = new File(graph_file_name);
		File file_weight = new File(weight_file_name);
		Scanner scan = new Scanner(file_graph);
		Scanner scan_prob = new Scanner(file_weight);
		int start_id, end_id;
		double weight;
		for (int i = 0; i < edge_num; i ++){
			start_id = scan.nextInt();
			end_id = scan.nextInt();
			weight = scan_prob.nextDouble();
			ajd_list.get(start_id).add(new NodeRelation(start_id, end_id, weight));
		}
		scan.close();
		scan_prob.close();
	}
	
}

