package main;

import utils.Utilities;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * Created by aldazj on 15.09.15.
 */
public class Launcher {

    public Launcher() { }

    /**
     * Méthode du dé équilibré
     * @param N : nombre de évènements
     * @param nb_sequences : nombre des lancers effectués
     * @return : les probabilités d'apparition pour chaque évènement
     */
    private double [] de_equilibre(int N, int nb_sequences){
        System.out.println("############  Méthode dé équilibre  N:"+N+", nbLancers:"+nb_sequences+"  ############ ");
        System.out.println("");
        int f_apparition [] = new int[N];
        double[] results = new double[N];
        Arrays.fill(f_apparition, 0);
        System.out.println("Proba attendue pour chaque évènement: "+(1/(double)N));

        //Generation des évènements
        for (int j = 0; j < nb_sequences; j++) {
            double rValue = Math.random();
            int i = (int)Math.floor(rValue*N);
            f_apparition[i] += 1;
        }

        //Calcul de la probabilité
        for (int i = 0; i < N; i++) {
            results[i] = (double)f_apparition[i]/(double)nb_sequences;
        }
        return  results;
    }

    /**
     * Méthode d'un dé pipé à l'aide d'un dé équilibré
     * @param N : nombre des évènements
     * @param proba_nonuniformes : probabilités non uniforme d'appartion pour chaque évènement
     * @return : la probabilité d'apparition de chaque évènement
     */
    private double[] de_pipe_avec_de_equilibre(int N, String[] proba_nonuniformes, int nb_sequences){
        System.out.println("############  Méthode dé pipé avec un dé équilibré  N:"+N+", nbLancers:"+nb_sequences+"  ############ ");
        System.out.println("Probabilités non uniformes: "+Arrays.toString(proba_nonuniformes));
        //Calcul le ppcm entre tous nous determinants
        int ppcm = 1;
        for (int i = 0; i < N; i++) {
            ppcm = Utilities.compute_ppcm(ppcm, Integer.parseInt(proba_nonuniformes[i].split("/")[1]));
        }
        //Phase de pre-analyse
        int[] pre_traitement = new int[ppcm];
        int[] recordEventInitSize = new int[proba_nonuniformes.length];
        int start = 0;
        for (int i = 0; i < proba_nonuniformes.length; i++) {
            //Normalisation
            String[] fract = proba_nonuniformes[i].split("/");
            int sizePre_traitement = (int)((Double.parseDouble(fract[0])/ Double.parseDouble(fract[1]))/(1.0/(double)ppcm));
            recordEventInitSize[i] = sizePre_traitement;
            //Mise à jour du tableau où chaque éléments a la même taille d'apparition
            int end = start+sizePre_traitement;
            for (int j = start; j < end; j++) {
                pre_traitement[j] = i;
            }
            start = end;
        }
        //Méthode du dé équilibré => calcule la probabilité d'apparition d'un evènement "probabilités uniformes"
        double[] probaUniformes = de_equilibre(pre_traitement.length, nb_sequences);
        System.out.println("Résultat après l'utilisation de la méthode dé équilibré");
        System.out.println(Arrays.toString(probaUniformes));
        System.out.println("Résultats Finaux");
        return Utilities.garderProbabilitesNonUniformes(recordEventInitSize, probaUniformes);
    }

    /**
     * Détermine l'évènement d'apparition avec des probabilités non uniformes et une pièce biaisée
     * @param proba_nonuniformes : probabilités non uniformes
     * @param numeroEvent : évènement à une étape quelconque x
     * @return : l'évènement qui est apparu
     */
    private int event(String proba_nonuniformes, int numeroEvent){
        String[] proba_nonuniform = proba_nonuniformes.split(";");
        for (int i = 0; i < proba_nonuniform.length; i++) {
            double prob_i = Utilities.probaElement(proba_nonuniform[i]);
            double r = Math.random();
            if(r < prob_i){
                return numeroEvent;
            }else{
                double probaComplementaire = 1-prob_i;
                //On Normalise
                String new_proba_nonuniformes = Utilities.normalizeProba(proba_nonuniform, probaComplementaire);
                numeroEvent += 1;
                return event(new_proba_nonuniformes, numeroEvent);
            }
        }
        return -1;
    }

    /**
     * Méthode du dé pipé avec une pièce biaisée
     * @param N : nombre d'évènements
     * @param proba_nonuniformes : probabilités non uniformes
     * @param nb_sequences : nombre des lancers
     * @return la probabilité d'apparition de chaque évènement
     */
    private double[] de_pipe_avec_piece_biaisee(int N, String[] proba_nonuniformes, int nb_sequences){
        System.out.println("############  Méthode dé pipé avec une pièce biaisée  N:"+N+", nbLancers:"+nb_sequences+"  ############ ");
        System.out.println("Probabilités non uniformes: "+Arrays.toString(proba_nonuniformes));

        int f_apparition [] = new int[N];
        double[] results = new double[N];
        Arrays.fill(f_apparition, 0);

        //Generation des évènements
        for (int j = 0; j < nb_sequences; j++) {
            int myEvent = event(Utilities.formatString(proba_nonuniformes), 0);
            f_apparition[myEvent] += 1;
        }

        //Calcul de la probabilité
        for (int i = 0; i < N; i++) {
            results[i] = (double)f_apparition[i]/(double)nb_sequences;
        }
        return results;
    }

    /**
     * Méthode la roulette
     * @param N : nombre des évènements
     * @param proba_nonuniformes : probabilités non uniformes
     * @param nb_sequences : nombre de lancers
     * @return : la probabilité d'apparition de chaque évènement
     */
    private double[] roulette(int N, String[] proba_nonuniformes, int nb_sequences){
        System.out.println("############  Méthode la roulette  N:"+N+", nbLancers:"+nb_sequences+"  ############ ");
        System.out.println("Probabilités non uniformes: "+Arrays.toString(proba_nonuniformes));

        int f_apparition [] = new int[N];
        double[] results = new double[N];
        Arrays.fill(f_apparition, 0);
        String[] probaAccumulatives = new String[proba_nonuniformes.length];

        //Calcul le ppcm entre tous nous determinants
        int ppcm = 1;
        for (int i = 0; i < N; i++) {
            ppcm = Utilities.compute_ppcm(ppcm, Integer.parseInt(proba_nonuniformes[i].split("/")[1]));
        }

        //Calcule les probabilités accumulatives
        double probaAccumulValue = 0;
        double delta = 1E-10;
        double tmp = 0;
        for (int i = 0; i < proba_nonuniformes.length; i++) {
            String[] fract = proba_nonuniformes[i].split("/");
            probaAccumulValue += ((Double.parseDouble(fract[0])/ Double.parseDouble(fract[1]))/(1.0/(double)ppcm))/(double)ppcm;
            probaAccumulatives[i] = tmp+";"+probaAccumulValue;
            tmp = probaAccumulValue+delta;
        }

        //Generation des évènements
        for (int j = 0; j < nb_sequences; j++) {
            int myEvent = Utilities.recherche_dichotomique(Math.random(), probaAccumulatives);
            f_apparition[myEvent] += 1;
        }

        //Calcul de la probabilité
        for (int i = 0; i < N; i++) {
            results[i] = (double)f_apparition[i]/(double)nb_sequences;
        }
        return results;
    }

    /**
     * Programme principal
     * @param args
     */
    public static void main(String[] args) {
        int relaunch = -1;
        Scanner scanner;
        do{
            Launcher launcher = new Launcher();
            int choix = -1;
            boolean valueAccepted = false;
            int N, nbSequences;
            String[] probabilities;
            String saisieProb;

            System.out.println("Choisissez une des simulations ci-dessous:");
            do{
                System.out.println("Dé équilibré: 1");
                System.out.println("Dé pipé à l'aide d'un dé équilibré: 2");
                System.out.println("Dé pipé à l'aide d'un pièce biaisée: 3");
                System.out.println("Roulette: 4");
                scanner = new Scanner(System.in);
                try {
                    choix = Integer.parseInt(scanner.nextInt() + "");
                } catch (InputMismatchException e) {
                    System.out.println("Entrez un numero entre 0 et 4");
                }
                if(choix >= 0 && choix < 5){
                    valueAccepted = true;
                }
            }while (!valueAccepted);

            if(choix == 1){
                System.out.println("---------------------------------");
                System.out.println("Méthode du dé équilibré");
                System.out.println("Entre le nombre d'évènements \"N\": ");
                N = Integer.parseInt(scanner.nextInt() + "");
                System.out.println("Entrez le nombre de lancers: ");
                nbSequences = Integer.parseInt(scanner.nextInt() + "");
                System.out.println(Arrays.toString(launcher.de_equilibre(N, nbSequences)));
            }else{
                if(choix == 2){
                    System.out.println("---------------------------------");
                    System.out.println("Méthode du dé pipé à l'aide d'un dé équilibré");
                }else if(choix == 3) {
                    System.out.println("---------------------------------");
                    System.out.println("Méthode du dé pipé à l'aide d'un pièce biaisée");
                }else if(choix == 4) {
                    System.out.println("---------------------------------");
                    System.out.println("Méthode de la roulette");
                }
                System.out.println("Entre le nombre d'évènements \"N\": ");
                N = Integer.parseInt(scanner.nextInt() + "");
                probabilities = new String[N];
                System.out.println("Entrez le nombre de lancers: ");
                nbSequences = Integer.parseInt(scanner.nextInt() + "");
                boolean probaOk = false;
                do {
                    System.out.println("Entrez vos probabilités sous la forme d'une franction! Exemple: 3/4");
                    scanner = new Scanner(System.in);
                    for (int i = 0; i < probabilities.length; i++) {
                        System.out.println("Entrez votre probabilité " + (i + 1) + ":");
                        saisieProb = scanner.nextLine();
                        if (saisieProb.matches("[0-9]+\\/[0-9]+")) {
                            probabilities[i] = saisieProb;
                        } else {
                            System.out.println("Entrez une valeur correcte svp.");
                            i -= 1;
                        }
                    }
                    if (Utilities.verifyProbabilities(probabilities)) {
                        probaOk = true;
                    } else {
                        System.out.println("La somme de vos probabilités ne fait pas 1");
                    }
                }while (!probaOk);
                if (choix == 2) {
                    System.out.println(Arrays.toString(launcher.de_pipe_avec_de_equilibre(N, probabilities, nbSequences)));
                } else if (choix == 3) {
                    System.out.println(Arrays.toString(launcher.de_pipe_avec_piece_biaisee(N, probabilities, nbSequences)));
                } else if (choix == 4) {
                    System.out.println(Arrays.toString(launcher.roulette(N, probabilities, nbSequences)));
                }
            }
            System.out.println("Voulez-vous relancer le programme: Oui:1 \t Non:0");
            relaunch = scanner.nextInt();
        }while (relaunch == 1);
    }
}

//        Exemple d'execution pour comparer les résultats
//        int[] nbSequences = {100, 1000, 10000, 100000};
//        String[] proba_nonuniformes = new String[]{"1/4", "1/4", "1/4", "1/8", "1/16", "1/16"};
//        double[] result1, result2, result3, result4;
//        WriteDatFile datFile1, datFile2, datFile3, datFile4;
//        int N = 6;
//
//        result1 = new double[proba_nonuniformes.length];
//        result2 = new double[proba_nonuniformes.length];
//        result3 = new double[proba_nonuniformes.length];
//        result4 = new double[proba_nonuniformes.length];
//
//        for (int i = 0; i < nbSequences.length; i++) {
//
//            Arrays.fill(result1, 0.0);
//            Arrays.fill(result2, 0.0);
//            Arrays.fill(result3, 0.0);
//            Arrays.fill(result4, 0.0);
//
//            datFile1 = new WriteDatFile("de_equilibre_N_"+N+"_nbSequences_"+nbSequences[i]);
//            datFile2 = new WriteDatFile("de_pipe_avec_de_equilibre_N_"+N+"_nbSequences_"+nbSequences[i]);
//            datFile3 = new WriteDatFile("de_pipe_avec_piece_biaisee_N_"+N+"_nbSequences_"+nbSequences[i]);
//            datFile4 = new WriteDatFile("roulette_N_"+N+"_nbSequences_"+nbSequences[i]);
//
//            result1 = launcher.de_equilibre(6, nbSequences[i]);
//            result2 = launcher.de_pipe_avec_de_equilibre(proba_nonuniformes.length, proba_nonuniformes, nbSequences[i]);
//            result3 = launcher.de_pipe_avec_piece_biaisee(proba_nonuniformes.length, proba_nonuniformes, nbSequences[i]);
//            result4 = launcher.roulette(proba_nonuniformes.length, proba_nonuniformes, nbSequences[i]);
//
//            datFile1.writeResults(result1);
//            datFile2.writeResults(result2);
//            datFile3.writeResults(result3);
//            datFile4.writeResults(result4);
//        }