/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fahdisa.smpp.module;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author prodigy4440
 */
public class MobileNetwork {
    
    private String name;
    private Network network;
    private Set<String> prefixes;
    private String id;

    private MobileNetwork(String name, Network network, Set<String> prefixes, String id) {
        this.name = name;
        this.network = network;
        this.prefixes = prefixes;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public Set<String> getPrefixes() {
        return prefixes;
    }

    public void setPrefix(Set<String> prefixes) {
        this.prefixes = prefixes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.name);
        hash = 17 * hash + Objects.hashCode(this.network);
        hash = 17 * hash + Objects.hashCode(this.prefixes);
        hash = 17 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MobileNetwork other = (MobileNetwork) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (this.network != other.network) {
            return false;
        }
        return Objects.equals(this.prefixes, other.prefixes);
    }

    @Override
    public String toString() {
        return "MobileNetwork{" + "name=" + name + ", network=" + network + ", prefixes=" + prefixes + ", id=" + id + '}';
    }

    
    public  static class Builder {

    private String name;
    private MobileNetwork.Network network;
    private Set<String> prefixes;
    private String id;

    public Builder() {
        prefixes = new HashSet<>();
    }

    public Builder setName(String name) {
        this.name = name;
        return this;
    }

    public Builder setNetwork(MobileNetwork.Network network) {
        this.network = network;
        return this;
    }

    public Builder addPrefix(String prefix) {
        this.prefixes.add(prefix);
        return this;
    }

    public Builder setId(String id) {
        this.id = id;
        return this;
    }

    public MobileNetwork build() {
        return new MobileNetwork(name, network, prefixes, id);
    }
    
}
    
    public enum Network{
        MTN, GLO, AIRTEL, ETISALAT,UNKNOWN;
    }
}
