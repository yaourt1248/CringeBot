package fr.cringebot.cringe.xp;

import com.google.gson.reflect.TypeToken;
import fr.cringebot.cringe.escouades.Squads;
import net.dv8tion.jda.api.entities.Member;

import java.io.*;

import java.util.ArrayList;
import java.util.HashMap;

import static fr.cringebot.cringe.event.BotListener.gson;
import static java.lang.System.currentTimeMillis;

public class XP_textuel {

    private static final int SECOND = 1000;
    private static final int MINUTE = 60 * SECOND;
    private static final int HOUR = 60 * MINUTE;
    private static final long delay = 1*HOUR + 0*MINUTE + 0*SECOND; // parametrage de la plage de temps pour calcule moyenne msg / heure

    private static String path = "save/XP.json"; // fichier de stockage

    private static final TypeToken<HashMap<String, ArrayList<Long>>> token = new TypeToken<HashMap<String, ArrayList<Long>>>() {};

    private static ArrayList<Long> temps;
    private String id;

    private static HashMap<String, ArrayList<Long>> xp_map = new HashMap<>();


    private XP_textuel(String id)
    {
        this.id = id;
        this.temps = new ArrayList<Long>();
        this.temps.add(currentTimeMillis());
        xp_map.put(this.id, this.temps);
    }


    public static void message(Member m){

        if (xp_map.get(m.getId()) != null)
        {
            xp_map.get(m.getId()).add(currentTimeMillis()); //ajoute l'eure du message dans la liste de la personne
        }
        else
        {
            new XP_textuel(m.getId());     // cree une nouvelle entre avec pour id l'id de la personne
        }
        check(m);
        addpts(m);
        save();

    }

    private static void addpts(Member m) {

        int nbmsg = xp_map.get(m.getId()).size();   //calcule le nombre de pts a donner en fonction du message
        double bite = -0.5*Math.pow(((nbmsg-5)/10.8),2);
        long calcul = Math.round((2995*Math.exp(bite))/(10.8*Math.sqrt(2*Math.PI)));
        System.out.println("double : "+ calcul);

        Squads.addPoints(m.getId(), calcul);
    }

    private static void check(Member m){

        int i = 0;
        int j = 0;
        int l = xp_map.get(m.getId()).size();   // supprime les heure de message qui sont plus vieux que la
                                                // plage de temps definie ligne 20
        while (i<l){
            if (xp_map.get(m.getId()).get(j) + delay < System.currentTimeMillis()) {
                xp_map.get(m.getId()).remove(j);
            }
            else{j++;}
            i++;
        }
    }


    private static void save() {
        if (!new File(path).exists()) {
            try {
                new File(path).createNewFile(); // sauvegarde les donner dans le json
            } catch (IOException e) {
                return;
            }
        }
        try {
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path)));
            gson.toJson(xp_map, token.getType(), bw);
            bw.flush();
            bw.close();
        } catch (IOException e) {
        }
    }

    public static void load() {
        if (new File(path).exists()) {          // charge les donner du json
            try {
                xp_map = gson.fromJson(new BufferedReader(new InputStreamReader(new FileInputStream(path))), token.getType());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                new File(path).createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (xp_map == null)
            xp_map = new HashMap<>();
    }

}

