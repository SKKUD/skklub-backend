package com.skklub.admin.controller;

import org.springframework.restdocs.snippet.Attributes;

public interface RestDocsUtils {
    static Attributes.Attribute example(String value){
        return new Attributes.Attribute("example", value);
    }
}
