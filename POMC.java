import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Random;


public class POMC {
	private Evaluation constrain; 
	private Evaluation object_function;
	private int n;
	private double budget;
	private int total_iter_num = 0;
	private double current_feasible_max_fit;
	public LinkedList<Solution> population = new LinkedList<Solution>();
	
	public POMC(Evaluation object_function, Evaluation constrain, double budget){
		this.constrain = constrain;
		this.object_function = object_function;
		this.n = object_function.get_n();
		this.budget = budget;
		total_iter_num = n * n * n;
	}
	
	public void set_iter_num(int num){
		this.total_iter_num = num;
	}
	
	
	public Solution run_pomc(){
		Solution s0 = new Solution(n);
		s0.set_cost(constrain.evaluate(s0.get_selected_ref()));
		s0.set_fit(object_function.evaluate(s0.get_selected_ref()));
		current_feasible_max_fit = s0.get_fit();
		population.add(s0);
		
		Random rand = new Random();
		for (int i = 0; i < total_iter_num; i ++){
			int r = rand.nextInt(population.size());
			Solution s = population.get(r).mutation();
			s.set_cost(constrain.evaluate(s.get_selected_ref()));
			s.set_fit(s.get_cost() > (2 * budget) ? 0 : object_function.evaluate(s.get_selected_ref()));
			update_population(s);
			
		}
		Solution result = get_best_from_populatio();
		return result;
	}
	
	public Solution get_best_from_populatio(){
		Solution result = population.get(0);
		for (Solution z : population){
			if (z.get_cost() <= budget && z.get_fit() > result.get_fit()){
				result = z;
			}
		}
		return result;
	}
	
	public boolean update_population(Solution s){
		boolean accepted = true;
		for (Solution z : population){
			if (z.get_fit() > s.get_fit() && z.get_cost() <= s.get_cost() || 
					z.get_fit() >= s.get_fit() && z.get_cost() < s.get_cost()){
				accepted = false;
				return accepted;
			}
		}
		ListIterator<Solution> iter = population.listIterator(); 
		
		while (iter.hasNext()){
			Solution z = iter.next();
			if (z.get_fit() <= s.get_fit() && z.get_cost() >= s.get_cost() )
				iter.remove();
		}
		iter = null;
		if (s.get_cost() < 0){
			population.addFirst(s);
		}
		else{
			iter = population.listIterator();
			int index = - 1;
			if (iter.hasNext()){
				iter.next();
				for (int i = 1; i < population.size(); i ++){
					Solution z = iter.next();
					if (z.get_fit() / z.get_cost() < s.get_fit() / s.get_cost()){
						index = i;
						break;
					}
				}
			}
			if (index == - 1){
				population.addLast(s);
			}
			else{
				population.add(index, s);
			}
			//current_max_fit = current_max_fit > s.get_fit() ? current_max_fit : s.get_fit();
			if (s.get_cost() <= budget) 
				current_feasible_max_fit = current_feasible_max_fit > s.get_fit() ? current_feasible_max_fit : s.get_fit();
		}
		
		if (population.size() > n){
			if (population.getLast().get_fit() != current_feasible_max_fit)
				population.removeLast();
			else
				population.remove(population.size() - 2);
		}
		return accepted;
	}
	
}
