package com.alaka_ala.florafilm.fragments.menu.models;

import androidx.annotation.AttrRes;

public class MenuItem {

    public enum ItemType {
        REGULAR,
        INFO,
        EXPANDABLE_INFO
    }

    private final ItemType type;
    private int imageResource;
    private String title;
    private String details;
    private String button1Text;
    private String button2Text;
    private int strokeColorAttr;
    private boolean isExpanded = false;

    // Конструкторы для EXPANDABLE_INFO
    public MenuItem(String title, String details) {
        this(title, details, null, null);
    }

    public MenuItem(String title, String details, String button1Text) {
        this(title, details, button1Text, null);
    }

    public MenuItem(String title, String details, String button1Text, String button2Text) {
        this.type = ItemType.EXPANDABLE_INFO;
        this.title = title;
        this.details = details;
        this.button1Text = button1Text;
        this.button2Text = button2Text;
    }

    // Другие конструкторы
    public MenuItem(String title) {
        this.type = ItemType.INFO;
        this.title = title;
    }

    public MenuItem(int imageResource, String title) {
        this(imageResource, title, 0);
    }

    public MenuItem(int imageResource, String title, @AttrRes int strokeColorAttr) {
        this.type = ItemType.REGULAR;
        this.imageResource = imageResource;
        this.title = title;
        this.strokeColorAttr = strokeColorAttr;
    }

    // Геттеры
    public ItemType getType() { return type; }
    public int getImageResource() { return imageResource; }
    public String getTitle() { return title; }
    public String getDetails() { return details; }
    public String getButton1Text() { return button1Text; }
    public String getButton2Text() { return button2Text; }
    @AttrRes public int getStrokeColorAttr() { return strokeColorAttr; }
    public boolean isExpanded() { return isExpanded; }

    // Сеттер
    public void setExpanded(boolean expanded) { isExpanded = expanded; }
}
