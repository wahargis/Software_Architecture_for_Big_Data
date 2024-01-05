package io.collective

import java.time.Clock

class SimpleAgedKache(val cacheClock: Clock = Clock.systemUTC()) {
    //Description:
    //   a class for a simple aged cache that allows both permanent
    //   and expiring cache entries
    //   entries are stored as key-value pairs alongside their retention time in milliseconds

    private var cacheTimeInMillis : Int = 0
    private var currentIndex = 0
    var keys = arrayOfNulls<String>(0)
    var values = arrayOfNulls<Any?>(0)

    fun put(key: String, value: Any?, retentionInMillis: Int = 0) {
        //Description: 
        //   a class method for inserting a key-value 
        //   pair and length of retention
        //Parameters:
        //   key | string | the string for index-retrieval of the stored value
        //   value | any? | the stored value, can be arbitrary type
        //   retentionInMillis | Int | retention age in milliseconds
        
        // Clean up expired entries
        removeExpiredEntries()

        // Resize arrays if necessary
        if (currentIndex >= keys.size) {
            resizeArrays()
        }

        // add new entry
        keys[currentIndex] = key
        if (retentionInMillis == 0) {
            // retentionInMillis is not specified
            // values entry should not be an ExpirableEntry and can take value parameter
            values[currentIndex] = value
        } else {
            // retentionInMillis is specified
            values[currentIndex] = ExpirableEntry(value, retentionInMillis, cacheClock)    
        }
        //increment currentIndex
        currentIndex++
    }

    private fun resizeArrays() {
        // resize arrays by declaring new array of currentIndex+1 size
        //    copy old array entries into new array and then insert new entry in array
        
        // include handling for when initial case is 0
        val newSize = if (keys.isEmpty()) 1 else keys.size * 2
        
        val newKeys = arrayOfNulls<String>(newSize)
            val newValues = arrayOfNulls<Any?>(newSize)

            for (i in keys.indices){
                newKeys[i] = keys[i]
                newValues[i] = values[i]
            }

            keys = newKeys
            values = newValues
    }

    fun size(): Int {
        //Description:
        //   a class method that checks cache size
        //Parameters:
        //   N/A
        //Returns:
        //   the size of the cache as Int
        // cache size is assumed to be equal to currentIndex as array size varies ahead of cache growth
        
        // Clean up expired entries
        removeExpiredEntries()
        
        return currentIndex
    }

    fun isEmpty(): Boolean {
        //Description:
        //   a class method that checks if the cache is empty
        //Parameters:
        //   N/A
        //Returns:
        //   true/false based on if length of cache is/is not 0; Boolean
        return size() == 0
    }

    fun get(key: String): Any? {
        //Description:
        //   a class method that returns a value based on given key
        //Parameters:
        //   key | String | the string for index-retrieval of the stored value
        //Returns:
        //   value | Any? | the stored value for the key, can be arbitrary type 
        val index = keys.indexOf(key)

        // return null if key not in keys array (index of -1)
        if (index == -1) {
            println("Key not found in cache")
            return null
        }

        val value = values[index]
        if (value is ExpirableEntry) {
            // value entry is an ExpirableEntry
            if (value.isExpired()){
                // the value has expired
                println("Entry has expired")
                removeAt(index)
                return null
            }
            // the value has not expired
            return value.value // return the value property of the ExpirableEntry
        }
        // value is not an ExpirableEntry
        return value   // simply return the value retrieved from values array
    }

    private fun removeExpiredEntries() {
        var i = 0
        while (i < currentIndex) {
            val value = values[i]
            if (value is ExpirableEntry && value.isExpired()) {
                removeAt(i)
            } else {
                i++
            }
        }
    }

    private fun removeAt(index: Int){
        // eject key, value pair from keys, values arrays
        // eject by copying the array entries excluding the dropped entry
        //    and assigning the array entries to an array of one size less than the original array          
        val newSize = keys.size - 1
        val newKeys = arrayOfNulls<String>(newSize)
        val newValues = arrayOfNulls<Any?>(newSize)

        for (i in 0 until index) {
        // for indices below the expelled index
            newKeys[i] = keys[i]
            newValues[i] = values[i]
        }
        for (i in index + 1 until keys.size) {
        // for indices above the expelled index
            newKeys[i - 1] = keys[i]
            newValues[i - 1] = values[i]
        }

        keys = newKeys
        values = newValues
        currentIndex-- // decrement the currentIndex to account for drop
    }

    class ExpirableEntry(val value: Any?, val maxDurationInMillis : Int, private val cacheClock: Clock) {
        // an internal class to handle entries meant to expire over time
        val creationTime : Long = cacheClock.instant().toEpochMilli()

        fun isExpired(): Boolean {
            //Description:
            //   a class method that checks if the entry has expired
            //   expiration is determined by finding entry age and comparing to maxDurationInMillis
            //Parameters:
            //   N/A
            //Returns:
            //   a boolean

            val expired = (cacheClock.instant().toEpochMilli() - creationTime) >= maxDurationInMillis
            println("Checking expiration: Current Time = ${cacheClock.instant().toEpochMilli()}, Creation Time = $creationTime, Duration = $maxDurationInMillis, Expired = $expired")
            return expired
        }
    }
}