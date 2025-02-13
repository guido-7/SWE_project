package src.utils;

public class StringManager {

    public static String reduceStringToFiveWords(String input) {
        // Suddividi la stringa in un array di parole
        String[] words = input.split("\\s+");

        // Se ci sono 5 o meno parole, restituisci la stringa originale
        if (words.length <= 5) {
            return input;
        }

        // Altrimenti, unisci solo le prime 5 parole
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            result.append(words[i]).append(" ");
        }

        // Rimuovi l'ultimo spazio in eccesso e restituisci la stringa risultante
        String shorthand = result.toString().trim();
        return shorthand + "...";
    }
}
