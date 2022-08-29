package operation.prolog_operation;


import org.jpl7.Query;
import org.jpl7.Term;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrologOperation {
    private static final String _dir = "src/operation/prolog_operation/";
    private static final String file_name = "knowledge_base.pl";
    private static final String path = "['"+_dir+file_name+"'].";

    public boolean isConnected(){
        Query query = new Query(path);
        return query.hasSolution();
    }

    public void show_sorted_list(Map<String, Integer> map, String type, JTextPane textPane, String order) {
        Query query = new Query("retractall(cases_country(X,Y)).");
        query.oneSolution();
        List<String> solutions = get_country();
        List<Integer> cases = map.values()
                                 .stream()
                                 .toList();

        for(int i = 0; i < cases.size(); i ++){
            String operations = "assert(cases_country(" + cases.get(i) + "," + solutions.get(i) + ")).";
            query = new Query(operations);
            query.hasSolution();
        }
        List<String> country_cases = get_cases();

        String create_list;
        String answer = country_cases.size()>0?"["+country_cases.get(0)+"]":null;
        for(int i = 1; i < country_cases.size(); i ++){
            create_list = "create_list(" + answer + "," + country_cases.get(i) + ", Mylist).";
            query = new Query(create_list);
            answer = query.oneSolution().get("Mylist").toString();
        }
        String sorting = "merge_sort("+answer+",SL).";
        query = new Query(sorting);
        List<String> sorted_list;
        if(order.equals("Ascending Order"))
            sorted_list = Arrays.stream(query.oneSolution().get("SL").listToTermArray())
                    .map(s -> s.toString().replaceAll("\'|-|\\(|\\)", "")
                            .replace(", ", "-")).toList();
        else{
            answer = query.oneSolution().get("SL").toString();
            String reverse_query = "reverseList(" + answer + ", RL).";
            query = new Query(reverse_query);
            sorted_list = Arrays.stream(query.oneSolution().get("RL").listToTermArray())
                    .map(s -> s.toString().replaceAll("\'|-|\\(|\\)", "")
                            .replace(", ", "-")).toList();
        }
        List<String> country = get_country_by_case(sorted_list);
        AtomicReference<String> to_set = new AtomicReference<>();
        to_set.set(type+" in " + order + "\n-----------------------------------------------------\n");
       for(int i = 0; i < country.size(); i ++){
           to_set.set(to_set.get() + formatCountry(country.get(i)) + ": " + sorted_list.get(i) + " case(s)\n");
       }
        textPane.setText(to_set.get());

    }
    private List<String> get_country_by_case(List<String> list){
        List<String> countries = new ArrayList<>();
        list.forEach(s->{
            String q = "cases_country("+s+",Y).";
            Query query = new Query(q);
            countries.add(query.oneSolution().get("Y").toString());
        });
        return countries;
    }

    private static List<String> get_cases() {
        List<String> solutions = new ArrayList<>();
        Query q = new Query("cases_country(X,Y)");
        Map<String, Term> sols;
//        Map<String, String> to_return = new HashMap<>();
        while(q.hasMoreSolutions()){
            sols = q.nextSolution();
//            solutions.add("("+sols.get("X").toString() +"," + sols.get("Y").toString() + ")");
            String to_add = sols.get("X").toString();

            solutions.add(to_add);
//            to_return.put(sols.get("Y").toString(),sols.get("X").toString());
        }
        return solutions;
    }

    private static List<String> get_country() {
        List<String> solutions = new ArrayList<>();
        Query q = new Query("country(X)");
        Map<String, Term> sols;
        while(q.hasMoreSolutions()){
            sols = q.nextSolution();
            solutions.add(sols.get("X").toString());
        }
        return solutions;
    }
    private String formatCountry(String s){
        Matcher m = Pattern.compile("(_\\w)").matcher(s);
        StringBuffer result = new StringBuffer();
        while(m.find())
            m.appendReplacement(result,m.group(0).toUpperCase());
        m.appendTail(result);
        s = result.toString().substring(0,1).toUpperCase() + result.toString().substring(1);
        return s;
    }
}
