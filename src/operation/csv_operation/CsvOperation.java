package operation.csv_operation;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CsvOperation {
    private static final String confirmed_path = ".\\dataset\\time_series_covid19_confirmed_global.csv";
    private static final String deaths_path = ".\\dataset\\time_series_covid19_deaths_global.csv";
    private static final String recovered_path = ".\\dataset\\time_series_covid19_recovered_global.csv";
    private static final List<List<String>> confirmed_list = getList(confirmed_path);
    private static final List<List<String>> deaths_list = getList(deaths_path);
    private static final List<List<String>> recovered_list = getList(recovered_path);
    private static final Map<String,Integer> country_total_confirmed = total_cases(confirmed_list);
    private static final Map<String,Integer> country_total_deaths = total_cases(deaths_list);
    private static final Map<String,Integer> country_total_recovered = total_cases(recovered_list);
    private static final String date = "\\d\\d?/\\d\\d?/\\d{2}";

    private static List<List<String>> getList(String path){
        BufferedReader br = null;
        List<List<String>> data = new ArrayList<>();
        try {
            br = new BufferedReader(new FileReader(path));
            String line;
            while((line = br.readLine()) != null) {
                List<String> lists = Arrays.asList(line.replace(", ","|").split(","));
                List<String> another = new ArrayList<>();
                lists.forEach(string -> another.add(string.replace("|", ", ")
                                             .replace("\"", "")));
                data.add(another);
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if(br!= null)
                    br.close();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
        return data;
    }
    public static Map<String, Integer> total_cases(List<List<String>> lists){
        final Map<String,Integer> country_total = new HashMap<>();
        lists.stream()
                .skip(1)
                .forEach(in_list -> {
                    Optional <String> total_optional = in_list
                            .stream()
                            .skip(4)
                            .max(Comparator.comparingInt(Integer::valueOf));
                    int total = Integer.parseInt(total_optional.orElse("-1"));
                    if (total != -1) {
                        if (country_total.containsKey(in_list.get(1))) {
                            country_total.put(in_list.get(1),country_total.get(in_list.get(1))+ total);
                        }else
                            country_total.put(in_list.get(1), total);
                    }
                });
        return new TreeMap<>(country_total);
    }
    public static <V, T> void show_result_all(Map<T, V> map, JTextPane textPane1, String selectedCountry) {
        List<String> information = new ArrayList<>();
        if(!selectedCountry.equals("All country")){
            map = map.entrySet().stream()
                     .filter(s->s.getKey().equals(selectedCountry))
                     .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        map.forEach((key, value) -> {
            String info = "------------------------------------------\nCountry: " + key + "\nTotal cases: " + value + "\n";
            information.add(info);
        });
        AtomicReference<String> to_set = new AtomicReference<>("");
        information.forEach(s-> to_set.set(to_set.get()+s));
        textPane1.setText(textPane1.getText()+to_set.get());
    }
    public static <V, T> void show_result_time(Map<T, Map<T,V>> map, JTextPane textPane1, String selectedCountry) {
        List<String> information = new ArrayList<>();
        if(!selectedCountry.equals("All country")){
            map = map.entrySet().stream()
                    .filter(s->s.getKey().equals(selectedCountry))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        map.forEach((key, value) -> {
            String value_string = value.entrySet()
                                       .stream()
                                       .map(tvEntry -> tvEntry.getKey() + " = " + tvEntry.getValue() + " case(s)")
                    .collect(Collectors.joining("\n"));
            String info = "------------------------------------------\nCountry: " + key + "\n" + value_string + "\n";
            information.add(info);
        });
        AtomicReference<String> to_set = new AtomicReference<>("");
        information.forEach(s-> to_set.set(to_set.get()+s));
        textPane1.setText(textPane1.getText()+to_set.get());
    }
    public static TreeMap<String, Map<String, Integer>> compute_daily(List<List<String>> lists) {
        final Map[] data = new Map[]{new HashMap<>()};
        List<String> header = lists.get(0);
        Optional<Integer> start_index = get_start_index(header.size(),header);
        lists.stream().skip(1).forEach(strings -> {
            String country = strings.get(1);
            Map<String, Integer> day_case = new HashMap<>();
            final int[] header_num = {start_index.orElse(-1)};
            final int[] previous_num = {0};
            strings.stream()
                  .skip(header_num[0])
                  .map(Integer::parseInt)
                  .forEach(integer -> {
                      if (previous_num[0] == 0) {
                          day_case.put(formatDate(header.get(header_num[0])),integer);
                      } else
                          day_case.put(formatDate(header.get(header_num[0])),integer- previous_num[0]);
                      previous_num[0] = integer;
                      header_num[0]  ++;
                  });
            data[0] = add_cases_into_data(data[0], day_case, country);
        });
        return new TreeMap<String, Map<String, Integer>>(data[0]);
    }
    private static String formatDate(String date){
        String year = date.split("/")[2];
        String month = date.split("/")[0];
        String day = date.split("/")[1];
        year = "20" + year;
        month = month.length()==2?month:"0"+month;
        day = day.length()==2?day:"0"+day;
        date = year + "/" + month + "/" + day;
        return date;
    }

    private static Optional<Integer> get_start_index(int size, List<String> header) {
        return Optional.of(IntStream.range(0, size)
                       .filter(i -> Pattern.matches(date, header.get(i)))
                       .findFirst()
                       .orElse(-1));
    }

    public static TreeMap<String, Map<String, Integer>> compute_weekly(List<List<String>> lists) {
        final Map[] data = new Map[]{new HashMap<>()};
        List<String> header = lists.get(0);
        lists.stream().skip(1).forEach(strings -> {
            String country = strings.get(1);
            final Map<String, Integer> week_case = new HashMap<>();
            int[] header_num = {0};
            int[] week_count = {1};
            int[] day_count = {0};
            int[] day_1_index = {0};
            strings.forEach(s -> {
                if(Pattern.matches(date,header.get(header_num[0]))){
                    day_count[0] ++;
                    if(day_count[0] == 2)
                        day_1_index[0] = header_num[0]-1;
                    if (day_count[0] == 8){
                        String weeks = formatDate(header.get(day_1_index[0])) + " ~ " + formatDate(header.get(header_num[0]-1));
                        day_count[0] = 1;
                        week_case.put(getWeeks(week_count[0],weeks),get_week_case(header_num[0]-1,day_1_index[0]-1,strings));
                        week_count[0] ++;
                    }if(week_case.isEmpty() && day_count[0] == 5){
                        day_count[0] = 1;
                        day_1_index[0] = header_num[0];
                        String weeks = formatDate(header.get(4)) + " ~ " + formatDate(header.get(header_num[0]-1));
                        week_case.put(getWeeks(week_count[0],weeks) , Integer.parseInt(strings.get(header_num[0]-1)));
                        week_count[0] ++;
                    }
                }
                header_num[0] ++;
            });
            data[0] = add_cases_into_data(data[0],week_case,country);
        });
        return new TreeMap<String, Map<String, Integer>>(data[0]);
    }


    public static TreeMap<String, Map<String, Integer>> compute_monthly(List<List<String>> lists) {
        final Map<Integer, String> indexes = new HashMap<>();
        List<String> header = lists.get(0);
        final Map<String, Map<String, Integer>>[] data = new Map[]{new HashMap<>()};
        final String[] current_month = {null};
        Optional<Integer> start_index = get_start_index(header.size(),header);
        final int[] header_num = {start_index.orElse(-1)};
        header.stream().filter(s -> Pattern.matches(date,s)).forEach(s -> {
            String this_month = s;
            String shorten = this_month.substring(0,2).replace("/","");
            if(current_month[0] == null || !shorten.equals(current_month[0])) {
                current_month[0] = shorten;
                this_month = get_full_month(this_month, shorten);
                indexes.put(header_num[0],this_month);
            }
            header_num[0]++;
        });

        final Map<Integer, String> sorted_indexes = new TreeMap<>(indexes);
        List<Integer> split_month_indexes = sorted_indexes.keySet().stream().sorted().toList();
        lists.stream().skip(1).forEach(strings -> {
            final String[] country_name = {null};
            final String[] description = {""};
            final int[] previous_index = {0};
            Map<String, Integer> month_case = new HashMap<>();
            split_month_indexes.forEach(integer -> {
                if (country_name[0] == null)
                    country_name[0] = strings.get(1);
                else if (month_case.isEmpty())
                    month_case.put(description[0], Integer.parseInt(strings.get(integer-1)));
                else
                    month_case.put(description[0],
                            Integer.parseInt(strings.get(integer-1))
                                    - Integer.parseInt(strings.get(previous_index[0] -1)));
                description[0] = sorted_indexes.get(integer);
                previous_index[0] = integer;
            });
            month_case.put(description[0],
                    Integer.parseInt(strings.get(strings.size()-1))
                            - Integer.parseInt(strings.get(previous_index[0]-1)));
            data[0] = add_cases_into_data(data[0],month_case,country_name[0]);
        });
        return new TreeMap<>(data[0]);
    }

    private static String get_full_month(String this_month, String shorten) {
        this_month = "20" + this_month.substring(this_month.length()-2);
        switch (shorten) {
            case "1" -> this_month += " - (01)January";
            case "2" -> this_month += " - (02)February";
            case "3" -> this_month += " - (03)March";
            case "4" -> this_month += " - (04)April";
            case "5" -> this_month += " - (05)May";
            case "6" -> this_month += " - (06)June";
            case "7" -> this_month += " - (07)July";
            case "8" -> this_month += " - (08)August";
            case "9" -> this_month += " - (09)September";
            case "10" -> this_month += " - (10)October";
            case "11" -> this_month += " - (11)November";
            case "12" -> this_month += " - (12)December";
        }
        return this_month;
    }
    private static Integer get_week_case(int day_7, int previous_day_7, List<String> strings) {
        return Integer.parseInt(strings.get(day_7)) - Integer.parseInt(strings.get(previous_day_7));
    }
    private static String getWeeks(int i, String weeks) {
        if (i < 10)
            return "Week **" + i + ": " + weeks;
        else if (i < 100)
            return "Week *" + i  + ": " + weeks;
        else
            return "Week " + i  + ": " + weeks;
    }


    private static Map<String, Map<String, Integer>> add_cases_into_data(Map<String, Map<String, Integer>> data,
                                                                         Map<String, Integer> cases, String country) {
        if(data.containsKey(country)){
            Map<String, Integer> to_change = data.get(country);
            for(Map.Entry<String, Integer> s: cases.entrySet())
                to_change.put(s.getKey(), to_change.get(s.getKey()) + s.getValue());
            data.put(country, new TreeMap<>(to_change));
        }else
            data.put(country, new TreeMap<>(cases));
        return data;
    }
    public static void find_lowest(Map<String, Map<String, Integer>> data,
                                   JTextPane textPane1, String timeGroup, String caseType, String selected_country) {
        AtomicReference<String> to_set = new AtomicReference<>();
        to_set.set("Lowest "+ caseType +" of " + selected_country + " in " +timeGroup+"\n");
        if(!selected_country.equals("All country")){
            data = data.entrySet()
                    .stream()
                    .filter(stringMapEntry -> stringMapEntry.getKey().equals(selected_country))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        data.forEach((country, data_set) -> {
            int min = min_values(data_set).orElse(-1)>0?min_values(data_set).get():0;
            to_set.set(to_set.get()+"----------------------------------------\n"
                                + country + ": " + min + " case(s)\n");
            keys(data_set, min)
                    .ifPresent(key -> key
                            .forEach(s -> to_set.set(to_set.get()+s+"\n")));
        });
        textPane1.setText(to_set.get());
    }
    public static void find_highest(Map<String, Map<String, Integer>> data, JTextPane textPane1,
                                    String timeGroup, String caseType, String selected_country) {
        AtomicReference<String> to_set = new AtomicReference<>();
        to_set.set("Highest "+ caseType +" of " + selected_country + " in "+timeGroup+'\n');
        if(!selected_country.equals("All country")){
            data = data.entrySet()
                    .stream()
                    .filter(stringMapEntry -> stringMapEntry.getKey().equals(selected_country))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }
        data.forEach((country, data_set) -> {
            int max = max_values(data_set).orElse(-1)>0?max_values(data_set).get():0;
            to_set.set(to_set.get() + "----------------------------------------\n"
                    + country + ": " + max + " case(s)\n");
            keys(data_set, max)
                    .ifPresent(key -> key
                            .forEach(s->to_set.set(to_set.get()+s+"\n")));
        });
        textPane1.setText(to_set.get());
    }
    private static <T,V> Optional<Stream<T>> keys(Map<T,V> map, V value){
        return Optional.
                of(map.entrySet()
                        .stream()
                        .filter(entry -> value
                                .equals(entry.getValue()))
                        .map(Map.Entry::getKey));
    }
    private static Optional<Integer> min_values(Map<String, Integer> data) {
        return Optional
                .ofNullable(data.entrySet()
                        .stream()
                        .min(Map.Entry
                                .comparingByValue())
                        .orElse(null)
                        .getValue());
    }

    private static Optional<Integer> max_values(Map<String, Integer> data) {
        return Optional
                .ofNullable(data
                        .entrySet()
                        .stream()
                        .max(Map.Entry
                                .comparingByValue())
                        .orElse(null)
                        .getValue());

    }
    public static  Map<String, Integer> get_total_confirmed(){
        return country_total_confirmed;
    }
    public static Map<String, Integer> get_total_deaths(){
        return country_total_deaths;
    }
    public static Map<String, Integer> get_total_recovered(){
        return country_total_recovered;
    }
    public static List<List<String>> getConfirmed_list(){return confirmed_list;}
    public static List<List<String>> getDeaths_list(){return deaths_list;}
    public static List<List<String>> getRecovered_list(){return recovered_list;}
//    private static final Map<String,Integer> country_total_confirmed = total_cases(confirmed_list);
//    private static final Map<String,Integer> country_total_deaths = total_cases(deaths_list);
//    private static final Map<String,Integer> country_total_recovered = total_cases(recovered_list);
//    private static final List<List<String>> confirmed_list = getList(confirmed_path);
//    private static final List<List<String>> deaths_list = getList(deaths_path);
//    private static final List<List<String>> recovered_list = getList(recovered_path);
//    public static void search_country(String country) {
//        Optional<Integer> confirmed_cases = Optional.ofNullable(country_total_confirmed
//                .entrySet()
//                .stream()
//                .filter(country_compare -> country_compare.getKey().equalsIgnoreCase(country))
//                .findAny()
//                .orElse(null)
//                .getValue());
//        Optional<Integer> death_cases = Optional.ofNullable(country_total_deaths
//                .entrySet()
//                .stream()
//                .filter(country_compare -> country_compare.getKey().equalsIgnoreCase(country))
//                .findAny()
//                .orElse(null)
//                .getValue());
//        Optional<Integer> recovered_cases = Optional.ofNullable(country_total_recovered
//                .entrySet()
//                .stream()
//                .filter(country_compare -> country_compare.getKey().equalsIgnoreCase(country))
//                .findAny()
//                .orElse(null)
//                .getValue());
//        System.out.println("Country: " + country);
//        confirmed_cases
//                .ifPresentOrElse(cases -> System.out.println("Total confirmed cases: " + cases + "(s)")
//                        , () -> System.out.println("No data for confirmed cases."));
//        death_cases
//                .ifPresentOrElse(cases -> System.out.println("Total death cases: " + cases + "(s)")
//                        , () -> System.out.println("No data for death cases"));
//        recovered_cases
//                .ifPresentOrElse(cases -> System.out.println("Total recovered cases: " + cases + "(s)")
//                        , () -> System.out.println("No data for recovered cases") );
//    }

}
