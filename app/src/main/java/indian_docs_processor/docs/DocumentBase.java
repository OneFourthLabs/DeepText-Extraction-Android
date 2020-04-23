package indian_docs_processor.docs;

import androidx.annotation.NonNull;

import indian_docs_processor.DocProcessor.DocType;

public abstract class DocumentBase {

    String id, name;
    public static final DocType docType = DocType.UNKNOWN;

    DocumentBase() {
        // I allow an empty constructor, because not all fields need to be successfully extracted.
    }

    DocumentBase(String id, String name) {
        this.id = id;
        this.name = name;
    }

    // static methods can't be abstract?? Java, y u do diz ;(
    // Devs, plis accept this as an abstract function to be implemented.
    public static DocumentBase parseDocFromString(String docString) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public static boolean isMatched(String docString) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @NonNull
    @Override
    public abstract String toString();

}
