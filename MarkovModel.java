//Markov Model
//Tomaz Chevres
//June 6, 2024


import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

class MarkovModel{
    private Map<String,Integer> table1;//the number of times each kgram appears
    private Map<String,int[]> table2;//number of times each character follows a kgram (char as index)
    private int order;

    public static void main(String args[]){
        String text = "";
        try{
            text = Files.readString(Paths.get("hamlet.txt"));
        }
        catch(IOException e){
            e.printStackTrace();
        }


        MarkovModel model = new MarkovModel(text,6);
        System.out.println(model.generateString("Hamlet ",100000));
    }

    public MarkovModel(String text, int k){
        table1 = new HashMap<String,Integer>();
        table2 = new HashMap<String, int[]>();
        int textLength = text.length();
        order = k;
        text += text.substring(0, order);

        for(int i=0; i<textLength; i++){
            String kgram = text.substring(i, i+order);

            if(table1.containsKey(kgram)){
                table1.put(kgram, table1.get(kgram)+1);
            }
            else {
                table1.put(kgram,1);
            }

            if(table2.containsKey(kgram)){
                table2.get(kgram)[text.charAt(i+order)]+=1; //increase the next char count
            }
            else {
                int[] val = new int[128];
                val[text.charAt(i+order)]=1;
                table2.put(kgram, val);
            }
        }
    }

    public String generateString(String start, int length){
        if(start.length()<order) throw new IllegalArgumentException("starting string must be longer");
        if(!table1.containsKey(start.substring(start.length()-order))) throw new IllegalArgumentException("kgram not found");

        if(start.length()>=length) return start.substring(0,length);
        String out = start;
        while(out.length()<length){
            String kgram = out.substring(out.length()-order);
            out+=Character.toString(random(kgram));
        }
        return out;
    }

    public char random(String kgram){
        if(table1.containsKey(kgram)){
            String allNext = "";
            for(int i=0; i<128; i++){
                for(int j=0; j<table2.get(kgram)[i]; j++){
                    allNext += (char)i;
                }
            }
            return allNext.charAt((int)(Math.random()*allNext.length()));
        }
        else throw new IllegalArgumentException("kgram not found");
    }


}