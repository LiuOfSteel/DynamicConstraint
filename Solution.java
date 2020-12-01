import java.util.Random;


public class Solution {

	private int [] selected;
	private double fit;
	private double cost;
	private int len;
	private int norm;
	public Solution mutation(){
		int [] s = new int [len];
		double p = 1.0 / len;
		Random rand = new Random();
		for (int i = 0; i < len; i ++)
			if (rand.nextDouble() < p){
				s[i] =  selected [i] == 1 ? 0 : 1; 
			}
			else{
				s[i] = selected[i];
			}
		return new Solution(s);
	}
	
	public Solution (int [] s){
		this.selected = s;
		this.len = s.length;
		for (int i = 0; i < s.length; i ++){
			if (s[i] == 1)
				this.norm = this.norm + 1;
		}
	}
	
	public Solution (int [] s, double fit, double cost){
		this.selected = s;
		this.len = s.length;
		this.fit = fit;
		this.cost = cost;
		for (int i = 0; i < s.length; i ++){
			if (s[i] == 1)
				this.norm = this.norm + 1;
		}
	}
	
	public int get_selected(int index){
		return selected[index];
	}
	
	public void set_selected(int index, int value){
		if (selected[index] == 0 && value == 1) this.norm ++;
		if (selected[index] == 1 && value == 0) this.norm --;
		this.selected[index] = value;
	}
	
	public Solution(int len){
		this.selected = new int[len];
		this.len = len;
		this.norm = 0;
	}
	
	public int [] get_selected_val (){
		int [] s = new int [len];
		for (int i = 0; i < len; i ++)
			s[i] = selected[i];
		return s;
	}
	
	public int [] get_selected_ref (){
		return this.selected;
	}
	
	public Solution copy(){
		Solution s = new Solution(this.get_selected_val());
		s.set_cost(this.cost);
		s.set_fit(this.fit);
		return s;
	}
	
	public int get_norm(){
		return this.norm;
	}
	
	public boolean equals(Object z){
		Solution s = (Solution) z;
		if (s.len != len)
			return false;
		for (int i = 0; i < len; i ++){
			if (this.selected[i] != s.get_selected(i))
				return false;
		}
		return true;
	}
	
	
	public double get_cost()	{	return this.cost;	}
	public double get_fit ()	{	return this.fit;	}
	public void set_cost(double cost)	{	this.cost = cost;	}
	public void set_fit(double fit)		{	this.fit = fit; 	}
	
	public static void main(String [] args){
		Solution a = new Solution(10);
		Solution b = new Solution(10);
		System.out.println(a.equals(b));
	}
}
