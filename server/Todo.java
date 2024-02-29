import java.util.HashSet;
import java.util.Scanner;
import java.io.*;
import java.util.ArrayList;
import java.time.*;
import java.time.format.DateTimeFormatter;
class Todo{

    static Scanner scanner;
    static File file_todo, file_completed, file_history, file_date, file_show, file_study, file_network, file_removed;
    static String string_date_on_file, which_section;
    static String string_date_today = LocalDate.now().toString();
    static ArrayList<String> arraylist_todo = new ArrayList<String>();
    static ArrayList<String> arraylist_history = new ArrayList<String>();
    static ArrayList<String> arraylist_study, arraylist_network, arraylist_removed;
    static int[] array_indexes = {0,0,0,0,0};
    static String[] array_section_strings = {"Now", "Later", "Tomorrow", "Someday", "Maybe"};
    static int int_completed;
    static PrintWriter writer;
    static boolean bool_show;
    static String[] args;

    public static void main(String[] arguments) throws FileNotFoundException{
        args = new String[arguments.length-1];
        for(int i=1; i<arguments.length; i++){
            args[i-1] = arguments[i];
        }
        Set_up_files(arguments[0]);
        Create_arraylist_from_history_file();
        Create_todo_arraylist_from_file();
        arraylist_removed = Create_arraylist_from_file(file_removed);
        int_completed = Integer.parseInt(Get_string_from_file(file_completed));
        Check_date();
        bool_show = Boolean.parseBoolean(Get_string_from_file(file_show));
        User_input();
        Print();
        Write_todo_file();
        Write_to_file(int_completed, file_completed);
        Write_to_file(string_date_today, file_date);
        Write_to_file(bool_show, file_show);
        Write_to_history_file(arraylist_history, file_history);
        Write_to_history_file(arraylist_removed, file_removed);
    }

    static String Get_day(){
        LocalDateTime myDateObj = LocalDateTime.now();    
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("E");  
    
        String formattedDate = myDateObj.format(myFormatObj);  
        return formattedDate;
    }

    static void Set_up_files(String user){
        try{
            file_todo = new File("users/"+user+"/todo.txt");
            file_completed = new File("users/"+user+"/completed.txt");
            file_history = new File("users/"+user+"/history.txt");
            file_date = new File("users/"+user+"/date.txt");
            file_show = new File("users/"+user+"/show.txt");
            file_study = new File("users/"+user+"/study.txt");
            file_network = new File("users/"+user+"/network.txt");
            file_removed = new File("users/"+user+"/removed.txt");
        }
        catch(Exception e){
            p("File not found");
            System.exit(0);
        }
    }

    static String Find_section(int index){
        int i = 0;
        while(i < array_indexes.length && index < array_indexes[i]){
            i++;
        }
        return array_section_strings[i];
    }

    static String Get_string_from_file(File file) throws FileNotFoundException{
        scanner = new Scanner(file);
        return scanner.nextLine();
    }

    static void Add_studies(ArrayList<String> arraylist, File file, int n) throws FileNotFoundException{
        arraylist = Create_arraylist_from_file(file);
        String quiz;
        for(int i = 0; i < n; i++){
            quiz = arraylist.remove(0);
            Top(quiz);
            arraylist.add(quiz);
        }
        Write_to_history_file(arraylist, file);
    }

    static void Check_date() throws FileNotFoundException{
        string_date_on_file = Get_string_from_file(file_date);
        
        if(!string_date_on_file.equals(string_date_today)){
            
            String[] dailies = {
                "take antidepressant",
                "exercise",
                "consume protein",
                "take vitamin d",
                "check email",
                "apply for job",
                "use nevermet likes",
                "use hinge likes",
                "get rid of something",
                "floss",
                "eat vegetables",
            };
            int_completed = 0;
            arraylist_history.add("");
            arraylist_history.add("===== "+string_date_today+" =====");
            String s;
            for(int i = dailies.length-1; i>=0; i--){
                s = dailies[i];
                if(!arraylist_todo.contains(s)){
                    Top(s);
                }
            }
            String today = Get_day();
            if(today.equalsIgnoreCase("sun")){
                Add("later", "take recyclables out");
            }

            while(array_indexes[2]!=array_indexes[3]){
                Move(array_indexes[2], "later");
            }
        }
    }

    static void Create_arraylist_from_history_file(){
        try{scanner = new Scanner(file_history);}catch(Exception e){}
        while(scanner.hasNextLine()){
            arraylist_history.add(scanner.nextLine());
        }
    }

    static void Create_todo_arraylist_from_file(){
        try{scanner = new Scanner(file_todo);}catch(Exception e){}
        String s, section;
        int int_i=0;
        for(int i=0; i<array_section_strings.length && scanner.hasNextLine(); i++){
            array_indexes[i] = int_i;
            section = array_section_strings[i];
            scanner.nextLine();
            while(scanner.hasNextLine()){
                s = scanner.nextLine();
                if(!s.equals("")){
                    arraylist_todo.add(s);
                    int_i++;
                } else {
                    break;
                }
            }
        }
    }

    static void Print(){
        int seperator = 1;
        p("Number of tasks completed today: "+int_completed);        
        for(int i=0; i<seperator; i++){
            if(array_indexes[i] != array_indexes[i+1]){
                Print_section(array_section_strings[i], array_indexes[i], array_indexes[i+1]);
            }
        }
        for(int i=seperator; i<array_indexes.length-1; i++){
            if(array_indexes[i] != array_indexes[i+1] && bool_show){
                Print_section(array_section_strings[i], array_indexes[i], array_indexes[i+1]);
            }
        }
        if(array_indexes[array_indexes.length-1] != arraylist_todo.size() && bool_show){
            Print_section(array_section_strings[array_indexes.length-1], array_indexes[array_indexes.length-1], arraylist_todo.size());
        }
    }

    static void Print_section(String section, int start, int end){
        p("===== "+section+" =====");
        for(int i=start; i<end; i++){
            p(i+". "+arraylist_todo.get(i));
        }
        p("");
    }

    static void Write_todo_file() throws FileNotFoundException{
        writer = new PrintWriter(new FileOutputStream(file_todo), true);
        for(int i=0; i<array_indexes.length-1; i++){
            Write_section(array_section_strings[i], array_indexes[i], array_indexes[i+1]);
        }
        Write_section(array_section_strings[array_indexes.length-1], array_indexes[array_indexes.length-1], arraylist_todo.size());
        writer.close();
    }

    static void Write_section(String section, int start, int end){
        writer.println("===== "+section+" =====");
        for(int i=start; i<end; i++){
            writer.println(arraylist_todo.get(i));
        }
        writer.println("");
    }

    static void User_input() throws FileNotFoundException{
        if(args.length == 0){
            return;
        }
        String[] command_split = args;
        String first_word = command_split[0];
        if(first_word.equalsIgnoreCase("do") || first_word.equalsIgnoreCase("add")){
            which_section = command_split[1]; // section second
            int start = 1;
            if(Check_section(which_section)){
                start++;
            }
            Add(which_section, Rebuild_string(command_split, start, command_split.length-1)); // section second
        }
        else if(first_word.equalsIgnoreCase("rm") || first_word.equalsIgnoreCase("remove")){
            int index = Integer.parseInt(command_split[1]);
            Remove(index);
        }
        else if(first_word.equalsIgnoreCase("did")){
            int index = Integer.parseInt(command_split[1]);
            Did(index);
        }
        else if(first_word.equalsIgnoreCase("reset")){
            if(command_split[1].equals("history")){
                arraylist_history.clear();
            } else if(command_split[1].equals("number")){
                int_completed = 0;
            }
        }
        else if(first_word.equalsIgnoreCase("top")){
            int index = Integer.parseInt(command_split[1]);
            Top(index);
        }
        else if(first_word.equalsIgnoreCase("mv") || first_word.equalsIgnoreCase("move")){
            int index = Integer.parseInt(command_split[1]);
            which_section = command_split[2];
            Move(index, which_section);
        }
        else if(first_word.equalsIgnoreCase("put") || first_word.equalsIgnoreCase("pt")){
            int index = Integer.parseInt(command_split[1]);
            int to = Integer.parseInt(command_split[2]);
            Move(index, to);
        }
        else if(first_word.equalsIgnoreCase("edit")){
            int index = Integer.parseInt(command_split[1]);
            arraylist_todo.set(index, Rebuild_string(command_split, 2, command_split.length-1));
        }
        else if(first_word.equalsIgnoreCase("hide")){
            bool_show = false;
        }
        else if(first_word.equalsIgnoreCase("show")){
            bool_show = true;
        }
        else if(first_word.equalsIgnoreCase("insert") || first_word.equalsIgnoreCase("in")){
            int index = Integer.parseInt(command_split[1]);
            int start = 2;
            Add(index, Rebuild_string(command_split, start, command_split.length-1)); // section second
        }
        else if(first_word.equalsIgnoreCase("a")){
            int count = 1;
            if(command_split.length>1){
                count = Integer.parseInt(command_split[1]);
            }
            Add_studies(arraylist_study, file_study, count); // section second
        }
        else if(first_word.equalsIgnoreCase("network")){
            int count = 1;
            if(command_split.length>1){
                count = Integer.parseInt(command_split[1]);
            }
            Add_studies(arraylist_network, file_network, count); // section second
        }
        else if(first_word.equalsIgnoreCase("productive")){
            Top("work on resume website");
            Top("study interview questions");
            Top("apply for job");
            Top("leet code question");
            Add_studies(arraylist_study, file_study, 1);
            Add_studies(arraylist_network, file_network, 1);
        }
    }

    static void Add(String section, String entry){
        if(!arraylist_todo.contains(entry)){
            Shift_sections(section);
            arraylist_todo.add(array_indexes[Convert_section_to_index_in_array_indexes(section)], entry);
        }
    }

    static void Shift_sections(String section){
        int int_section = Convert_section_to_index_in_array_indexes(section);
        for(int i=int_section+1; i<array_indexes.length; i++){
            array_indexes[i]++;
        }
    }

    static void Add(int index, String entry){
        if(!arraylist_todo.contains(entry)){
            Shift_sections(Find_section(index));
            arraylist_todo.add(index, entry);
        }
    }

    static String Remove(int index){
        int marker = 0;
        while(marker<array_indexes.length && index >= array_indexes[marker]){
            marker++;
        }
        for(int i = marker; i<array_indexes.length; i++){
            array_indexes[i]--;
        }
        String s = arraylist_todo.remove(index);
        Add_removed(s);
        return s;
    }

    static void Add_removed(String s){
        arraylist_removed.add(s);
        if(arraylist_removed.size()>10){
            arraylist_removed.remove(0);
        }
    }

    static void Top(int index){
        Add("now", Remove(index));
    }

    static void Top(String s){
        Add("now", s);
    }

    static void Did(int index){
        arraylist_history.add(Remove(index));
        int_completed++;
    }

    static void Move(int index, String section){
        String s = Remove(index);
        Add(section, s);
    }

    static void Move(int index, int to){
        String s = Remove(index);
        Add(to, s);
    }

    static int Convert_section_to_index_in_array_indexes(String section){
        for(int i=0; i<array_section_strings.length; i++){
            if(section.equalsIgnoreCase(array_section_strings[i])){
                return i;
            }
        }
        return 0;
    }

    static boolean Check_section(String section){
        for(int i=0; i<array_section_strings.length; i++){
            if(section.equalsIgnoreCase(array_section_strings[i])){
                return true;
            }
        }
        return false;
    }

    static void Write_to_file(Object x, File file) throws FileNotFoundException{
        writer = new PrintWriter(new FileOutputStream(file), true);
        writer.print(x);
        writer.close();
    }

    static void Write_to_history_file(ArrayList<String> arraylist, File file) throws FileNotFoundException{
        writer = new PrintWriter(new FileOutputStream(file), true);
        for(String s : arraylist){
            writer.print(s+"\n");
        }
        writer.close();
    }


    // sub functions


    static String Rebuild_string(String[] array, int first, int last){
        String s = array[first];
        for(int i=first+1; i<=last; i++){
            s += " "+array[i];
        }
        return s;
    }

    static String input(String s){
        scanner = new Scanner(System.in);
        p(s);
        return scanner.nextLine();
    }

    static void Print_arraylist(ArrayList arraylist){
        for(int i=0; i<arraylist.size(); i++){
            p(i+". "+arraylist.get(i));
        }
    }

    static void Print_array(int[] array){
        for(Integer s : array){
            System.out.print(s+" ");
        }
        p("");
    }

    static ArrayList<String> Create_arraylist_from_file(File file){
        try{scanner = new Scanner(file);}catch(Exception e){}
        ArrayList<String> list = new ArrayList<String>();
        while(scanner.hasNextLine()){
            list.add(scanner.nextLine());
        }
        return list;
    }

    static void p(Object s){
        System.out.println(s);
    }
}

/*

        try{scanner = new Scanner(file_todo);}catch(Exception e){}
        ArrayList<String> list = new ArrayList<String>();
        while(scanner.hasNextLine()){
            list.add(scanner.nextLine());
        }
        return list;




    public static void Print(){
        p("Number of tasks completed today: "+int_completed);
        p("===== NOW =====");
        Print_arraylist(arraylist_todo);
        p("");
        //p("===== LATER =====");
    }



*/