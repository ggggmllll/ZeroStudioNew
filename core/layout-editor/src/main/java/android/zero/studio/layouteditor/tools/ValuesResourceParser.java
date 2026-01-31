package android.zero.studio.layouteditor.tools;

import android.widget.TextView;
import android.zero.studio.layouteditor.adapters.models.ValuesItem;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author android_zero
 */
public class ValuesResourceParser {
    public static final String TAG_STRING = "string";
    public static final String TAG_COLOR = "color";

    private List<ValuesItem> valuesList;

    public ValuesResourceParser(InputStream stream, String tag) {
        valuesList = new ArrayList<>();
        if (stream != null) {
            parseXML(stream, tag);
        }
    }

    private void parseXML(InputStream stream, String tag) {
        String name = "";
        String value = "";
        
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(stream, null);

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    String tagName = xpp.getName();
                    if (tagName.equalsIgnoreCase(tag)) {
                        String rawName = xpp.getAttributeValue(null, "name");
                        name = rawName != null ? rawName : "unknown_name";
                        value = ""; 
                    }
                } else if (eventType == XmlPullParser.TEXT) {
                    String rawText = xpp.getText();
                    if (rawText != null) {
                        value = rawText; 
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                    String tagName = xpp.getName();
                    if (tagName.equalsIgnoreCase(tag)) {
                        if (!name.isEmpty()) {
                            valuesList.add(new ValuesItem(name, value));
                        }
                        // 重置临时变量
                        name = "";
                        value = "";
                    }
                }
                eventType = xpp.next();
            }
        } catch (XmlPullParserException | IOException | IllegalArgumentException e) {
            e.printStackTrace();
        } finally {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void createTextView(TextView textView) {
        StringBuilder builder = new StringBuilder();
        for (ValuesItem item : valuesList) {
            builder.append(item.name).append(" = ").append(item.value).append("\n");
        }
        textView.setText(builder.toString());
    }

    public List<ValuesItem> getValuesList() {
        return this.valuesList;
    }

    public void setValuesList(List<ValuesItem> valuesList) {
        this.valuesList = valuesList;
    }
}