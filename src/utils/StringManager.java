package src.utils;

public class StringManager {

    public static String reduceStringToNWords(String input,int n) {
        // Suddividi la stringa in un array di parole
        String[] words = input.split("\\s+");

        // Se ci sono 5 o meno parole, restituisci la stringa originale
        if (words.length <= n) {
            return input;
        }

        // Altrimenti, unisci solo le prime 5 parole
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < n; i++) {
            result.append(words[i]).append(" ");
        }

        // Rimuovi l'ultimo spazio in eccesso e restituisci la stringa risultante
        String shorthand = result.toString().trim();
        return shorthand + "...";
    }
}
