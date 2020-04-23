package indian_docs_processor;

import indian_docs_processor.docs.AadharFront;
import indian_docs_processor.docs.DocumentBase;

public class DocProcessor {

    public static enum DocType {
        UNKNOWN,
        AADHAR_FRONT,
        PAN_CARD,
        REGIONAL_VOTER_ID,
    };

    public DocProcessor() {

    }

    public static DocumentBase getDocFromString(DocType docType, String docString) {

        switch (docType) {
            case AADHAR_FRONT:
                return AadharFront.parseDocFromString(docString);
            default:
                return null;
        }
    }

    public static DocType detectDocType(String docString) {

        if (AadharFront.isMatched(docString)) {
            return DocType.AADHAR_FRONT;
        }

        return DocType.UNKNOWN;
    }
}
