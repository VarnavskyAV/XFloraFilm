package com.alaka_ala.florafilm.utils.appUpdate.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@Keep
public class OutputMetadata {
    @SerializedName("elements")
    private List<Element> elements;

    public List<Element> getElements() { return elements; }
}
