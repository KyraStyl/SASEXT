package hybridutils;

import cetsmanager.cet.EventTrend;
import cetsmanager.cet.SimpleEventTrend;
import sasesystem.engine.ConfigFlags;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;


/**
 * A class to manage results from CET engine
 * @author kyrastyl
 *
 */

public class CetManager {

    public static HashSet<EventTrend> cets = new HashSet<>();
    public static HashSet<EventTrend> rawcets = new HashSet<>();
    public static HashSet<EventTrend> distinct_cets = new HashSet<>();
    public static int cetsdetectedtotal = 0;

    public static void writeToFile(boolean raw) throws IOException {
        HashSet<EventTrend> towrite = distinct_cets;
        if(distinct_cets.size() == 0){
           towrite = cets;
        }
        if(raw){
            towrite = rawcets;
        }
        Writer output ;
        output = new BufferedWriter(new FileWriter(ConfigFlags.outFile, true));
        for(EventTrend e: towrite){
            output.append(e.toString());
            output.append("\n");
        }
        output.close();
    }

    public static void allComb(){
        ArrayList<EventTrend> temp = (ArrayList<EventTrend>) cets.clone();
        for (EventTrend e: temp){
            ArrayList<EventTrend> combos = (ArrayList<EventTrend>) findAllCombos(e);
            for(EventTrend evtr:combos)
                distinct_cets.add(evtr);
        }
        cets.removeAll(temp);
    }


    /*
    public static ArrayList<EventTrend> distinctCets(ArrayList<EventTrend> all){
        ArrayList<EventTrend> distinct =new ArrayList<>();
        for(EventTrend e:all){
            if(!distinct.contains(e) && e.events().size()!=0)
                distinct.add(e);
        }
        return distinct;
    }

    public static ArrayList<EventTrend> allCetsCombinations(){
        ArrayList<EventTrend> all = new ArrayList<>();

        for(EventTrend e:cets){
            all.addAll(findAllCombos(e));
        }
        all = distinctCets(all);
        return all;
    }
    */


    private static Collection<? extends EventTrend> findAllCombos(EventTrend e) {
        ArrayList<EventTrend> all = new ArrayList<>();
        ArrayList<int[]> byteVectors = new ArrayList<>();
        int size = e.events().size();
        int comb = 1<<size;
        for(int i=0;i< comb;i++){
            int[] temp = new int[size];
            String t = toBinary(i,size);
            int j =0;
            for(String tk:t.split("(?!^)")){
                temp[j] = Integer.parseInt(tk);
                j++;
            }
            byteVectors.add(temp);
        }

        for(int[] bv:byteVectors){
            SimpleEventTrend temp = new SimpleEventTrend();
            for(int pos=0; pos<bv.length; pos++){
                int b = bv[pos];
                if(b == 1){
                    temp.append(e.get(pos));
                }
            }
            if(temp.events().size()>1)
                all.add(temp);
        }
        return all;
    }

    public static String toBinary(int x, int len)
    {
        if (len > 0)
            return String.format("%" + len + "s", Integer.toBinaryString(x)).replaceAll(" ", "0");

        return null;
    }

}
