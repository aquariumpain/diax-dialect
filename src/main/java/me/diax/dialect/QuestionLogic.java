package me.diax.dialect;
import java.sql.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class QuestionLogic {
    private static String[] stopWordsList = {
        "without", "see", "unless", "due", "also", "must", "might", "like", "will", "may", "can", "much",
        "every", "the", "in", "other", "this", "the", "many", "any", "an", "or", "for", "in", "is", "a", "about", "above", "after", "again", "against", "all", "am", "an", "and", "any", "are", "aren't", "as", "at", "be", "because", "been", "before", "being", "below", "between", "both", "but", "by", "can't", "cannot", "could",
        "couldn't", "did", "didn't", "do", "does", "doesn't", "doing", "don't", "down", "during", "each", "few", "for", "from", "further", "had", "hadn't", "has", "hasn't", "have", "haven't", "having",
        "he", "he'd", "he'll", "he's", "her", "here", "here's", "hers", "herself", "him", "himself", "his", "how", "how's", "i", "i'd", "i'll", "i'm", "i've", "if", "in", "into", "is",
        "isn't", "it", "it's", "its", "itself", "let's", "me", "more", "most", "mustn't", "my", "myself", "no", "nor", "not", "of", "off", "on", "once", "only", "ought", "our", "ours", "ourselves",
        "out", "over", "own", "same", "shan't", "she", "she'd", "she'll", "she's", "should", "shouldn't", "so", "some", "such", "than",
        "that", "that's", "their", "theirs", "them", "themselves", "then", "there", "there's", "these", "they", "they'd", "they'll", "they're", "they've",
        "this", "those", "through", "to", "too", "under", "until", "up", "very", "was", "wasn't", "we", "we'd", "we'll", "we're", "we've",
        "were", "weren't", "what", "what's", "when", "when's", "where", "where's", "which", "while", "who", "who's", "whom",
        "why", "why's", "with", "won't", "would", "wouldn't", "you", "you'd", "you'll", "you're", "you've", "your", "yours", "yourself", "yourselves"};
    private static String[] punctuationList = {
        ".", ",", "[", "]", "|", "\"", "'", "{", "}", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "-", "_", "+", "=", "<", ">", "~", "`", "\\", "/", "?", ";", ":"
    };
    public static int findBestMatch(String input) {
        int bestMatchId = 0;
        int bestMatchWordAmount = -1;
        try {
            String[] inputNoStopWords = removeStopWords(input);
            ResultSetBundle bundle = DatabaseOperations.query("select m.* from messages m left outer join links l on m.id = l.input_id", null);
            ResultSet rs = bundle.getResultSet();
            while (rs.next()) {
                int id = rs.getInt(1);
                String text = rs.getString(2);
                if (text.equals(input)) {
                    bestMatchId = id;
                    break;
                } else {
                    String[] thisNoStopWords = removeStopWords(text);
                    int thisWordAmount = 0;
                    for(String el : inputNoStopWords) {
                        if(Arrays.asList(thisNoStopWords).contains(el)){
                            thisWordAmount++;
                            List<String> listNoStopWords = new LinkedList<String>(Arrays.asList(thisNoStopWords));
                            int index = listNoStopWords.indexOf(el);
                            listNoStopWords.remove(index);
                            listNoStopWords.toArray(thisNoStopWords);
                        }
                    }
                    if (thisWordAmount > bestMatchWordAmount) {
                        bestMatchWordAmount = thisWordAmount;
                        bestMatchId = id;
                    }
                }
            }
            bundle.closeAll();
        }
        catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return bestMatchId;
    }

    private static String[] removeStopWords(String input) {
        String output;
        String[] words = input.split(" ");
        StringBuilder outputBuilder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String OriginalWord = Arrays.asList(words).get(i);
            String word = Arrays.asList(words).get(i).toLowerCase();
            if (!Arrays.asList(stopWordsList).contains(word)) {
                outputBuilder.append(OriginalWord);
                if (i != (words.length - 1)) {
                    outputBuilder.append(" ");
                }
            }
        }
        output = outputBuilder.toString();
        for (int i = 0; i < punctuationList.length; i++) {
            String el = Arrays.asList(punctuationList).get(i);
            output = output.replace(el, "");
        }
        output = output.replaceAll("[ ]{2,}", " ");
        output = output.trim();
        String[] outputArray = output.split(" ");
        return outputArray;
    }

    public static String getOutputFromId(int input) {
        String output = "";
        int bestOutputId = 0;
        int bestOutputWeight = -1;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/diaxdialect", "diaxdialect", "diaxDialect");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select * from links where input_id=" + input);
            while (rs.next()){
                int outputId = rs.getInt(3);
                int weight = rs.getInt(4);
                if (weight > bestOutputWeight) {
                    bestOutputId = outputId;
                    bestOutputWeight = weight;
                }
            }
            ResultSet rs2 = stmt.executeQuery("select * from messages where id=" + bestOutputId);
            if (rs2.next()){
                output = rs2.getString(2);
            }
            con.close();
        }
        catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
        }
        return output;
    }
}
