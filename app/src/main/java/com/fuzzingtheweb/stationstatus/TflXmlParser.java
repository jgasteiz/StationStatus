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

    public static class Entry {
        public final String location;
        public final String destination;
        public final String timeTo;
        public final String departTime;
        public final String direction;

        private Entry(String location, String destination, String timeTo, String departTime, String direction) {
            this.location = location;
            this.destination = destination;
            this.timeTo = timeTo;
            this.departTime = departTime;
            this.direction = direction;
        }
    }

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

    private List<Entry> readFeed(XmlPullParser parser) throws XmlPullParserException, IOException {
        List<Entry> entries = new ArrayList<Entry>();

        parser.require(XmlPullParser.START_TAG, ns, "ROOT");
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.next() == 1) {
                break;
            }
            while (parser.getName() == null || !parser.getName().equals("P")) {
                parser.next();
            }
            String name = parser.getName();
            if (name.equals("P")) {
                entries = readPlatform(parser, entries);
            } else {
                skip(parser);
            }
        }

        return entries;
    }

    public List<Entry> readPlatform(XmlPullParser parser, List<Entry> entries) throws IOException, XmlPullParserException {

        String direction = null;
        while (parser.getName() == null || !parser.getName().equals("T")) {
            if (parser.getName() != null && parser.getName().equals("P")) {
                direction = parser.getAttributeValue(ns, "N");
            }
            parser.next();
        }

        while (parser.next() > 1) {
            if (parser.getName() != null) {
                Entry entry = readEntry(parser, direction);
                if (entry.destination != null && entry.timeTo != null) {
                    entries.add(entry);
                }
            }
            parser.next();
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
