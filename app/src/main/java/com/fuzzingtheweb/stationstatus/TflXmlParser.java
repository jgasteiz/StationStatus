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
    private static final String LOG_TAG = TflXmlParser.class.getSimpleName();

    public List<Platform> parse(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            // parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readFeed(parser);
        } finally {
            in.close();
        }
    }

    /**
     * Goes through the XML and fetches the relevant information
     * @param parser - xml parser
     * @return list of platforms with their next arrivals on each
     * @throws XmlPullParserException
     * @throws IOException
     */
    private List<Platform> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Platform> platformList = new ArrayList<Platform>();
        Platform platform = new Platform();
        Entry entry = null;

        // While it's not the end of the document.
        while (parser.next() > XmlPullParser.END_DOCUMENT) {

            if (parser.getName() == null || parser.getEventType() == XmlPullParser.END_TAG) {
                continue;
            }

            // If we get a platform, get the direction
            if (parser.getName().equals("P")) {
                String direction = parser.getAttributeValue(null, "N");
                // If the platform changes, create a new one
                if (platform.getDirection() != null && !platform.getDirection().equals(direction)) {
                    platformList.add(platform);
                    platform = new Platform();
                }
                platform.setDirection(direction);
            } else if (parser.getName().equals("T")) {
                entry = readEntry(parser, platform.getDirection());
                platform.addEntry(entry);
            }
        }

        platformList.add(platform);

        return platformList;
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
