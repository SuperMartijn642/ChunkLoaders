package com.supermartijn642.chunkloaders.util;

import java.util.Objects;

/**
 * Created 2/9/2021 by SuperMartijn642
 */
public class Pair<K, V> {

    private K key;
    private V value;

    public K getKey(){
        return this.key;
    }

    public V getValue(){
        return this.value;
    }

    public Pair(K key, V value){
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString(){
        return this.key + "=" + this.value;
    }

    @Override
    public int hashCode(){
        return this.key.hashCode() * 13 + (this.value == null ? 0 : this.value.hashCode());
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        Pair<?,?> pair = (Pair<?,?>)o;
        return Objects.equals(this.key, pair.key) && Objects.equals(this.value, pair.value);
    }
}
