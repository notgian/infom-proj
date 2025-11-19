package org.example.eqlab2;

class InvalidFields extends Exception {
    public InvalidFields() {
        super("A provided field is incorrect");
    } 
    public InvalidFields(String message) {
        super(message);
    }
}
