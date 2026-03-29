package com.alaka_ala.florafilm.utils.appUpdate.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OutputMetadata {
    @SerializedName("elements")
    private List<Element> elements;

    public List<Element> getElements() { return elements; }
}
