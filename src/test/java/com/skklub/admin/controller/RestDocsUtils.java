package com.skklub.admin.controller;

import org.springframework.restdocs.snippet.Attributes;

import javax.swing.text.html.CSS;

public interface RestDocsUtils {
    static Attributes.Attribute example(String value){
        return new Attributes.Attribute("example", value);
    }
}
