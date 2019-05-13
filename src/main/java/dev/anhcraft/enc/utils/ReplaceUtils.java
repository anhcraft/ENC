package dev.anhcraft.enc.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReplaceUtils {
    public static Set<String> replaceVariables(List<String> data, HashMap<String, List<String>> groups, boolean caseSensitive){
        Set<String> n = new HashSet<>();
        for(String s : data){
            char a = s.charAt(0);
            String w = s.substring(1);
            if(a == '$'){
                if(groups.containsKey(w)){
                    for(String v : groups.get(w)){
                        n.add(caseSensitive ? v : v.toUpperCase());
                    }
                }
                continue;
            }
            else if(a == '-'){
                if(s.length() >= 2 && s.charAt(1) == '$'){
                    if(groups.containsKey(w)){
                        for(String v : groups.get(w)){
                            n.remove(caseSensitive ? v : v.toUpperCase());
                        }
                    }
                } else {
                    n.remove(caseSensitive ? w : w.toUpperCase());
                }
                continue;
            }
            else if(a == '\\' && s.length() >= 2) {
                char b = s.charAt(1);
                if(b == '$' || b == '-'){
                    s = w;
                }
            }
            n.add(caseSensitive ? s : s.toUpperCase());
        }
        return n;
    }
}
