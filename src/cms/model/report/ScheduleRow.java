package cms.model.report;

import cms.model.dto.Day;
import cms.model.dto.TimeSlot;

public final class ScheduleRow implements Comparable<ScheduleRow> {

    private final Day day;
    private final int startMinutes;
    private final String slotText;
    private final String courseCode;
    private final String courseTitle;
    private final String note;

    public ScheduleRow(TimeSlot slot, String courseCode, String courseTitle, String note) {
        this.day = slot.getDay();
        this.startMinutes = slot.getStartMinutes();
        this.slotText = slot.format();
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.note = note;
    }

    public Day getDay() {
        return day;
    }

    public String getSlotText() {
        return slotText;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public String getNote() {
        return note;
    }

    @Override
    public int compareTo(ScheduleRow other) {
        int byDay = Integer.compare(day.ordinal(), other.day.ordinal());
        return byDay != 0 ? byDay : Integer.compare(startMinutes, other.startMinutes);
    }
}
