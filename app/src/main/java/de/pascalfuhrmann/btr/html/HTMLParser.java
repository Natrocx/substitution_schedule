/* Copyright 2018 Pascal Fuhrmann, Jonas Lauschke

    This file is part of substitution_schedule.

    substitution_schedule is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    substitution_schedule is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with substitution_schedule.  If not, see <http://www.gnu.org/licenses/>.
  */
package de.pascalfuhrmann.btr.html;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


import de.pascalfuhrmann.btr.utility.DataEntry;

/**
 * Responsible for the parsing of HTML information into a readable format.
 * Author: Pascal Fuhrmann
 */
public class HTMLParser {
    private List<DataEntry> entryList;
    private Document document;
    private List<String> parsedList;


    public HTMLParser(String html) {
        //Using Jsoup to parse the html string
        document = Jsoup.parse(html);
        entryList = parse(html);
    }

    /**
     * Returns an unsorted list of strings with the data entrys content.
     */
    public List<String> toStringList() {
        List<String> parsedList = new ArrayList<>();
        for(DataEntry entry : entryList) {
            parsedList.add(entry.getEssentialInfo());
        }
        return parsedList;
    }

    /**
     * Returns a list of strings which only contains searched elements.
     * Checks for class shorthands/teacher shorthands.
     */
    public List<String> searchSort(String search) {
        List<String> parsedList = new ArrayList<>();

        //comparing all school class shorthands to the search input
        for(DataEntry entry : entryList) {
            if(entry.getSchoolClass().equalsIgnoreCase(search)) {
                parsedList.add(entry.getEssentialInfo());
            }
        }

        //comparing all teacher shorthands to the search input
        for(DataEntry entry : entryList) {
            boolean teacher        = entry.getTeacher().substring(0,2).equalsIgnoreCase(search);
            boolean changedTeacher = false;
            if(!entry.getChangedTeacher().isEmpty())
                changedTeacher = entry.getChangedTeacher().substring(0,2).equalsIgnoreCase(search);


            if(teacher || changedTeacher) {
                parsedList.add(entry.getEssentialInfo());
            }
        }

        if(parsedList.isEmpty()) {
            parsedList.add("No search results.\n" +
                            "Try using a class shorthand e.g.: 'DI71'\n" +
                            "or using a teacher shorthand e.g.: KR");
        }
        return parsedList;
    }

    /**
     * Return a list of strings which prioritizes entrys of the school class
     * defined in the settings  activity.
     */
    public List<String> sortByClass(String schoolClass) {
        parsedList = new ArrayList<>();
        HashSet<String> schoolClasses = new HashSet<>();

        //if there was an error we cant sort the list and don't have to
        if (entryList.size() == 1) {
            parsedList.add(entryList.get(0).getEssentialInfo());
            return parsedList;
        }

        for(DataEntry entry : entryList) {
            //we are creating a list that contains all school classes excluding duplicates
            schoolClasses.add(entry.getSchoolClass());

            //if the momentary entry equals the passed argument we add it to the sorted list
            //the passed argument is handled as our prioritized school class (favored by user in settings)
            if(entry.getSchoolClass().equalsIgnoreCase(schoolClass)) {
                parsedList.add(entry.getEssentialInfo());
            }
        }

        //adding all other DataEntry information sorted by school class
        for(String sortedClass : schoolClasses) {
            for(int i = 0; i < entryList.size(); i++) {
                //sort by class
                if(sortedClass.equalsIgnoreCase(entryList.get(i).getSchoolClass()) &&
                        !sortedClass.equalsIgnoreCase(schoolClass)) {
                    parsedList.add(entryList.get(i).getEssentialInfo());
                }
            }
        }

        return parsedList;
    }

    /**
     * Gets all dates and returns them as a list of strings.
     */
    public String getDate(int offset) {
        Calendar calendar = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat().getDateTimeInstance();
        calendar.add(Calendar.DAY_OF_YEAR, offset);
        String strDate = dateFormat.format(calendar.getTime());
        return strDate;
    }

    /**
     * Counts how many html tables exist and returns the count.
     * @return
     */
    public int tableCount() {
        Elements tables = document.getElementsByTag("table");
        return tables.size();
    }

    /**
     * Parses the whole HTML file so only the relevant table content is contained
     * in the returned List of DataEntry's.
     * If the passed string is empty the program assumes that something went wrong
     * while fetching it from the website.
     * If there is no table content the program assumes that there is no representation plan.
     * @param html
     * @return
     */
    private List<DataEntry> parse(String html) {
        entryList = new ArrayList<>();

        //if the html string is empty return an error to the user
        if(html.isEmpty()) {
            entryList.add(  new DataEntry(
                    "Es konnte kein HTML Content gefetched werden."));
            return entryList;
        }

        //using the document to filter all table rows
        Elements tables = document.getElementsByTag("table");

        //if there are no table rows the user is being informed that there is no table atm
        if(tables.eachText().isEmpty()) {
            entryList.add(  new DataEntry(
                    "Es liegen keine aktuellen Vertretungspläne vor."));
            return entryList;
        }

        DataEntry entry;
        String firstField;
        Elements firstRow, secondRow, tableRows;
        Element e;
        //iterating through each table
        for(int i = 0; i < tables.size(); i++) {
            tableRows = tables.get(i).getElementsByTag("tr");

            for(Iterator<Element> element = tableRows.iterator(); element.hasNext();) {
                e = element.next();
                if(!e.children().hasText()) {
                    element.remove();
                    continue;
                }

                firstField = e.children().eachText().get(0);
                if(firstField.contains("Allgemeine") || firstField.contains("Klasse")) {
                    element.remove();
                    continue;
                }

                firstRow = e.getElementsByTag("td");
                e = element.next();
                secondRow = e.getElementsByTag("td");
                entry = new DataEntry(firstRow, secondRow, getDate(i));
                entryList.add(entry);
            }
        }

        return entryList;
    }

    public List<String> getParsedList() {
        return parsedList;
    }
}
