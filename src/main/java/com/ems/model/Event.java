package com.ems.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Event {
    private int id;
    private String title;
    private String description;
    private LocalDate eventDate;
    private String location;
    private int createdBy;
    private String createdByName;
    private String assignedGroupNames;
    private List<Integer> assignedGroupIds = new ArrayList<>();
    private Timestamp createdAt;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public String getAssignedGroupNames() {
        return assignedGroupNames;
    }

    public void setAssignedGroupNames(String assignedGroupNames) {
        this.assignedGroupNames = assignedGroupNames;
    }

    public List<Integer> getAssignedGroupIds() {
        return assignedGroupIds;
    }

    public void setAssignedGroupIds(List<Integer> assignedGroupIds) {
        this.assignedGroupIds = assignedGroupIds;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
