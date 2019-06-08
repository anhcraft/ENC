package dev.anhcraft.enc.utils

import java.util.*

object ReplaceUtil {
    /*
    This method is used to compile a list with group placeholders (e.g: $all, $entities) into normal list
    - data: Is the list which you want to compile
    - groups: A map of group placeholders. Groups must be lowercase. eg:
        + $all -> list ["a", "b","c"]
        + $number -> list [1, 2, 3]
     - notUpCase: To tell the compiler not upper case elements

     Available placeholders:
     $<group>        : group addition
     -$<group>       : group exclusion
     -<element>      : element exclusion
     (*) groups are case-insensitive
     To add an element which is like a placeholder, put "\" at first e.g: \$<group>
     */
    @JvmStatic
    fun replaceVariables(data: Collection<String>, groups: Map<String, List<String>>, notUpCase: Boolean): Set<String> {
        val n = HashSet<String>() // set of elements
        for (s in data) {
            var sn = s
            val a = s[0] // the first character ($) which is used to indicate placeholders
            var w = s.substring(1) // the name of the placeholder
            if (a == '$') { // yeah, it is a group addition placeholder
                w = w.toLowerCase()
                if (groups.containsKey(w)) { // make sure it is available
                    for (v in groups.getValue(w)) n.add(if (notUpCase) v else v.toUpperCase())
                }
                continue
            } else if (a == '-') { // yeah, it is a exclusion placeholder
                if (s.length >= 2 && s[1] == '$') { // group exclusion
                    w = w.toLowerCase()
                    if (groups.containsKey(w)) {
                        for (v in groups.getValue(w)) n.remove(if (notUpCase) v else v.toUpperCase())
                    }
                } else // element exclusion
                    n.remove(if (notUpCase) w else w.toUpperCase())
                continue
            } else if (a == '\\' && s.length >= 2) { // it is not a placeholder, we have '\' at first!
                val b = s[1]
                if (b == '$' || b == '-') sn = w
            }
            n.add(if (notUpCase) sn else sn.toUpperCase())
        }
        return n
    }
}
