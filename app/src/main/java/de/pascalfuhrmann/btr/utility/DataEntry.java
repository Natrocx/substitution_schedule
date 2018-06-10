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
package de.pascalfuhrmann.btr.utility;
import org.jsoup.select.Elements;

public class DataEntry {
    private String schoolClass;
    private String lesson;
    private String room;
    private String teacher;
    private String subject;
    private String reason;
    private String changedLesson;
    private String changedRoom;
    private String changedTeacher;
    private String changedSubject;
    private String date;

    /**
     * Constructor basically copy's the websites table
     * layout excluding empty table fields.
     * @param tableRow1
     * @param tableRow2
     */
    public DataEntry(Elements tableRow1, Elements tableRow2, String date) {
        schoolClass = tableRow1.eachText().get(0);
        this.date = date.substring(0, 12);
        lesson = tableRow1.eachText().get(2);

        if(tableRow1.eachText().size() > 7) {
            room = tableRow1.eachText().get(3);
            teacher = tableRow1.eachText().get(4);
            subject = tableRow1.eachText().get(5);
            reason = tableRow1.eachText().get(8);
            if(reason.isEmpty())
                reason = "Vertretung";
        } else {
            room = "";
            teacher = tableRow1.eachText().get(3);
            reason = tableRow1.eachText().get(6);
            if(reason.isEmpty())
                reason = "Vertretung";
        }

        if(!tableRow2.eachText().isEmpty()) {
            changedLesson = tableRow2.eachText().get(1);
            changedRoom = tableRow2.eachText().get(2);
            changedTeacher = tableRow2.eachText().get(3);
            changedSubject = tableRow2.eachText().get(4);
        }

        if(changedTeacher.isEmpty()) {
            changedTeacher = "";
        }
    }

    /**
     * Constructor mostly used for displaying errors in the list.
     * @param reason
     */
    public DataEntry(String reason) {
        this.reason = reason;
    }

    public String getSchoolClass() {
        return schoolClass;
    }

    public String getTeacher() {
        return teacher;
    }

    public String getChangedTeacher() {
        return changedTeacher;
    }

    public String getDate() {
        return date;
    }

    public String getEssentialInfo() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(schoolClass);
        buffer.append("\t\t");
        buffer.append(date);
        buffer.append("\n");
        buffer.append(lesson);
        buffer.append(" ");
        buffer.append(subject);
        buffer.append(" ");
        buffer.append(reason);
        buffer.append("\n");

        if (reason.equalsIgnoreCase("fällt aus")) {
            return buffer.toString();
        } else if (reason.equalsIgnoreCase("Vertretung")) {
            buffer.append("\t\t  bei " + changedTeacher + "\n");
            buffer.append("\t\t  in " + changedRoom + "\n");
            buffer.append("\t\t  Fach: " + subject + "\n");
            return buffer.toString();
        } else if (reason.equalsIgnoreCase("Raumänderung")) {
            buffer.append("\t\t  in " + changedRoom + "\n");
            return buffer.toString();
        } else if (reason.contains("vorgezogen")) {
            buffer.append("\t\t bei " + changedTeacher + "\n");
            buffer.append("\t\t in " + changedRoom + "\n");
            buffer.append("\t\t Fach: " + subject + "\n");
            return buffer.toString();
        } else if (!reason.isEmpty()) {
            buffer.append(schoolClass + " " + date + "\n");
            buffer.append(lesson + " " + subject + " " + "\n" + reason + "\n");
            return buffer.toString();
        } else {
            return "Error: Could not parse essential information";
        }
    }
}
