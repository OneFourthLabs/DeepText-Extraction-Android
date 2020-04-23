package indian_docs_processor.docs;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import indian_docs_processor.DocProcessor;

public class AadharFront extends DocumentBase {

    public static final DocProcessor.DocType docType = DocProcessor.DocType.AADHAR_FRONT;
    private String DOB;
    private char gender;

    public static AadharFront parseDocFromString(@NonNull String docString) {

        if (docString.isEmpty())
            return null;

        String[] lines = docString.toUpperCase().split("\n");
        System.out.println("Parsing AadharFront...");

        int i = 0;
        // Find the first line called 'Government of India'
        while (i < lines.length) {
            if (lines[i].contains("GOVERN")) // Hope atleast this should match
                break;
            i++;
        }
        if (++i >= lines.length) {
            // If I don't find "Govern", maybe look for "DOB" and come back a bit
            for(i = 2; i < lines.length; ++i) {
                if (lines[i].contains("DOB") && i+1 < lines.length && lines[i+1].contains("MALE"))
                    break;
            }
            if (i >= lines.length)
                return null;
            i -= 2;
        }

        AadharFront aadharFront = new AadharFront();

        // Ok, so now the next line will have name in regional letters
        // So just skip that
        if (++i >= lines.length)
            return null;

        // The current line should have the name in English
        aadharFront.name = lines[i];
        System.out.println("Name: " + aadharFront.name);

        // The next line will contain some regional text followed by "DOB: dd/MM/YYYY"
        // Since our current model cannot predict special chars, we'll search for a 6-8 digit number
        // TODO: Handle dd/MM/YYYY also in the same regex

        if (++i >= lines.length)
            return null;

        aadharFront.DOB = lines[i].substring(lines[i].indexOf("DOB")+3);

        Pattern dobPattern = Pattern.compile("\\d{6,8}");
        Matcher dobMatcher = dobPattern.matcher(aadharFront.DOB);
        if (!dobMatcher.find())
            return null;
        aadharFront.DOB = dobMatcher.group(); // Assumes there will be 1 match
        System.out.println("DOB: " + aadharFront.DOB);

        // The next line contains gender, something more sensitive than Aadhar ID for liberals
        if (++i >= lines.length)
            return null;

        // TODO: Handle queers. I mean, not in an Abrahamic way
        aadharFront.gender = lines[i].contains("FEMALE") ? 'F' : 'M';
        System.out.println("Gender: " + aadharFront.gender);

        // Now brace yourself, anywhere in the next few lines we may get the Aadhar ID
        String aadharRegex = "(\\d{4})\\s*(\\d{4})\\s*(\\d{4})";
        Pattern idPattern = Pattern.compile(aadharRegex);
        Matcher idMatcher;
        aadharFront.id = null;
        while (++i < lines.length) {
            idMatcher = idPattern.matcher(lines[i]);
            if (idMatcher.find()) {
                aadharFront.id = idMatcher.group().replaceAll(aadharRegex, "$1$2$3");
                break;
            }
        }

        if (aadharFront.id == null)
            return null;
        System.out.println("ID: " + aadharFront.id);

        return aadharFront;

    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder details = new StringBuilder("");
        if (name != null && !name.isEmpty())
            details.append("Name: " + name + '\n');
        if (id != null && !id.isEmpty())
            details.append("ID: " + id + '\n');
        if (DOB != null && !DOB.isEmpty())
            details.append("DOB: " + DOB + '\n');
        if (gender != '\0')
            details.append("Gender: " + gender + '\n');

        return details.toString();
    }

    public static boolean isMatched(String docString) {
        // Weak and Pathetic, but ok
        return docString.toUpperCase().contains("GOVERN");
    }

}
