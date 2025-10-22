package step2.classes;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class UTF8BundleReader {
    private final ResourceBundle bundle;
    public UTF8BundleReader(String baseName, Locale locale){
        this.bundle = ResourceBundle.getBundle(baseName,locale, new UTF8Control());
    }
    public String getKey(String key){
        String value = bundle.getString(key);
        byte[] bytes = value.getBytes(StandardCharsets.ISO_8859_1);
        return new String(bytes, StandardCharsets.UTF_8);
    }
    public String getString(String key){
        String value = bundle.getString(key);
        byte[] bytes = value.getBytes(StandardCharsets.ISO_8859_1);
        String res = new String(bytes,StandardCharsets.UTF_8);
        return value;
    }
    public Enumeration<String> getKeys(){
        return bundle.getKeys();
    }
}