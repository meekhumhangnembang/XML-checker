import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class test {
    public static void main(String[] args) {
    Map<Integer, List<Integer>> data = new HashMap<>();

    data.put(0, new ArrayList<>());
    data.put(1, new ArrayList<>());
    data.put(2, new ArrayList<>());
    data.get(0).add(10); // apple
    data.get(0).add(11); // orange

    data.get(1).add(20); // carrot
    data.get(1).add(21); // potato

    System.out.println("first line " + data.get(1).get(1));
            //Output: {0=[10, 11], 1=[20, 21], 2=[]}

    }
    
}
