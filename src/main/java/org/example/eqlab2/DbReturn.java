package org.example.eqlab2;

/**
 * An object to hold the return status of a DbConnection query.
 * It has a type, title, and msg that can be accessed with getters.
 */
class DbReturn {
    public DbReturn(String type, String title, String msg) {
        this.type = type;
        this.title = title;
        this.message = msg;
    }

    public String getType() { return this.type; }
    public String getTitle() { return this.title; }
    public String getMessage() { return this.message; }

    private String type;
    private String title;
    private String message;
}
