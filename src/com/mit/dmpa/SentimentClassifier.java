package com.mit.dmpa;

import java.io.File;
import java.io.IOException;
import com.aliasi.classify.ConditionalClassification;
import com.aliasi.classify.LMClassifier;
import com.aliasi.util.AbstractExternalizable;

public class SentimentClassifier {

	String[] categories;
    LMClassifier c;  
    public SentimentClassifier() {  
    try {  
       c= (LMClassifier) AbstractExternalizable.readObject(new File("res\\classifier.txt"));  
       categories = c.categories();  
    }  
    catch (ClassNotFoundException e) {  
       e.printStackTrace();  
    }  
    catch (IOException e) {  
       e.printStackTrace();  
    }  
    }  
    public String classify(String text) {  
    ConditionalClassification classification = c.classify(text);  
    return classification.bestCategory();  
    }  
	
}
