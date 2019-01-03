/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ucv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author derekworth
 */
public class Main {
    
    static final int PHASE1_ONLY = 0;
    static final int PHASE2_ONLY = 1;
    static final int BOTH_SAME   = 2;
    static final int BOTH_DIFF   = 3;

    public static void main(String[] args) {
        LinkedList<UCTClass> classList = new LinkedList();
        // Track all classes and students
        HashMap<String, UCTClass> classMap = new HashMap();
        HashMap<String, Student>  studMap = new HashMap();
        
        // populate classes
        File cFile = new File("Classes.csv");
        try {
            FileReader fr = new FileReader(cFile);
            try (BufferedReader br = new BufferedReader(fr)) {
                String line = br.readLine();
                String[] tokens = line.split(",");
                if(tokens.length == 7 && tokens[0].contains("Class Number") && tokens[4].contains("Class Number")) {
                    UCTClass c = new UCTClass("unassigned", "unassigned");
                    classList.add(c);
                    classMap.put("unassigned", c);
                    while((line = br.readLine()) != null) {
                        tokens = line.split(",");
                        String p1 = tokens[0];
                        String p2 = tokens[4];
                        // create class
                        c = new UCTClass(p1, p2);
                        // add to maps
                        classList.add(c);
                        classMap.put(p1, c);
                        classMap.put(p2, c);
                    }
                }
            } 
        } catch(IOException e) { /*DO NOTHING*/ }
        
        // populate students
        File folder = new File(".");
        File[] files = folder.listFiles();
        for(File f : files) {
            if(f.getName().contains(".csv")) {
                try {
                    FileReader fr = new FileReader(f);
                    try (BufferedReader br = new BufferedReader(fr)) {
                        String line = br.readLine();
                        String[] tokens = line.split(",");
                        if(tokens.length == 25 && tokens[2].equalsIgnoreCase("\"PDSCLASS\"") && tokens[9].equalsIgnoreCase("\"CMPL_DTE\"")) {
                            while((line = br.readLine()) != null) {
                                tokens = line.split("\"");
                                String name = tokens[1];
                                String ssan = tokens[3];
                                String clas = tokens[5];
                                String trqi = tokens[7];
                                String rank = tokens[13];
                                String stat = tokens[17];
                                
                                Student stud;
                                if(studMap.containsKey(ssan)) {
                                    // get existing student
                                    stud = studMap.get(ssan);
                                } else {
                                    // create new student
                                    stud = new Student(name, ssan, trqi, rank, stat);
                                    // add student to map
                                    studMap.put(ssan, stud);
                                }
                                // point student to UCT class
                                UCTClass c = classMap.get(clas);
                                if(clas.contains("OQR")) {
                                    if(c == null) {
                                        stud.setPhase1(classMap.get("unassigned"));
                                    } else {
                                        stud.setPhase1(c);
                                    }
                                } else {
                                    if(c == null) {
                                        stud.setPhase2(classMap.get("unassigned"));
                                    } else {
                                        stud.setPhase2(c);
                                    }
                                }
                                // add student to class
                                if(c == null) {
                                    classMap.get("unassigned").addStudent(stud);
                                } else {
                                    c.addStudent(stud);
                                }
                            }
                        }
                    }
                } catch(IOException e) { /*DO NOTHING*/ }
            }
        }

        try {
            FileWriter fw = new FileWriter("Summary.csv");
            BufferedWriter bw = new BufferedWriter(fw);
            
            // print students to file
            for(UCTClass uc : classList) {
                if(uc.size()>0) {
                    int c1 = 0;
                    int c2 = 0;
                    for(Student s : uc.students()) {
                        if(s.classStatus() == PHASE1_ONLY) {
                            c1++;
                        }
                    }
                    for(Student s : uc.students()) {
                        if(s.classStatus() == BOTH_SAME) {
                            c1++;
                            c2++;
                        }
                    }
                    for(Student s : uc.students()) {
                        if(s.classStatus() == PHASE2_ONLY) {
                            c2++;
                        }
                    }
                    for(Student s : uc.students()) {
                        if(s.classStatus() == BOTH_DIFF) {
                            if(s.p1.equals(uc))
                                c1++;
                            else
                                c2++;
                        }
                    }


                    bw.write(c1 + "/" + c2 + uc + "\n");
                    for(Student s : uc.students()) {
                        if(s.classStatus() == PHASE1_ONLY) {
                            bw.write(",X,," + s.getSSAN() + "," + s.getTRQI() + "," + s.getStat() + "," + s.getRank() + ",\"" + s.getName() + "\"\n");
                        }
                    }
                    for(Student s : uc.students()) {
                        if(s.classStatus() == BOTH_SAME) {
                            bw.write(",X,X," + s.getSSAN() + "," + s.getTRQI() + "," + s.getStat() + "," + s.getRank() + ",\"" + s.getName() + "\"\n");
                        }
                    }
                    for(Student s : uc.students()) {
                        if(s.classStatus() == PHASE2_ONLY) {
                            bw.write(",,X," + s.getSSAN() + "," + s.getTRQI() + "," + s.getStat() + "," + s.getRank() + ",\"" + s.getName() + "\"\n");
                        }
                    }
                    for(Student s : uc.students()) {
                        if(s.classStatus() == BOTH_DIFF) {
                            if(s.p1.equals(uc))
                                bw.write(",O,," + s.getSSAN() + "," + s.getTRQI() + "," + s.getStat() + "," + s.getRank() + ",\"" + s.getName() + "\"\n");
                            else
                                bw.write(",,O," + s.getSSAN() + "," + s.getTRQI() + "," + s.getStat() + "," + s.getRank() + ",\"" + s.getName() + "\"\n");
                        }
                    }
                    bw.write("\n");

                }
            }
        
            bw.close();
        } catch(IOException ex) { }
    }
    
    public static String getClassStatus(int stat) {
        switch(stat) {
            case PHASE1_ONLY:
                return "P1";
            case PHASE2_ONLY:
                return "P2";
            case BOTH_SAME:
                return "BS";
            default:
                return "BD";
        }
    }
    
    public static class UCTClass {
        String p1, p2;
        LinkedList<Student> studList;
        HashMap<String, Student> studMap;
        
        public UCTClass(String p1, String p2) {
            studList = new LinkedList();
            studMap = new HashMap();
            this.p1 = p1;
            this.p2 = p2;
        }
        
        public void addStudent(Student stud) {
            if(!studMap.containsKey(stud.getSSAN())) {
                studList.add(stud);
                studMap.put(stud.getSSAN(), stud);
            }
        }
        
        public LinkedList<Student> students() {
            return studList;
        }
        
        public int size() {
            return studList.size();
        }
        
        @Override
        public String toString() {
            if(p1.equals("unassigned")) {
                return ": unassigned";
            } else {
                return ": P1 " + p1.substring(15) + " | P2 " + p2.substring(15);
            }
        }
    }
    
    public static class Student {
        final String name, ssan, trqi, rank, stat;
        UCTClass p1, p2;
        
        
        public Student(String name, String ssan, String trqi, String rank, String stat) {
            this.name = name;
            this.ssan = ssan;
            this.trqi = trqi;
            this.rank = rank;
            this.stat = stat;
            p1 = null;
            p2 = null;
        }
        
        public void setPhase1(UCTClass p1) {
            this.p1 = p1;
        }
        
        public void setPhase2(UCTClass p2) {
            this.p2 = p2;
        }
        
        public int classStatus() {
            if(p2 == null) {
                return PHASE1_ONLY;
            } else if(p1 == null) {
                return PHASE2_ONLY;
            } else if(p1.equals(p2)) {
                return BOTH_SAME;
            } else {
                return BOTH_DIFF;
            }
        }
        
        public String getSSAN() {
            return ssan;
        }
        
        public String getStat() {
            return stat;
        }
        
        public String getName() {
            return name;
        }
        
        public String getTRQI() {
            return trqi;
        }
        
        public String getRank() {
            return rank;
        }
        
        @Override
        public String toString() {
            return ssan + " | " + trqi + " | " + rank + " | " + stat + " | " + name + " | " + p1 + " | " + p2;
        }
    }
}
