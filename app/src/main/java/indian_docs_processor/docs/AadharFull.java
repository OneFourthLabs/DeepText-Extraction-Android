package indian_docs_processor.docs;

import androidx.annotation.NonNull;

import org.w3c.dom.Element;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class AadharFull extends AadharFront {
    // TODO: Handle backside

    private String careOf;
    // Address
    private String house, street, landmark;
    private String vtcName; // Name of Village/Town/City
    private String postOffice, district, subDistrict;
    private String state, pinCode;

    private void loadFromXmlElement(final Element data) {
        id = data.getAttribute("uid");
        name = data.getAttribute("name");

        // Convert DOB from YYYY-MM-DD to DD/MM/YYYY (as in printed Aadhar)
        String[] dob = data.getAttribute("dob").split("-");
        DOB = dob.length > 1 ? dob[2] + '/' + dob[1] + '/' + dob[0] : "";

        gender = data.getAttribute("gender").charAt(0);

        // Parse Aadhar Backside
        careOf = data.getAttribute("co");

        house = data.getAttribute("house");
        street = data.getAttribute("street");
        landmark = data.getAttribute("lm");

        vtcName = data.getAttribute("vtc");
        postOffice = data.getAttribute("po");
        district = data.getAttribute("dist");
        subDistrict = data.getAttribute("subdist");

        state = data.getAttribute("state");
        pinCode = data.getAttribute("pc");

    }

    public static AadharFull parseDocFromXML(final String xmlStr) {
        try {
            DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            ByteArrayInputStream input = new ByteArrayInputStream(xmlStr.getBytes("UTF-8"));
            Element root = docBuilder.parse(input).getDocumentElement();

            AadharFull aadharFull = new AadharFull();
            aadharFull.loadFromXmlElement(root);
            return aadharFull;

        } catch (Exception e) {
            return null;
        }
    }

    public static AadharFull createAadharFromFrontSide(final AadharFront aadharFront) {
        AadharFull aadharFull = new AadharFull();
        aadharFull.id = aadharFront.id;
        aadharFull.name = aadharFront.name;
        aadharFull.DOB = aadharFront.DOB;
        aadharFull.gender = aadharFront.gender;

        return aadharFull;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder details = new StringBuilder(super.toString());

        if (careOf != null && !careOf.isEmpty())
            details.append("Care Of: ").append(careOf).append('\n');

        details.append("\nADDRESS:\n");
        if (house != null && !house.isEmpty())
            details.append("House: ").append(house).append('\n');
        if (street != null && !street.isEmpty())
            details.append("Street: ").append(street).append('\n');
        if (landmark != null && !landmark.isEmpty())
            details.append("Landmark: ").append(landmark).append('\n');
        if (vtcName != null && !vtcName.isEmpty())
            details.append("Village/Town/City: ").append(vtcName).append('\n');
        if (postOffice != null && !postOffice.isEmpty())
            details.append("Post Office: ").append(postOffice).append('\n');
        if (subDistrict != null && !subDistrict.isEmpty())
            details.append("Sub-District: ").append(subDistrict).append('\n');
        if (district != null && !district.isEmpty())
            details.append("District: ").append(district).append('\n');
        if (state != null && !state.isEmpty())
            details.append("State: ").append(state).append('\n');
        if (pinCode != null && !pinCode.isEmpty())
            details.append("Pin Code: ").append(pinCode).append('\n');

        return details.toString();
    }
}
