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

    public static final String[] DOCS_SUPPORTED = {
            "AADHAR FRONT SIDE",
            "AADHAR QR",
            "PAN CARD FRONT SIDE"
    };

    public DocProcessor() {

    }

    public static DocumentBase parseDocFromResult(String docName, String docString) {
        docName = docName.toUpperCase();
        if (docName.contains("QR")) {
            if (docName.contains("AADHAR"))
                return getDocFromQR(DocType.AADHAR_FULL, docString);
            return null;
        }

        if (docName.contains("AADHAR"))
            return getDocFromString(DocType.AADHAR_FRONT, docString);

        if (docName.contains("PAN"))
            return getDocFromString(DocType.PAN_CARD, docString);

        return null;
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
