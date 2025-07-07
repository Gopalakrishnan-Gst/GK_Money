package com.example.gkmoney;

public class Notes {
    private String NoteType;
    private String NoteDate;
    private String NoteAmount;
    private String NoteDescription;
    private String NoteCreatedBy;
    private String NoteCreatedOn;

    public Notes(){

    }


    public Notes(String noteType, String noteDate, String noteAmount, String noteDescription, String noteCreatedBy, String noteCreatedOn) {
        NoteType = noteType;
        NoteDate = noteDate;
        NoteAmount = noteAmount;
        NoteDescription = noteDescription;
        NoteCreatedBy = noteCreatedBy;
        NoteCreatedOn = noteCreatedOn;
    }

    public String getNoteType() {
        return NoteType;
    }

    public void setNoteType(String noteType) {
        NoteType = noteType;
    }

    public String getNoteDate() {
        return NoteDate;
    }

    public void setNoteDate(String noteDate) {
        NoteDate = noteDate;
    }

    public String getNoteAmount() {
        return NoteAmount;
    }

    public void setNoteAmount(String noteAmount) {
        NoteAmount = noteAmount;
    }

    public String getNoteDescription() {
        return NoteDescription;
    }

    public void setNoteDescription(String noteDescription) {
        NoteDescription = noteDescription;
    }

    public String getNoteCreatedOn() {
        return NoteCreatedOn;
    }

    public void setNoteCreatedOn(String noteCreatedOn) {
        NoteCreatedOn = noteCreatedOn;
    }

    public String getNoteCreatedBy() {
        return NoteCreatedBy;
    }

    public void setNoteCreatedBy(String noteCreatedBy) {
        NoteCreatedBy = noteCreatedBy;
    }
}
