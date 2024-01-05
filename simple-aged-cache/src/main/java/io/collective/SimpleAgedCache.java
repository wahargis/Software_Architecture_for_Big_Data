package io.collective;

import java.time.Clock;

public class SimpleAgedCache {
    //Description:
    //   a class for a simple aged cache that allows both permanent
    //   and expiring cache entries
    //   entries are stored as key-value pairs alongside their retention time in milliseconds    
    
    private final Clock cacheClock;
    private int currentIndex = 0;
    String[] keys = new String[0]; // declaring zero-length array of String for keys
    Object[] values = new Object[0]; // declaring zero-length array of Object/Anytype for values

    // constructor to accept clock parameter
    public SimpleAgedCache(Clock cacheClock) {
        this.cacheClock = cacheClock;
    }

    // overloaded constructor for default value handling
    public SimpleAgedCache() {
        this(Clock.systemUTC()); // provides default clock if none provided
    }

    private static class ExpirableEntry {
        // an internal class to handle entries meant to expire over time
        private final Object value;
        private final int maxDurationInMillis;
        private final Clock cacheClock;
        private final long creationTime;

        // constructor to accept parameters
        private ExpirableEntry(Object value, int maxDurationInMillis, Clock cacheClock) {
            this.value = value;
            this.maxDurationInMillis = maxDurationInMillis;
            this.cacheClock = cacheClock;
            this.creationTime = cacheClock.instant().toEpochMilli();
        }

        public boolean isExpired() {
            //Description:
            //   a class method that checks if the entry has expired
            //   expiration is determined by finding entry age and comparing to maxDurationInMillis
            //Parameters:
            //   N/A
            //Returns:
            //   a boolean
            boolean expired = (cacheClock.instant().toEpochMilli() - creationTime) >= maxDurationInMillis;
            return expired;
        }
    }  

    private void resizeArrays() {
        // resize arrays by declaring new array of currentIndex+1 size
        //    copy old array entries into new array and then insert new entry in array
        
        // handling for initial case when size is 0
        int newSize = (keys.length == 0) ? (1) : (keys.length * 2);

        // inline if for java takes the form of (boolean condition) ? (true return) : (false return)
        //    unusually illegible quirk of the language

        String[] newKeys = new String[newSize];
        Object[] newValues = new Object[newSize];

        // java has no .indices property for arrays, so a length range iteration must be used
        for (int i = 0; i < keys.length; i++){
            newKeys[i] = keys[i];
            newValues[i] = values[i];
        }

        keys = newKeys;
        values = newValues;
    }

    private void removeAt(int index){
        // eject key, value pair from keys, values arrays
        // eject by copying the array entries excluding the dropped entry
        //    and assigning the array entries to an array of one size less than the original array          
        
        int newSize = keys.length - 1;
        String[] newKeys = new String[newSize];
        Object[] newValues = new Object[newSize];

        for (int i = 0; i < index; i++) {
        // for indices below the expelled index
            newKeys[i] = keys[i];
            newValues[i] = values[i];
        }
        for (int i = index +1; i < keys.length; i++) {
        // for indices above the expelled index
            newKeys[i - 1] = keys[i];
            newValues[i - 1] = values[i];
        }

        keys = newKeys;
        values = newValues;
        currentIndex--; // decrement the currentIndex to account for drop
    }

    private void removeExpiredEntries() {
        //Description:
        //   a method to check the values array and remove expired entries
        //Parameters:
        //   N/A
        //Returns:
        //   void
        int i = 0;
        while (i < currentIndex) {
            Object value = values[i];
            if ((value instanceof ExpirableEntry) && ((ExpirableEntry) value).isExpired()) {
                removeAt(i);
            } else {
                i++;
            }
        }
    }

    private int indexOf(Object[] array, Object element) {
        //Description:
        //   A method intended to mimic the behavior of Kotlin's array.indexOf() method for arrays.
        //   the method should return the index of an element in an array
        //   and return -1 when the element is not found
        //   due to exception handling in the SimpleagedKache class put method, this method can ignore null elements
        //Parameters:
        //   array | Object[] | an array that accepts object type
        //   element | Object | an element of object type to be checked for existence in the array
        //Returns:
        //   the index of the element when found
        //   -1 when element not found in array
        for (int i = 0; i < array.length; i++) {
            if ( (element != null) && (element.equals(array[i])) ){
                return i;
            }
        }
        return -1;
    }
    
    public int size() {
        //Description:
        //   a class method that checks cache size
        //Parameters:
        //   N/A
        //Returns:
        //   the size of the cache as Int
        //Note:
        // cache size is assumed to be equal to currentIndex as array size varies ahead of cache growth
        
        // Clean up expired entries
        this.removeExpiredEntries();

        return currentIndex;
    }

    public boolean isEmpty() {
        //Description:
        //   a class method that checks if the cache is empty
        //Parameters:
        //   N/A
        //Returns:
        //   true/false based on if length of cache is/is not 0; Boolean
        return this.size() == 0;  
    }

    private boolean isDuplicateKey(String key){
        //Description:
        //   a method to check if a key is already present in the keys array
        //Parameters:
        //   key | String | a String to be checked for existence in our keys array
        //Returns:
        //   boolean; True if key found in array, False if key not found in array
        return (this.indexOf(keys, key) != -1);
    }

    public void put(String key, Object value, int retentionInMillis) {
        //Description: 
        //   a class method for inserting a key-value 
        //   pair and length of retention
        //Parameters:
        //   key | string | the string for index-retrieval of the stored value
        //   value | any? | the stored value, can be arbitrary type
        //   retentionInMillis | Int | retention age in milliseconds
        //Returns:
        //   void

        // Clean up expired entries
        this.removeExpiredEntries();

        // disallow key-value pair assignment of null values
        if (value == null){
            throw new IllegalArgumentException("value cannot be null");
        }
        // disallow key-value pair assignment where key is null
        if (key == null){
            throw new IllegalArgumentException("key cannot be null");
        }
        // disallow duplicate key entries in keys array
        if (this.isDuplicateKey(key)){
            throw new IllegalArgumentException("key already exists in the array");
        }

        // Resize arrays if necessary
        if (currentIndex >= keys.length) {
            this.resizeArrays();
        }

        // add new entry
        keys[currentIndex] = key;

        if (retentionInMillis == 0) {
            // retentionInMillis is not specified
            // values entry should not be an ExpirableEntry and can take value parameter
            values[currentIndex] = value;
        } else {
            // retentionInMillis is specified
            values[currentIndex] = new ExpirableEntry(value, retentionInMillis, cacheClock);    
        }
        //increment currentIndex
        currentIndex++;
    }

    public Object get(String key) {
        //Description:
        //   a class method that returns a value based on given key
        //Parameters:
        //   key | String | the string for index-retrieval of the stored value
        //Returns:
        //   value | Any? | the stored value for the key, can be arbitrary type 
        
        int index = this.indexOf(keys, key);

        // return null if key not in keys array (index of -1)
        if (index == -1) {
            return null;
        }

        Object value = values[index];
        if (value instanceof ExpirableEntry) {
            // value entry is an ExpirableEntry
            if (((ExpirableEntry) value).isExpired()){
                // the value has expired
                removeAt(index);
                return null;
            }
            // the value has not expired
            return ((ExpirableEntry) value).value; // return the value property of the ExpirableEntry
        }
        // value is not an ExpirableEntry
        return value;   // simply return the value retrieved from values array
    }

}