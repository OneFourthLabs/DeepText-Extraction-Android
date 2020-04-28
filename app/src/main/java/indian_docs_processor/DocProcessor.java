package indian_docs_processor;

import indian_docs_processor.docs.AadharFront;
import indian_docs_processor.docs.AadharFull;
import indian_docs_processor.docs.DocumentBase;
import indian_docs_processor.docs.PanCard;

public class DocProcessor {

    public static enum DocType {
        UNKNOWN,
        AADHAR_FRONT,
        AADHAR_FULL,
        PAN_CARD,
        REGIONAL_VOTER_ID,
    };

    public DocProcessor() {

    }

    public static DocumentBase getDocFromString(DocType docType, String docString) {

        switch (docType) {
            case AADHAR_FRONT:
                return AadharFront.parseDocFromString(docString);
            case PAN_CARD:
                return PanCard.parseDocFromString(docString);
            default:
                return null;
        }
    }

    public static DocumentBase getDocFromQR(DocType docType, String qrText) {

        switch (docType) {
            case AADHAR_FULL:
                return AadharFull.parseDocFromXML(qrText);
            default:
                return null;
        }
    }

    public static DocType detectDocType(String docString) {

        if (AadharFront.isMatched(docString))
            return DocType.AADHAR_FRONT;

        if (PanCard.isMatched(docString))
            return DocType.PAN_CARD;

        return DocType.UNKNOWN;
    }
}
