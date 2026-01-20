package com.ved.filehash.model;

public class HashResult {

    private String fileName;
    private String algorithm;
    private String hashValue;

    public HashResult(String fileName, String algorithm, String hashValue) {
        this.fileName = fileName;
        this.algorithm = algorithm;
        this.hashValue = hashValue;
    }

    public String getFileName() {
        return fileName;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getHashValue() {
        return hashValue;
    }
}
