package indian_docs_processor.docs;

import androidx.annotation.NonNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import indian_docs_processor.DocProcessor;

public class PanCard extends DocumentBase {

    public static final DocProcessor.DocType docType = DocProcessor.DocType.PAN_CARD;
    private String parentName;

    public static PanCard parseDocFromString(@NonNull String docString) {

        if (docString.isEmpty())
            return null;

        String[] lines = docString.toUpperCase().split("\n");
        System.out.println("Parsing PAN card...");

        int start_i = 0;
        // Find the first line called 'Income Tax Department'
        while (start_i < lines.length) {
            if (lines[start_i].contains("INCOME") || lines[start_i].contains("TAX")) // Hope atleast this should match
                break;
            start_i++;
        }

        if (++start_i >= lines.length)
            return null;

        // Now, there are 2 fields immediately below. The person & his parent's name
        // This can span a max of 4 lines (when long name for child & parent), and a min of 2 lines.
        // Better find the ending line and parse accordingly. How? By finding which line has DOB

        int end_i;

        String dobRegex = "(\\d{1,2})[/]{0,1}(\\d{1,2})[/]{0,1}(\\d{4})";
        Pattern dobPattern = Pattern.compile(dobRegex);
        Matcher dobMatcher;

        PanCard panCard = new PanCard();

        for (end_i = start_i; end_i < lines.length; ++end_i) {
            String line = lines[end_i].trim().replaceAll("\\s", "");
            dobMatcher = dobPattern.matcher(lines[end_i]);
            if (dobMatcher.find()) {
                panCard.DOB = dobMatcher.group();
                break;
            }
        }

        if (end_i >= lines.length)
            return null;

        System.out.println("DOB: " + panCard.DOB);

        // Now, the 2 names are in [start_i, end_i)
        if (end_i - start_i <= 2) {
            // This is the majority case. One line for baby and another for daddy
            panCard.name = lines[start_i];
            if (end_i - start_i == 2)
                panCard.parentName = lines[start_i+1];
        } else {
            // Assume 2-lined person name
            panCard.name = lines[start_i] + ' ' + lines[start_i+1];
            panCard.parentName = lines[start_i+2];
            if (end_i - start_i > 3) // Probably a Telugu guy
                panCard.parentName += ' ' + lines[start_i+3];
        }

        System.out.println("Name: " + panCard.name);
        System.out.println("Parent: " + panCard.parentName);

        // Ok. Finally, the PAN ID extraction time. Let us see what Wiki bro says:
        /* PAN number is a ten-character long alpha-numeric unique identifier.
         * The PAN structure is as follows: AAAPL1234C
         * - The first five characters are letters (in uppercase by default),
         * - followed by four numerals,
         * - and the last (tenth) character is a letter.
         * */

        // String panIdRegex = "[A-Z]{5}\\d{4}[A-Z]{1}";
        String panIdRegex = "\\w{10}"; // Reality is often disappointing
        Pattern panIdPattern = Pattern.compile(panIdRegex);
        panCard.id = null;
        for (int i=end_i+1; i < lines.length; ++i) {
            Matcher panIdMatcher = panIdPattern.matcher(lines[i]);
            if (panIdMatcher.find()) {
                panCard.id = panIdMatcher.group();
                break;
            }
        }

        if (panCard.id == null)
            return null;

        System.out.println("ID: " + panCard.id);

        return panCard;

    }

    public static boolean isMatched(String docString) {
        // Weak and Pathetic, but ok
        docString = docString.toUpperCase();
        return docString.contains("INCOME") || docString.contains("TAX");
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
        if (parentName != null && !parentName.isEmpty())
            details.append("Parent: " + parentName + '\n');

        return details.toString();
    }
}
