package com.mycompany.tahiti.analysis.utils;

import lombok.val;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JenaUtil {
    public static Set<String> getSources(Model model) {
        Set<String> sources = new HashSet<>();
        val iter_subject = model.listSubjects();
        String prefix = "http://knowledge.richinfo.com/";
        while(iter_subject.hasNext()) {
            String subject = iter_subject.next().toString();
            if(!subject.startsWith(prefix)) {
                continue;
            }
            int index_slash = subject.indexOf("/", prefix.length());
            if(index_slash != -1) {
                sources.add(subject.substring(0, index_slash + 1));
            }
        }
        return sources;
    }

    public static Set<String> getSources(List<Resource> subjects) {
        Set<String> sources = new HashSet<>();
        for(val subject: subjects) {
            sources.add(getSource(subject.toString()));
        }
        return sources;
    }

    public static String getSource(String subject) {
        String prefix = "http://knowledge.richinfo.com/";
        int index_slash = subject.indexOf("/", prefix.length());
        if(index_slash != -1) {
            return subject.substring(0, index_slash + 1);
        }
        else throw new RuntimeException("Error in get source");
    }

    public static String getSourceName(String subject) {
        String prefix = "http://knowledge.richinfo.com/";
        int index_slash = subject.indexOf("/", prefix.length());
        if(index_slash != -1) {
            return subject.substring(prefix.length(), index_slash);
        }
        else throw new RuntimeException("Error in get source name");
    }

    public static String getObjectName(String subject) {
        String prefix = "http://knowledge.richinfo.com/";
        int index_slash = subject.indexOf("/", prefix.length());
        if(index_slash != -1) {
            return subject.substring(index_slash + 1);
        }
        else throw new RuntimeException("Error in get source name");
    }
}
