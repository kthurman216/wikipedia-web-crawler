package edu.umsl;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;

import java.io.IOException;
import java.util.ArrayList;

public class Crawler {
    public static void main(String[] args) {
        String url = "https://en.wikipedia.org/";
        crawlAndCount(url);
    }

    //crawler function crawls websites then counts instances of words
    public static void crawlAndCount(String iUrl) {
        int pageCount = 1000;                            //NUMBER OF LINKS TO CRAWL
        ArrayList<String> pUrls = new ArrayList<>();    //pending URLs
        ArrayList<String> tUrls = new ArrayList<>();    //traversed URLs
        ArrayList<String> words = new ArrayList<>();    //list of encountered words
        ArrayList<Integer> occurs = new ArrayList<>();  //2D array, number of occurrences

        pUrls.add(iUrl);    //add initial URL

        System.out.println("CRAWLING...");

        while (!pUrls.isEmpty() && tUrls.size() <= pageCount)
        {
            //sets current url to visit to the first url on pending list
            String urlString = pUrls.remove(0);

            //checks if current url is already visited
            if (!tUrls.contains(urlString))
            {
                //requests access to current url and assigns to document to be processed
                Document document= request(urlString, tUrls);

                //pulls sub urls from current document
                getSubUrls(document, pUrls, tUrls);

                //processes document for word/word instances
                if (document != null) {
                    wordCounter(document, words, occurs);
                }
            }
        }
        System.out.println();
        for (int i = 0; i < words.size(); i++)
        {
            System.out.println("WORD: " + words.get(i) + "\tOCCURRENCES: " + occurs.get(i));
        }
    }

    //checks that url provided is accessible; if so, returns html document
    private static Document request(String url, ArrayList<String> tUrls){
        try{
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();
            Thread.sleep(500);

            if (connection.response().statusCode() == 200)
            {
                System.out.println("TITLE: " + document.title());
                tUrls.add(url);

                return document;
            }
            return null;
        }
        catch(IOException | InterruptedException ex){
                return null;
        }
    }

    //pulls sub urls from the current link and adds them to the pending url list if they are not already visited
    public static void getSubUrls(Document doc, ArrayList<String> pUrls, ArrayList<String> tUrls) {
        if (doc != null)
        {
            for(Element link : doc.select("a[href]"))
            {
                String newLink = link.absUrl("href");
                if(!tUrls.contains(newLink))
                {
                    pUrls.add(newLink);
                }
            }
        }
    }

    //method to count instances of words
    public static void wordCounter (Document doc, ArrayList<String> words, ArrayList<Integer> occurs) {
        Document scrubbed = Jsoup.parse(doc.body().text());
        Safelist sl = Safelist.none();
        String text = Jsoup.clean(scrubbed.html(), sl);

        //splits text into individual words by searching whitespace and non-alphanumeric characters...
        //also removes letters that aren't arabic letters i guess???
        String[] split = text.split("\\W+");

        //changes all the words in the split up text to lower case for matching purposes
        for (int i = 0; i < split.length; i++)
        {
            split[i] = split[i].toLowerCase();
        }

        //checks each word from document against list of words already encountered
        for (int i = 0; i < split.length; i++)
        {
            //if word is already on list
            if (words.contains(split[i]))
            {
                //finds the index of the word and the matching count index and increments it
                int intIndex = words.indexOf(split[i]);
                occurs.set(intIndex, occurs.get(intIndex) + 1);
            }
            //else adds the word to the list and a matching count for number of occurrences
            else
            {
                words.add(split[i].toLowerCase());
                occurs.add(1);
            }
        }
    }

}


