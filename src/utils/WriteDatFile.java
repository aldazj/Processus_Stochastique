package utils;

import java.io.*;

/**
 * Created by aldazj on 19.09.15.
 * Ecrit une les résultats obtenus dans un fichier .dat
 * lequel sera ensuite lu avec Matlab par exemple 
 */
public class WriteDatFile {

    private String filename;
    private BufferedWriter bw;
    private String folder = "src/data";

    public WriteDatFile(String filename) {
        this.filename = folder+"/"+filename+".dat";
    }

    public void writeResults(double[] results){
        try {
            File file = new File(filename);
            if(!file.exists()){
                file.createNewFile();
            }
            bw = new BufferedWriter(new FileWriter(file));
            for (int i = 0; i < results.length; i++) {
                bw.write(Double.toString(results[i]));
                bw.write("\n");
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
