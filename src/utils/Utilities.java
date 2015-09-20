package utils;

import java.util.StringJoiner;

/**
 * Created by aldazj on 15.09.15.
 */
public class Utilities {

    private Utilities(){

    }

    /**
     * On calcule le ppcm entre deux valeurs
     * Exemple: 4 et 16 => 16
     * @param value1 : Première valeur à trouver le ppcm
     * @param value2 : Deuxième valeur à trouver le ppcm
     * @return : Le ppcm entre value1 et value2
     */
    public static int compute_ppcm(int value1, int value2){
        int ppcm_value;
        if (value2 == value1 || value2 == 1){
            ppcm_value = value1;
        }else{
            if(value1 == 1){
                ppcm_value = value2;
            }else{
                ppcm_value = 0;
            }
        }
        //On augmente la valeur mm juqu'à être plus grand ou égale à nn
        //ensuite pareil pour nn, jusqu'à que nos valeur soit égales
        if (ppcm_value == 0) {
            int value1_tmp = value1;
            int value2_tmp = value2;
            while (value1_tmp != value2_tmp) {
                while (value1_tmp < value2_tmp) {
                    value1_tmp += value1;
                }
                while (value2_tmp < value1_tmp) {
                    value2_tmp += value2;
                }
            }
            ppcm_value = value1_tmp;
        }
        return ppcm_value;
    }

    /**
     * Nouns metons nous probabilités sous cette forme "1/4;1/4;1/4;1/8;1/16;1/16"
     * Exemple [1/4,1/4,1/4,1/8,1/16,1/16] => "1/4;1/4;1/4;1/8;1/16;1/16"
     * @param proba_nonuniformes: Tableau avec nous probabilités
     * @return : les probabilités non uniforme dans un format simple à manipuler
     */
    public static String formatString(String[] proba_nonuniformes){
        StringJoiner sj = new StringJoiner(";");
        for (int i = 0; i < proba_nonuniformes.length; i++) {
            sj.add(proba_nonuniformes[i]);
        }
        return sj.toString();
    }

    /**
     * Reécupère la probabilité d'une fraction
     * Exemple: 1/4 = 0.25
     * @param proba_nonuniform : probabilité non uniforme
     * @return : probabilité en format double
     */
    public static double probaElement(String proba_nonuniform){
        String[] fract = proba_nonuniform.split("/");
        return Double.parseDouble(fract[0])/ Double.parseDouble(fract[1]);
    }

    /**
     * Normalize une serie de probabilités
     * @param proba_nonuniform
     * @param probaComplementaire
     * @return
     */
    public static String normalizeProba(String[] proba_nonuniform, double probaComplementaire){
        StringJoiner sj = new StringJoiner(";");

        //On se deplace de "1" car on est pas tombé sur pile
        for (int i = 1; i < proba_nonuniform.length; i++) {
            double normaliseValue = probaElement(proba_nonuniform[i])/probaComplementaire;
            sj.add(Utilities.double_to_Fract(normaliseValue));
        }
        return sj.toString();
    }

    /**
     * À partir d'un nombre décimale on trouve sa fraction
     * Exemple: 0.25 => 1/4
     * @param value
     * @return
     */
    public static String double_to_Fract(double value){
        double tolerance = 0.000001;
        double h1 = 1;
        double h2 = 0;
        double k1 = 0;
        double k2 = 1;
        double b = value;
        do{
            double a = Math.floor(b);
            double aux = h1;
            h1 = a*h1+h2;
            h2 = aux;
            aux = k1;
            k1 = a*k1+k2;
            k2 = aux;
            b = (1/(b-a));
        }while(Math.abs(value-h1/k1) > value*tolerance);
        return (int)h1+"/"+(int)k1;
    }

    /**
     * Détermine si une valeur se trouve dans un interval de deux valeurs
     * Exemple:
     * Intervale sur la forme: "0.25;0.50"
     * @param value : valeur aléatoire
     * @param interval : interval
     * @return
     */
    private static boolean is_in_probaAccumulatives(double value, String interval){
        String[] myInterval = interval.split(";");
        double startInterval = Double.parseDouble(myInterval[0]);
        double endInterval = Double.parseDouble(myInterval[1]);
        return  value >= startInterval && value <= endInterval;
    }

    /**
     * Détermine si une valeur est plus grande que les deux valeurs existantes dans un interval
     * Exemple:
     * Intervale sur la forme: "0.25;0.50"
     * @param value : valeur aléatoire
     * @param interval : interval
     * @return
     */
    private static boolean is_greater_than(double value, String interval){
        String[] myInterval = interval.split(";");
        double startInterval = Double.parseDouble(myInterval[0]);
        double endInterval = Double.parseDouble(myInterval[1]);
        return  value > startInterval && value > endInterval;
    }

    /**
     * Détermine si une valeur est plus petite que les deux valeurs existantes dans un interval
     * Exemple:
     * Intervale sur la forme: "0.25;0.50"
     * @param value : valeur aléatoire
     * @param interval : interval
     * @return
     */
    private static boolean is_less_than(double value, String interval){
        String[] myInterval = interval.split(";");
        double startInterval = Double.parseDouble(myInterval[0]);
        double endInterval = Double.parseDouble(myInterval[1]);
        return  value < startInterval && value < endInterval;
    }

    /**
     * Passer du tableau où les probabilités d'apparition sont uniformes vers un tableau qui garde les
     * probabilités non-uniformes du départ.
     * @param recordEventInitSize : taille original de chaque évènement
     * @param probaUniformes : probabilités uniformes
     * @return probabilités non uniformes
     */
    public static double[] garderProbabilitesNonUniformes(int[] recordEventInitSize, double[] probaUniformes){
        double[] result = new double[recordEventInitSize.length];
        int pointer = 0;
        for (int i = 0; i < recordEventInitSize.length; i++) {
            double value = 0.0;
            for (int j = pointer; j < pointer+recordEventInitSize[i]; j++) {
                value += probaUniformes[j];
            }
            result[i] = value;
            pointer += recordEventInitSize[i];
        }
        return result;
    }

    /**
     * Recherche dichotonique. Nous division l'interval de recherche par deux
     * à chaque étape
     * @param value : valeur à trouver
     * @param probaAccumulatives : probabilités accumulatives
     * @return : l'évènement trouvé ou pas
     */
    public static int recherche_dichotomique(double value, String[] probaAccumulatives){
        int start = 0;
        int end = probaAccumulatives.length;
        boolean found = false;
        int pointer;
        do{
            pointer = ((start+end)/2);
            if(is_in_probaAccumulatives(value, probaAccumulatives[pointer])){
                found = true;
            }else if(is_greater_than(value, probaAccumulatives[pointer])){
                start = pointer+1;
            }else if(is_less_than(value, probaAccumulatives[pointer])){
                end = pointer-1;
            }
        }while (found == false && start <= end);

        if(found){
            return pointer;
        }else{
            return -1;
        }
    }

    /**
     * Vérifie si la somme des probabilités fait 1
     * @param probabilities
     * @return
     */
    public static boolean verifyProbabilities(String[] probabilities){
        double valueMax = 1.0;
        double valueTmp = 0.0;
        for (int i = 0; i < probabilities.length; i++) {
            valueTmp += probaElement(probabilities[i]);
        }
        if(valueMax != valueTmp){
            return false;
        }
        return true;
    }
}
