package com.fuzzingtheweb.stationstatus;


import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TflXmlParser {

    private static final String ns = null;

    public List<Entry> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    /**
     * Goes through the XML and fetches the relevant information
     * @param parser
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    private List<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Entry> entries = new ArrayList<Entry>();
        String direction = null;

        parser.require(XmlPullParser.START_TAG, ns, "ROOT");
        while (parser.next() > 1) {

            if (parser.getName() == null) {
                parser.next();
                continue;
            }

            if (parser.getName().equals("T")) {
                Entry entry = readEntry(parser, direction);
                entries.add(entry);
            }
        }

        return entries;
    }

    // Parses the contents of an entry. If it encounters a title, summary, or link tag, hands them off
    // to their respective "read" methods for processing. Otherwise, skips the tag.
    private Entry readEntry(XmlPullParser parser, String direction) throws XmlPullParserException, IOException {
        String location = parser.getAttributeValue(null, "Location");
        String destination = parser.getAttributeValue(null, "Destination");
        String timeTo = parser.getAttributeValue(null, "TimeTo");
        String departTime = parser.getAttributeValue(null, "DepartTime");
        return new Entry(location, destination, timeTo, departTime, direction);
    }


    private void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
