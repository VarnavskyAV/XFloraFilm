// TranslationResponse.java
package com.alaka_ala.florafilm.utils.balancers.hdvb.models;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public class TranslationResponse {
    private String file;

    @SerializedName("end_tag")
    private String endTag;

    private String title;
    private String translator;
    private String id;

    @SerializedName("text2")
    private String text2;

    // Getters and setters
    public String getFile() { return file; }
    public void setFile(String file) { this.file = file; }

    public String getEndTag() { return endTag; }
    public void setEndTag(String endTag) { this.endTag = endTag; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTranslator() { return translator; }
    public void setTranslator(String translator) { this.translator = translator; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getText2() { return text2; }
    public void setText2(String text2) { this.text2 = text2; }
}